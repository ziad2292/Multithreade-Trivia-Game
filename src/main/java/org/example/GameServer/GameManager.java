package org.example.GameServer;

import java.io.IOException;//M
import java.util.List;

import org.example.LookUp.QuestionRepository;
import org.example.Models.Question;

public class GameManager {
    public static void startSinglePlayer(ClientHandler player) throws IOException {
        List<Question> questions = QuestionRepository.getRandomQuestions(5);
        int score = 0;

        for (Question q : questions) {
            try {
                player.sendquestion(q);
                String answer = player.readAnswer();
                if (answer.equalsIgnoreCase(q.getCorrectAnswer())) {
                    score++;
                    player.sendMessage("Correct!");
                } else {
                    player.sendMessage("Wrong! Correct answer: " + q.getCorrectAnswer());
                }
            } catch (IOException e) {
                System.err.println("Error during single-player game: " + e.getMessage());
            }
        }
        player.sendMessage("Game Over! Your score: " + score);
    }

    public static void startTeamGame(List<ClientHandler> players) throws IOException{
        for(Question q : QuestionRepository.getRandomQuestions(5)) {
            ServerBroadcaster.broadcastQuestion(players, q);
            evaluateAnswers(players, q);
        }
        endGame(players);
    }

    private static void evaluateAnswers(List<ClientHandler> players, Question question) {
        for (ClientHandler player : players) {
            try {
                // Read the player's answer
                String answer = player.readAnswer();

                // Check if the answer is correct
                if (answer.equalsIgnoreCase(question.getCorrectAnswer())) {
                    player.sendMessage("Correct!");
                } else {
                    player.sendMessage("Wrong! Correct answer: " + question.getCorrectAnswer());
                }
            } catch (IOException e) {
                System.err.println("Error evaluating answer for player: " + e.getMessage());
            }
        }
    }

    private static void endGame(List<ClientHandler> players) throws IOException {
        for (ClientHandler player : players) {
            player.sendMessage("Game Over! Thank you for playing.");
        }
    }

    //lo el game feh time limitation 
    public static void startCountdown() throws InterruptedException {
        int time = 15;
        while (time > 0) {
            Thread.sleep(5000);
            ServerBroadcaster.broadcast("Time left: " + time + " seconds");
            time -= 5;
        }
    }
}

