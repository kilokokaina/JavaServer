package org.example.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RequestUtils {

    public static final String TEMPLATE_PAGE = "src/main/resources/pages/%s.html";
    public static final String RESOURCE = "src/main/resources/static/%s";

    public static String[] getResourceByRequest(String path) {
        if (path.length() > 1) path = path.substring(1);
        String result = null;

        if (!path.contains("static/")) {
            if (path.equals("/")) result = String.format(TEMPLATE_PAGE, "home");
            else result = String.format(TEMPLATE_PAGE, path);
        }

        return new String[] { "text/html; charset=utf-8", result };
    }

    public static String[] getStaticByRequest(String request) {
        String path = request.split("static/")[1];
        String resourceExtension = path.split("\\.")[path.split("\\.").length - 1];

        String contentType;
        switch (resourceExtension) {
            case "js" -> contentType = "text/javascript";
            case "css" -> contentType = "text/css";
            case "png" -> contentType = "image/png";
            case "webm" -> contentType = "video/webm";
            default -> contentType = "*/*";
        }

        return new String[] { contentType, String.format(RESOURCE, path) };
    }

    public static Map<String, String> getHeadersFromRequest(BufferedReader input) {
        var headers = new HashMap<String, String>();

        try {
            String payload;
            while ((payload = input.readLine()) != null) {
                if (payload.isEmpty()) break;

                String[] header = payload.split(": ");
                headers.put(header[0], header[1]);
            }
        } catch (IOException e) {
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), e.getMessage());
        }

        return headers;
    }

    public static String getBodyFromRequest(BufferedReader input) {
        var body = new StringBuilder();

        try {
            while(input.ready()) body.append((char) input.read());
        } catch (IOException e) {
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), e.getMessage());
        }

        return body.toString().trim();
    }

}
