package org.example.GameServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//Run the server and accept client connections
public class TriviaServer {
    private static final int PORT = 5000;

    public static void main(String[] args){
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Trivia Server Started...");

            while (true) {
                Socket clientSocket = serverSocket.accept();

                System.out.println("New client connected");

                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
