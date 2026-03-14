package org.example.LookUp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LookUpClient {
    public static String requestRandomQuestion() {

        try {

            Socket socket = new Socket("localhost", 6000);

            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            out.println("GET_RANDOM");

            return in.readLine();

        } catch (IOException e) {
            return "ERROR";
        }
    }
}
