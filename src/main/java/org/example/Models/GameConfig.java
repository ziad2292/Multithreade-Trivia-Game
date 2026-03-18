package org.example.Models;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameConfig {
    private int minPlayersPerTeam = 1;
    private int minPlayersPublicRoom = 2;
    private int maxPlayersPublicRoom = 4;
    private int maxPlayersPerTeam = 4;
    private int questionDurationSeconds = 15;
    private int maxScoreHistoryEntries = 10;
    private final List<Integer> countdownUpdates = new ArrayList<>();
    //TODO
    public GameConfig() {
        countdownUpdates.add(10);
        countdownUpdates.add(5);
        countdownUpdates.add(3);
        countdownUpdates.add(2);
        countdownUpdates.add(1);
    }

    public int getMinPlayersPublicRoom() {
        return minPlayersPublicRoom;
    }

    public void setMinPlayersPublicRoom(int minPlayersPublicRoom) {
        this.minPlayersPublicRoom = Math.max(1, minPlayersPublicRoom);
    }

    public int getMaxPlayersPublicRoom() {
        return maxPlayersPublicRoom;
    }

    public void setMaxPlayersPublicRoom(int maxPlayersPublicRoom) {
        this.maxPlayersPublicRoom = Math.max(1, maxPlayersPublicRoom);
    }

    public int getMinPlayersPerTeam() {
        return minPlayersPerTeam;
    }

    public void setMinPlayersPerTeam(int minPlayersPerTeam) {
        this.minPlayersPerTeam = Math.max(1, minPlayersPerTeam);
    }

    public int getMaxPlayersPerTeam() {
        return maxPlayersPerTeam;
    }

    public void setMaxPlayersPerTeam(int maxPlayersPerTeam) {
        this.maxPlayersPerTeam = Math.max(1, maxPlayersPerTeam);
    }

    public int getQuestionDurationSeconds() {
        return questionDurationSeconds;
    }

    public void setQuestionDurationSeconds(int questionDurationSeconds) {
        this.questionDurationSeconds = Math.max(5, questionDurationSeconds);
    }

    public int getMaxScoreHistoryEntries() {
        return maxScoreHistoryEntries;
    }

    public void setMaxScoreHistoryEntries(int maxScoreHistoryEntries) {
        this.maxScoreHistoryEntries = Math.max(1, maxScoreHistoryEntries);
    }

    public List<Integer> getCountdownUpdates() {
        return Collections.unmodifiableList(countdownUpdates);
    }

    public void setCountdownUpdates(List<Integer> updates) {
        countdownUpdates.clear();
        for (Integer update : updates) {
            if (update != null && update > 0) {
                countdownUpdates.add(update);
            }
        }
        if (countdownUpdates.isEmpty()) {
            countdownUpdates.add(5);
            countdownUpdates.add(3);
            countdownUpdates.add(1);
        }
    }

}
