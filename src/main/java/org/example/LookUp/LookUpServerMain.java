package org.example.LookUp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class LookUpServerMain {

    private static final int PORT = 6000;

    public static void main(String[] args) {

        QuestionRepository.loadQuestions("storage/questions.txt");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("Lookup Server Running");

            while (true) {

                Socket socket = serverSocket.accept();

                new Thread(new LookUpHandler(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}