package org.example.LookUp;

import org.example.Models.Question;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionRepository {
    private static final List<Question> questions = new ArrayList<>();

    public static void loadQuestions(String path) {

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",");

                String text = parts[0];
                String category = parts[1];
                String difficulty = parts[2];

                List<String> choices = Arrays.asList(
                        parts[3], parts[4], parts[5], parts[6]
                );

                String answer = parts[7];

                questions.add(new Question(text, category,
                        difficulty, choices, answer));
            }

        } catch (IOException e) {
            System.out.println("Could not load questions");
        }
    }

    public static List<Question> getQuestions() {

        System.out.println("question : "+questions);
        return questions;
    }
}
