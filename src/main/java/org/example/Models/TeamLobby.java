package org.example.Models;


import org.example.GameServer.ClientHandler;

import java.util.LinkedHashSet;
import java.util.Set;

public class TeamLobby {
    private final String name;
    private final String creatorUsername;
    private final String category;
    private final String difficulty;
    private final int questionCount;
    private final Set<ClientHandler> members = new LinkedHashSet<>();

    public TeamLobby(String name, String creatorUsername, String category, String difficulty, int questionCount) {
        this.name = name;
        this.creatorUsername = creatorUsername;
        this.category = category;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
    }

    public String getName() {
        return name;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public Set<ClientHandler> getMembers() {
        return members;
    }
}
