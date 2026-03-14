package org.example.LookUp;

import org.example.Models.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LookUpHandler implements Runnable{

    private final Socket socket;

    public LookUpHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {

            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

            PrintWriter out =
                    new PrintWriter(socket.getOutputStream(), true);

            String request = in.readLine();

            if (request.equals("GET_RANDOM")) {

                Question q =
                        QuestionRepository.getQuestions()
                                .get(0);

                out.println(q.getText());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
