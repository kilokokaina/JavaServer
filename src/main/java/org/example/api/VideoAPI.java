package org.example.api;

import org.example.server.RequestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class VideoAPI {

    private static void sendOK(OutputStream output, String extension, long length) throws IOException {
        String response = "HTTP/1.1 200 OK\n" +
                "Accept-Ranges: bytes\n" +
                String.format("Content-Type: video/%s\n", extension) +
                String.format("Content-Length: %d\n", length);

        output.write(response.getBytes(StandardCharsets.UTF_8));
    }

    public static void watch(DataOutputStream output, Map<String, String> headers, String path) throws IOException {
        String filename = path.split("/watch/")[1];
        String filePath = String.format(RequestUtils.RESOURCE, filename);
        String fileExtension = filename.split("\\.")[filename.split("\\.").length - 1];

        var response = new StringBuilder();

        try(var file = new RandomAccessFile(filePath, "r")) {
            try {
                String rangeHeader = headers.get("Range").split("=")[1];

                long start, end;
                String[] range = rangeHeader.split("-");

                start = Integer.parseInt(range[0]);
                if (range.length == 1) end = start + (1024 * 1024 * 11);
                else end = Integer.parseInt(range[1]);

                if (end > file.length()) end = file.length();

                byte[] buffer = new byte[(int) (end - start)];

                file.seek(start); file.readFully(buffer);

                response.append("HTTP/1.1 206 Partial content\n");
                response.append("Accept-Ranges: bytes\n");
                response.append(String.format("Content-Type: video/%s\n", fileExtension));
                response.append(String.format("Content-Length: %d\n", buffer.length));
                response.append(String.format("Content-Range: bytes %d-%d/%d\n", start, end, file.length()));
                response.append("\n");

                output.write(response.toString().getBytes(StandardCharsets.UTF_8));
                output.write(buffer);
            } catch (NullPointerException exception) {
                sendOK(output, fileExtension, file.length());
            }
        } catch (IOException exception) {
            output.write(("HTTP/1.1 404 Not Found").getBytes(StandardCharsets.UTF_8));
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), exception.getMessage());
        }
    }

    public static void download(DataOutputStream output, String path) throws IOException {
        String filename = path.split("/download/")[1];
        String filePath = String.format(RequestUtils.RESOURCE, filename);
        String fileExtension = filename.split("\\.")[filename.split("\\.").length - 1];

        var response = new StringBuilder();

        try(var file = new FileInputStream(filePath)) {
            long fileSize = Files.size(Path.of(filePath));

            response.append("HTTP/1.1 206 Partial content\n");
            response.append(String.format("Content-Type: video/%s\n", fileExtension));
            response.append(String.format("Content-Length: %d\n", fileSize));
            response.append(String.format("Content-Disposition: attachment; filename=\"%s\"", filename));
            response.append("\n");

            output.write(response.toString().getBytes(StandardCharsets.UTF_8));
            output.write(file.readAllBytes());
        } catch (IOException exception) {
            output.write(("HTTP/1.1 404 Not Found").getBytes(StandardCharsets.UTF_8));
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), exception.getMessage());
        }
    }

}
