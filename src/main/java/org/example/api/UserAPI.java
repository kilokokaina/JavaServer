package org.example.api;

import org.example.db.UserSession;
import org.example.exceptions.EmptyBody;
import org.example.server.RequestUtils;
import org.example.service.UserService;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class UserAPI {

    private static String[] getCredentials(String body) throws EmptyBody {
        System.out.printf("[%s]: Body: %s\n", Thread.currentThread().getName(), body);

        String[] credentials = body.split("&");

        try {
            String username = credentials[0].split("=")[1];
            String password = credentials[1].split("=")[1];

            return new String[]{username, password};
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new EmptyBody("Empty body");
        }
    }

    public static void create(BufferedReader input, DataOutputStream output) {
        String body = RequestUtils.getBodyFromRequest(input);
        var response = new StringBuilder();

        try {
            String[] credentials = getCredentials(body);
            var user = UserService.createUser(credentials[0], credentials[1]);

            response.append("HTTP/1.1 301 OK\n");
            response.append("Content-Type: application/json; charset=utf-8\n");
            response.append("Location: /\n");
            response.append("\n");

            output.write(response.toString().getBytes(StandardCharsets.UTF_8));
            output.write(user.toString().getBytes(StandardCharsets.UTF_8));
        } catch (EmptyBody exception) {
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), exception.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void login(BufferedReader input, DataOutputStream output) {
        String body = RequestUtils.getBodyFromRequest(input);
        var response = new StringBuilder();

        try {
            String[] credentials = getCredentials(body);
            var user = UserService.findUser(credentials[0]);

            if (user != null) {
                if (user.getPassword().equals(credentials[1])) {
                    String sessionToken = UserSession.startSession(user);

                    response.append("HTTP/1.1 301 OK\n");
                    response.append("Content-Type: application/json; charset=utf-8\n");
                    response.append(String.format("Set-Cookie: JSESSIONID=%s; Path=/\n", sessionToken));
                    response.append("Location: /secured\n");
                    response.append("\n");

                    output.write(response.toString().getBytes(StandardCharsets.UTF_8));
                    output.write(user.toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    response.append("HTTP/1.1 401 Wrong Credentials\n");
                    response.append("Content-Type: application/json; charset=utf-8\n");
                    response.append("\n");

                    output.write(response.toString().getBytes(StandardCharsets.UTF_8));
                    output.write(("{\"msg\":\"Wrong credentials\"}").getBytes(StandardCharsets.UTF_8));
                }
            } else {
                response.append("HTTP/1.1 404 User not found\n");
                response.append("Content-Type: application/json; charset=utf-8\n");
                response.append("\n");

                output.write(response.toString().getBytes(StandardCharsets.UTF_8));
                output.write(("{\"msg\":\"User not found\"}").getBytes(StandardCharsets.UTF_8));
            }
        } catch (EmptyBody exception) {
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), exception.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
