package org.example.server;

import org.example.Main;
import org.example.api.UserAPI;
import org.example.api.VideoAPI;
import org.example.db.UserSession;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketThread implements Runnable {

    private final Socket socket;

    public SocketThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (var input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             var output = new DataOutputStream(socket.getOutputStream())) {

            var response = new StringBuilder();

            String request = input.readLine();
            System.out.println();
            System.out.printf("[%s]: Request: %s\n", Thread.currentThread().getName(), request);

            String method = request.split(" ")[0];
            String path = request.split(" ")[1];

            var headers = RequestUtils.getHeadersFromRequest(input);
            for (var header : headers.entrySet()) {
                String key = header.getKey();
                String value = header.getValue();

                System.out.printf("[%s]: Headers: %s: %s\n", Thread.currentThread().getName(), key, value);
            }

            if (Main.SECURED_PATH.contains(path)) {
                String sessionToken = headers.get("Cookie");

                if (sessionToken == null || UserSession.getUserBySession(sessionToken.split("=")[1]) == null) {
                    output.write(("HTTP/1.1 403 Forbidden\n").getBytes(StandardCharsets.UTF_8));
                    return;
                }
            }

            if (method != null && method.equals("GET") && path.contains("/static/")) {
                String[] resource = RequestUtils.getStaticByRequest(path);

                try(var resourcePath = new FileInputStream(resource[1])) {
                    byte[] bytes = resourcePath.readAllBytes();

                    response.append("HTTP/1.1 200 OK\n");
                    response.append(String.format("Content-Type: %s\n", resource[0]));
                    response.append("\n");

                    output.write(response.toString().getBytes(StandardCharsets.UTF_8));
                    output.write(bytes);
                }
            } else if (method != null && method.equals("GET") && !path.contains("/api/v1")) {
                String[] template = RequestUtils.getResourceByRequest(path);

                try (var resourcePath = new BufferedReader(new FileReader(template[1]))) {
                    response.append("HTTP/1.1 200 OK\n");
                    response.append(String.format("Content-Type: %s\n", template[0]));
                    response.append("\n");

                    String line;
                    while ((line = resourcePath.readLine()) != null) {
                        response.append(line);
                        response.append("\n");
                    }

                    output.write(response.toString().getBytes(StandardCharsets.UTF_8));
                } catch (NullPointerException | IOException e) {
                    output.write(("HTTP/1.1 404 Not Found\n").getBytes(StandardCharsets.UTF_8));
                    System.err.printf("[%s]: No such file or directory - %s\n", Thread.currentThread().getName(), path);
                }
            } else if (method != null && path.contains("/api/v1")) {
                String endpoint = path.split("/v1/")[1];

                if (endpoint.equals("login")) UserAPI.login(input, output);
                if (endpoint.equals("create")) UserAPI.create(input, output);
                if (endpoint.contains("watch")) VideoAPI.watch(output, headers, path);
                if (endpoint.contains("download")) VideoAPI.download(output, path);
            }
        } catch (NullPointerException | IOException e) {
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), e.getMessage());
        }
    }

}
