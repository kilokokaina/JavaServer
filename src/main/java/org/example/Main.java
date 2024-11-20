package org.example;

import org.example.server.SocketThread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class Main {

    public static final Set<String> SECURED_PATH = new HashSet<>();

    public static void main(String[] args) {
        try(var reader = new BufferedReader(new FileReader("src/main/resources/config/secured.path"))) {
            String path;
            while((path = reader.readLine()) != null) SECURED_PATH.add(path);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }

        try (var service = Executors.newFixedThreadPool(8);
             var server = new ServerSocket(8080)) {
            System.out.println("Server started...");

            for(;;) {
                var socket = server.accept();
                service.execute(new SocketThread(socket));
            }
        } catch(IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

}
