package org.example.LookUp;

import org.example.Models.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class LookUpClient {
    public static List<Question> requestQuestions(String category,
                                                  String difficulty,
                                                  int questionCount) {
        List<Question> questions = new ArrayList<>();

        try (Socket socket = new Socket("localhost", 6000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET_RANDOM|" + category + "|" + difficulty + "|" + questionCount);

            String status = in.readLine();
            if (status == null || !status.startsWith("OK|")) {
                return questions;
            }

            int count = parseCount(status);
            for (int i = 0; i < count; i++) {
                String line = in.readLine();
                if (line == null || line.isEmpty()) {
                    continue;
                }
                Question question = decodeQuestion(line);
                if (question != null) {
                    questions.add(question);
                }
            }

        } catch (IOException ignored) {
        }

        return questions;
    }

    public static String requestRandomQuestion() {
        List<Question> questions = requestQuestions("ANY", "ANY", 1);
        if (questions.isEmpty()) {
            return "ERROR";
        }
        return questions.get(0).getText();
    }

    private static int parseCount(String status) {
        try {
            String[] parts = status.split("\\|", 2);
            return Integer.parseInt(parts[1]);
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    private static Question decodeQuestion(String encodedLine) {
        try {
            String[] fields = encodedLine.split("\\|");
            if (fields.length != 8) {
                return null;
            }

            String text = decode(fields[0]);
            String category = decode(fields[1]);
            String difficulty = decode(fields[2]);
            List<String> choices = Arrays.asList(
                    decode(fields[3]),
                    decode(fields[4]),
                    decode(fields[5]),
                    decode(fields[6])
            );
            String answer = decode(fields[7]);

            return new Question(text, category, difficulty, choices, answer);
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static String decode(String value) {
        byte[] bytes = Base64.getDecoder().decode(value);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
