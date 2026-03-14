package org.example.Models;

import java.util.List;

public class Question {
    private final String text;
    private final String category;
    private final String difficulty;
    private final List<String> choices;
    private final String correctAnswer;

    public Question(String text, String category,
                    String difficulty, List<String> choices,
                    String correctAnswer) {

        this.text = text;
        this.category = category;
        this.difficulty = difficulty;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
    }

    public String getText() { return text; }

    public String getCategory() { return category; }

    public String getDifficulty() { return difficulty; }

    public List<String> getChoices() { return choices; }

    public String getCorrectAnswer() { return correctAnswer; }
}
