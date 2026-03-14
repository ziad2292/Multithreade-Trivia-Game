package org.example.Client;

import java.io.IOException;
import java.net.Socket;

public class ClientMain {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args){

        try{
            Socket socket = new Socket(SERVER_HOST, SERVER_PORT);

            System.out.println("Connected to server");

            ClientListener listener = new ClientListener(socket);
            ClientSender sender = new ClientSender(socket);

            new Thread(listener).start();
            new Thread(sender).start();
        }
        catch (IOException e){

        }
    }
}
