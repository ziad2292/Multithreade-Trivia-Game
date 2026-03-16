package org.example.GameServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScoreHistoryRepo {
    private static final Map<String, Deque<String>> historyByUser = new ConcurrentHashMap<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static String filePath;

    private ScoreHistoryRepo() {
    }

    public static synchronized void loadHistory(String path) {
        filePath = path;
        historyByUser.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 2);
                if (parts.length != 2) {
                    continue;
                }
                historyByUser.computeIfAbsent(parts[0], ignored -> new ArrayDeque<>()).addLast(parts[1]);
            }
            System.out.println("Score history loaded");
        } catch (IOException e) {
            System.out.println("Could not load score history file. Starting empty history.");
        }
    }

    public static synchronized void addEntry(String username, String mode, int score, String summary) {
        if (username == null || username.isBlank()) {
            return;
        }

        String cleanSummary = summary == null ? "" : summary.replace("\n", " ").trim();
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String record = timestamp + " | mode=" + mode + " | score=" + score + " | " + cleanSummary;

        Deque<String> entries = historyByUser.computeIfAbsent(username, ignored -> new ArrayDeque<>());
        entries.addFirst(record);

        int maxEntries = GameConfigRepo.getConfig().getMaxScoreHistoryEntries();
        while (entries.size() > maxEntries) {
            entries.removeLast();
        }

        persist();
    }

    public static List<String> getHistory(String username) {
        Deque<String> entries = historyByUser.get(username);
        if (entries == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(entries);
    }

    private static synchronized void persist() {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map.Entry<String, Deque<String>> entry : historyByUser.entrySet()) {
                for (String item : entry.getValue()) {
                    writer.write(entry.getKey() + "|" + item);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Could not persist score history");
        }
    }
}
