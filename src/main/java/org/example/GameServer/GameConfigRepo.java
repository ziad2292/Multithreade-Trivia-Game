package org.example.GameServer;

import org.example.Models.GameConfig;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameConfigRepo {

    private static final GameConfig CONFIG = new GameConfig();

    private GameConfigRepo() {
    }

    public static void loadConfig(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                String key = parts[0].trim().toLowerCase();
                String value = parts[1].trim();
                applyConfig(key, value);
            }
        } catch (IOException e) {
            System.out.println("Could not load game config file. Using defaults.");
        }

        System.out.println("Game config loaded: minPlayers=" + CONFIG.getMinPlayersPerTeam()
                + ", maxPlayers=" + CONFIG.getMaxPlayersPerTeam()
                + ", questionDuration=" + CONFIG.getQuestionDurationSeconds() + "s");
    }

    private static void applyConfig(String key, String value) {
        try {
            switch (key) {
                case "min_players_per_team":
                    CONFIG.setMinPlayersPerTeam(Integer.parseInt(value));
                    break;
                case "max_players_per_team":
                    CONFIG.setMaxPlayersPerTeam(Integer.parseInt(value));
                    break;
                case "question_duration_seconds":
                    CONFIG.setQuestionDurationSeconds(Integer.parseInt(value));
                    break;
                case "max_score_history_entries":
                    CONFIG.setMaxScoreHistoryEntries(Integer.parseInt(value));
                    break;
                case "countdown_updates":
                    String[] tokens = value.split(",");
                    List<Integer> updates = new ArrayList<>();
                    for (String token : tokens) {
                        updates.add(Integer.parseInt(token.trim()));
                    }
                    CONFIG.setCountdownUpdates(updates);
                    break;
                default:
                    break;
            }
        } catch (NumberFormatException ignored) {
            System.out.println("Invalid config value for key: " + key);
        }
    }

    public static GameConfig getConfig() {
        return CONFIG;
    }
}

