package org.example.GameServer;

import org.example.LookUp.LookUpClient;
import org.example.LookUp.QuestionRepository;
import org.example.Models.Question;

import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameSession implements Runnable {

    private final String mode; //single wala mutli
    private final List<ClientHandler> players = new CopyOnWriteArrayList<>(); // avoiding inconsitant behavior
    private final Map<String, String> userTeam = new HashMap<>(); // lel multi mode
    private final Map<String, Integer> scores = new LinkedHashMap<>();
    private final Map<String, List<String>> questionDetails = new HashMap<>();
    private final Object questionLock = new Object();
    private final String category;
    private final String difficulty;
    private final int questionCount;
    private volatile boolean running; // vol =>imidiate update for other threads
    private volatile boolean accepting;
    private volatile boolean forceclose;
    private Question currentQuestion;
    private final Map<String, String> currentAnswers = new HashMap<>();
    private List<Question> selectedQuestions = new ArrayList<>(); // I need it also for admin pannel to get Total questions played

    public GameSession(Collection<ClientHandler> participants,
                       String mode,
                       String category,
                       String difficulty,
                       int questionCount,
                       Map<String, String> userTeam) {
        this.players.addAll(participants);
        this.mode = mode;
        this.category = category;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
        this.userTeam.putAll(userTeam);

        for (ClientHandler player : players) {
            scores.put(player.getUsername(), 0);
            questionDetails.put(player.getUsername(), new ArrayList<>());
        }

    }
    public void start() {
        running = true;
        for (ClientHandler player : players) {
            player.attachGame(this);
        }
        new Thread(this, "GameSession-" + System.nanoTime()).start();
    }
    public boolean containsPlayer(String username) {
        return running && scores.containsKey(username);
    }

    public boolean submitAnswer(ClientHandler player, String answer) {
        if (!accepting || currentQuestion == null || !running) {
            player.sendMessage("INFO Answer ignored. Question is already closed.");
            return false;
        }

        String username = player.getUsername();
        synchronized (questionLock) {
            if (!accepting) {
                player.sendMessage("INFO Answer ignored. Question is already closed.");
                return false;
            }

            if (currentAnswers.containsKey(username)) {
                player.sendMessage("INFO You already answered this question. Only the first answer is considered.");
                return false;
            }

            String normalizedAnswer = normalize(answer);
            currentAnswers.put(username, normalizedAnswer);
            player.sendMessage("INFO Answer accepted: " + normalizedAnswer);

            String correct = normalize(currentQuestion.getCorrectAnswer());
            if (normalizedAnswer.equals(correct)) {
                forceclose = true;
                questionLock.notifyAll();
            }
            return true;
        }

    }

    private String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().toUpperCase();
    }

    public void removePlayer(ClientHandler player) {
        players.remove(player);
        if (players.isEmpty()) {
            running = false;
            GameManager.onSessionFinished(this);
            synchronized (questionLock) {
                forceclose = true;
                questionLock.notifyAll();
            }
        }
    }
    @Override
    public void run() {
        selectedQuestions = LookUpClient.requestQuestions(category, difficulty, questionCount);

        if (selectedQuestions.isEmpty()) {
            broadcast("ERROR No questions found for category='" + category + "' difficulty='" + difficulty + "'.");
            endGame();
            return;
        }

        broadcast("GAME_START mode=" + mode + " category=" + category + " difficulty=" + difficulty
                + " questions=" + selectedQuestions.size());

        for (int index = 0; index < selectedQuestions.size() && running; index++) {
            currentQuestion = selectedQuestions.get(index);
            runQuestion(index + 1, selectedQuestions.size(), currentQuestion);
        }

        endGame();
    }

    private void runQuestion(int number, int total, Question question) {
        synchronized (questionLock) {
            currentAnswers.clear();
            accepting = true;
            forceclose = false;
        }

        StringBuilder questionMessage = new StringBuilder();
        questionMessage.append("QUESTION ").append(number).append("/").append(total).append(" | ")
                .append(question.getText()).append("\n")
                .append("A) ").append(question.getChoices().get(0)).append("\n")
                .append("B) ").append(question.getChoices().get(1)).append("\n")
                .append("C) ").append(question.getChoices().get(2)).append("\n")
                .append("D) ").append(question.getChoices().get(3)).append("\n")
                .append("(answer with ANSWER <A|B|C|D> )");

        broadcast(questionMessage.toString());

        int duration = GameConfigRepo.getConfig().getQuestionDurationSeconds();
        Set<Integer> updatesToSend = buildCountdownUpdates(duration);
        Set<Integer> sentUpdates = new HashSet<>();

        long deadline = System.currentTimeMillis() + (duration * 1000L);
        synchronized (questionLock) {
            while (running && !forceclose) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    break;
                }

                int remainingSeconds = (int) Math.ceil(remaining / 1000.0);
                if (updatesToSend.contains(remainingSeconds) && !sentUpdates.contains(remainingSeconds)) {
                    sentUpdates.add(remainingSeconds);
                    broadcast("TIME_LEFT " + remainingSeconds + "s");
                }

                try {
                    questionLock.wait(Math.min(remaining, 250));
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            accepting = false;
        }

        evaluateCurrentQuestion();
        broadcastScoreboard();
    }








    public GameSession(String mode, String category, String difficulty, int questionCount) {
        this.mode = mode;
        this.category = category;
        this.difficulty = difficulty;
        this.questionCount = questionCount;
    }

    private Set<Integer> buildCountdownUpdates(int durationSeconds) {
        Set<Integer> updates = new HashSet<>();
        for (Integer update : GameConfigRepo.getConfig().getCountdownUpdates()) {
            if (update != null && update > 0 && update < durationSeconds) {
                updates.add(update);
            }
        }
        int half = durationSeconds / 2;
        if (half > 0 && half < durationSeconds) {
            updates.add(half);
        }
        return updates;
    }

    private void evaluateCurrentQuestion() {
        if (currentQuestion == null) {
            return;
        }

        String correct = normalize(currentQuestion.getCorrectAnswer());
        boolean someoneCorrect = false;

        for (ClientHandler player : players) {
            String username = player.getUsername();
            String answer = currentAnswers.get(username);

            if (answer == null) {
                questionDetails.get(username)
                        .add(currentQuestion.getText() + " -> NO_ANSWER (correct=" + correct + ")");
                continue;
            }

            boolean isCorrect = correct.equals(answer);
            if (isCorrect) {
                someoneCorrect = true;
                scores.put(username, scores.get(username) + 10);
                questionDetails.get(username)
                        .add(currentQuestion.getText() + " -> CORRECT (" + answer + ")");
            } else {
                questionDetails.get(username)
                        .add(currentQuestion.getText() + " -> WRONG (" + answer + ", correct=" + correct + ")");
            }
        }

        if (!someoneCorrect) {
            broadcast("QUESTION_RESULT No correct answer. Correct answer was " + correct + ".");
        } else {
            broadcast("QUESTION_RESULT A correct answer was submitted. Correct answer is " + correct + ".");
        }
    }

    private void broadcastScoreboard() {
        StringBuilder sb = new StringBuilder("SCORE_UPDATE ");
        if ("MULTI".equals(mode) && !userTeam.isEmpty()) {
            Map<String, Integer> teamScores = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> score : scores.entrySet()) {
                String team = userTeam.getOrDefault(score.getKey(), "NO_TEAM");
                teamScores.put(team, teamScores.getOrDefault(team, 0) + score.getValue());
            }
            sb.append("Teams: ").append(teamScores).append(" | Players: ").append(scores);
        } else {
            sb.append(scores);
        }
        broadcast(sb.toString());
    }

    private void endGame() {
        running = false;
        accepting = false;
        GameManager.onSessionFinished(this);

        List<Map.Entry<String, Integer>> ranking = new ArrayList<>(scores.entrySet());
        ranking.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder finalBoard = new StringBuilder();
        finalBoard.append("GAME_END Final Ranking:\n");
        int rank = 1;
        for (Map.Entry<String, Integer> entry : ranking) {
            finalBoard.append(rank++)
                    .append(". ")
                    .append(entry.getKey())
                    .append(" -> ")
                    .append(entry.getValue())
                    .append("\n");
        }

        finalBoard.append("DETAILS:\n");
        for (Map.Entry<String, List<String>> detail : questionDetails.entrySet()) {
            finalBoard.append(detail.getKey()).append(":\n");
            for (String item : detail.getValue()) {
                finalBoard.append(" - ").append(item).append("\n");
            }
        }

        broadcast(finalBoard.toString().trim());

        for (ClientHandler player : players) {
            String username = player.getUsername();
            int score = scores.getOrDefault(username, 0);
            ScoreHistoryRepo.addEntry(username, mode, score,
                    "category=" + category + ", difficulty=" + difficulty + ", questions=" + selectedQuestions.size());

            player.attachGame(null);
            player.sendMessage(GameManager.menuText());
        }
    }



    private void broadcast(String message) {
        for (ClientHandler player : players) {
            player.sendMessage(message);
        }
    }




}
