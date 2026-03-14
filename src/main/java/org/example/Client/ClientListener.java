package org.example.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientListener implements Runnable{

    private final Socket socket;
    private BufferedReader input;

    public ClientListener(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            String message;

            while ((message = input.readLine()) != null) {
                System.out.println("SERVER: " + message);
            }

        } catch (IOException e) {
            System.out.println("Disconnected from server");
        }
    }
}
