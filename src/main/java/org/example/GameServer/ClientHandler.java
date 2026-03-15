package org.example.GameServer;

import org.example.Auth.AuthService;

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
        ConnectionManager.addClient(this);
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
        String[] parts = message.split(" ");

        String command = parts[0];

        switch (command) {

            case "LOGIN":

                if (parts.length != 3) {
                    sendMessage("INVALID_COMMAND");
                    return;
                }

                String loginResult =
                        AuthService.login(parts[1], parts[2]);

                sendMessage(loginResult);

                break;

            case "REGISTER":

                if (parts.length != 4) {
                    sendMessage("INVALID_COMMAND");
                    return;
                }

                String registerResult =
                        AuthService.register(parts[1], parts[2], parts[3]);

                sendMessage(registerResult);

                break;

            case "QUIT":

                sendMessage("DISCONNECTED");
                cleanup();
                break;

            default:

                sendMessage("UNKNOWN_COMMAND");
        }
    }

    private void cleanup() {
        try {
            ConnectionManager.removeClient(this);
            socket.close();
        } catch (IOException ignored) {}
    }
}


