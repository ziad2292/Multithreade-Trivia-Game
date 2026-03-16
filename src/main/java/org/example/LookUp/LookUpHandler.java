package org.example.LookUp;


import org.example.Models.Question;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class LookUpHandler implements Runnable{

    private final Socket socket;

    public LookUpHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String request = in.readLine();
            if (request == null) {
                return;
            }

            String[] parts = request.split("\\|", 4);
            if (!"GET_RANDOM".equals(parts[0])) {
                out.println("ERROR Unsupported request");
                return;
            }

            String category = parts.length > 1 ? parts[1] : "ANY";
            String difficulty = parts.length > 2 ? parts[2] : "ANY";
            int questionCount = parts.length > 3 ? parseQuestionCount(parts[3]) : 1;

            List<Question> selected = selectQuestions(category, difficulty, questionCount);

            out.println("OK|" + selected.size());
            for (Question question : selected) {
                out.println(encodeQuestion(question));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private List<Question> selectQuestions(String category, String difficulty, int questionCount) {
        List<Question> filtered = new ArrayList<>();

        for (Question question : QuestionRepository.getQuestions()) {
            System.out.println("Questions : " + question.getText());
            boolean categoryMatch = "ANY".equalsIgnoreCase(category)
                    || question.getCategory().equalsIgnoreCase(category);
            boolean difficultyMatch = "ANY".equalsIgnoreCase(difficulty)
                    || question.getDifficulty().equalsIgnoreCase(difficulty);
            if (categoryMatch && difficultyMatch) {
                filtered.add(question);
            }
        }

        if (filtered.isEmpty()) {
            return filtered;
        }

        Collections.shuffle(filtered);
        int effectiveCount = Math.min(Math.max(1, questionCount), filtered.size());
        return new ArrayList<>(filtered.subList(0, effectiveCount));
    }

    private int parseQuestionCount(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String encodeQuestion(Question question) {
        List<String> values = new ArrayList<>();
        values.add(question.getText());
        values.add(question.getCategory());
        values.add(question.getDifficulty());
        values.add(question.getChoices().get(0));
        values.add(question.getChoices().get(1));
        values.add(question.getChoices().get(2));
        values.add(question.getChoices().get(3));
        values.add(question.getCorrectAnswer());

        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                encoded.append("|");
            }
            encoded.append(Base64.getEncoder().encodeToString(values.get(i).getBytes(StandardCharsets.UTF_8)));
        }
        return encoded.toString();
    }
}

