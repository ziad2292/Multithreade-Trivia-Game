package org.example.Storage;

import java.io.*;
//import java.util.Map;
//import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreRepository {
    private static final ConcurrentHashMap<String, Integer> scores = new ConcurrentHashMap<>();

    private static final String SCORE_FILE = "src/main/java/org/example/Storage/scores.txt";

    //Load scores from file
    public static void loadScores(String path) {
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("Scores file not found. Creating new one.");
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.out.println("Could not create scores file");
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    scores.put(username, score);
                }
            }
        }catch (IOException e) {
            System.out.println("Could not load scores file");
            e.printStackTrace();
        }
    }

    public static int getScore(String username) {
        return scores.getOrDefault(username,0);
    }

    public static void updateScore(String username, int score) {
        scores.put(username, score);
    }

    public static boolean hasScore(String username) {
        return scores.containsKey(username);
    }

    private static void saveScores() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(SCORE_FILE))) {
            for (String username : scores.keySet()) {
                writer.write(username + "," + scores.get(username));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Could not save scores file");
            e.printStackTrace();
        }
    }
}
