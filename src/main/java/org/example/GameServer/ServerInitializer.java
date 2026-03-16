package org.example.GameServer;

import org.example.Auth.UserRepository;
import org.example.LookUp.QuestionRepository;//M
import org.example.Storage.ScoreRepository;//M

public class ServerInitializer {

    public static void initialize() {

        System.out.println("Loading server data...");

        UserRepository.loadUsers("src/main/java/org/example/Storage/users.txt");

        //Maya
        QuestionRepository.loadQuestions("src/main/java/org/example/LookUp/questions.txt");
        ScoreRepository.loadScores("src/main/java/org/example/Storage/scores.txt");

        System.out.println("Server initialization complete");
    }
}
