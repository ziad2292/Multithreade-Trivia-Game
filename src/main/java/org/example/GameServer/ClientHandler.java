package org.example.GameServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//Handle Communication with one specific client
public class ClientHandler implements Runnable{
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;

    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            output = new PrintWriter(socket.getOutputStream(), true);

            String message;

            while ((message = input.readLine()) != null) {

                System.out.println("Received: " + message);

                handleMessage(message);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected");
        }
        finally {
            cleanup();
        }
    }

    public void sendMessage(String message){
        if (output != null) {
            output.println(message);
        }
    }

    private void handleMessage(String message) {
        //TODO: send message to game logic
        if (message.equalsIgnoreCase("QUIT")) {
            sendMessage("DISCONNECTED");
            cleanup();
        } else {
            System.out.println("Message: " + message);
        }
    }

    private void cleanup() {
        try {
            ConnectionManager.removeClient(this);
            socket.close();
        } catch (IOException ignored) {}
    }
}
