package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static server.Constants.NUMBER_OF_SERVER_THREAD;
import static server.Constants.SERVER_PORT_NUMBER;

public class Server {

    private static final int serverPort = SERVER_PORT_NUMBER;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_SERVER_THREAD);

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("Started server on port: " + serverPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Future<?> submit = executorService.submit(new ClientHandler(clientSocket));

                try {
                    if (submit.get() == null) {
                        System.out.println("Successfully Handled Client Request!");
                    } else {
                        System.out.println("Client request handle was not successful!");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    System.out.println();
                }
            }
        } catch (IOException e) {
            System.out.println("Error while starting server. Error is: " + e.getLocalizedMessage());
        }
    }
}
