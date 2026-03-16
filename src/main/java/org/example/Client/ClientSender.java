package org.example.Client;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClientSender implements Runnable{
    private final Socket socket;
    private PrintWriter output;

    public ClientSender(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {

            output = new PrintWriter(
                    socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            while (true) {

                String input = scanner.nextLine();

                output.println(input);

                if (input.equalsIgnoreCase("QUIT") || input.equals("-")) {
                    socket.close();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
