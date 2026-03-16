package org.example.GameServer;

import org.example.Models.TeamLobby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GameManager {
    private static final Map<String, ClientHandler> onlineUsers = new ConcurrentHashMap<>();
    private static final Map<String, TeamLobby> teams = new ConcurrentHashMap<>();
    private static final Map<String, String> userToTeam = new ConcurrentHashMap<>();
    private static final List<GameSession> activeGames = Collections.synchronizedList(new ArrayList<>());

    private GameManager() {
    }

    public static synchronized void onLogin(String username, ClientHandler handler) {
        onlineUsers.put(username, handler);
        handler.sendMessage("LOGIN_OK Welcome " + username);
        handler.sendMessage(menuText());
    }

    public static synchronized boolean isUserOnline(String username) {
        return onlineUsers.containsKey(username);
    }

    public static synchronized void onDisconnect(ClientHandler handler) {
        if (handler.getUsername() != null) {
            onlineUsers.remove(handler.getUsername());
            leaveTeam(handler, false);
            quitCurrentGame(handler, false);
        }
    }

    public static synchronized String createTeam(ClientHandler creator,
                                                 String teamName,
                                                 String category,
                                                 String difficulty,
                                                 int questionCount) {
        if (!creator.isLoggedIn()) {
            return "ERROR You must login first.";
        }

        String normalizedTeamName = normalizeTeamName(teamName);
        if (normalizedTeamName.isBlank()) {
            return "ERROR Team name is required.";
        }

        if (teams.containsKey(normalizedTeamName)) {
            return "ERROR Team name already exists. Team names must be unique.";
        }

        if (userToTeam.containsKey(creator.getUsername())) {
            return "ERROR You are already in a team. Leave current team first.";
        }

        TeamLobby team = new TeamLobby(normalizedTeamName,
                creator.getUsername(), category, difficulty, questionCount);
        team.getMembers().add(creator);

        teams.put(normalizedTeamName, team);
        userToTeam.put(creator.getUsername(), normalizedTeamName);

        return "TEAM_CREATED name=" + normalizedTeamName + " category=" + category
                + " difficulty=" + difficulty + " questions=" + questionCount;
    }

    public static synchronized String joinTeam(ClientHandler player, String teamName) {
        if (!player.isLoggedIn()) {
            return "ERROR You must login first.";
        }

        if (userToTeam.containsKey(player.getUsername())) {
            return "ERROR You are already in a team.";
        }

        TeamLobby team = teams.get(normalizeTeamName(teamName));
        if (team == null) {
            return "ERROR Team not found.";
        }

        int maxPlayers = GameConfigRepo.getConfig().getMaxPlayersPerTeam();
        if (team.getMembers().size() >= maxPlayers) {
            return "ERROR Team is full. maxPlayersPerTeam=" + maxPlayers;
        }

        team.getMembers().add(player);
        userToTeam.put(player.getUsername(), team.getName());

        notifyTeam(team, "TEAM_UPDATE " + player.getUsername() + " joined team " + team.getName());

        return "TEAM_JOINED " + team.getName();
    }

    public static synchronized String leaveTeam(ClientHandler player, boolean notifyPlayer) {
        String teamName = userToTeam.remove(player.getUsername());
        if (teamName == null) {
            return "ERROR You are not in a team.";
        }

        TeamLobby team = teams.get(teamName);
        if (team == null) {
            return "ERROR Team no longer exists.";
        }

        team.getMembers().remove(player);

        if (team.getMembers().isEmpty()) {
            teams.remove(teamName);
        } else {
            notifyTeam(team, "TEAM_UPDATE " + player.getUsername() + " left team " + teamName);
        }

        if (notifyPlayer) {
            player.sendMessage("TEAM_LEFT " + teamName);
        }

        return "TEAM_LEFT " + teamName;
    }

    public static synchronized String myTeamInfo(ClientHandler player) {
        String teamName = userToTeam.get(player.getUsername());
        if (teamName == null) {
            return "INFO You are not in a team.";
        }

        TeamLobby team = teams.get(teamName);
        if (team == null) {
            userToTeam.remove(player.getUsername());
            return "INFO Team no longer exists.";
        }

        List<String> names = new ArrayList<>();
        for (ClientHandler member : team.getMembers()) {
            names.add(member.getUsername());
        }

        return "MY_TEAM name=" + team.getName() + " creator=" + team.getCreatorUsername()
                + " members=" + names + " category=" + team.getCategory()
                + " difficulty=" + team.getDifficulty()
                + " questions=" + team.getQuestionCount();
    }

    public static synchronized String startSingleGame(ClientHandler player,
                                                      String category,
                                                      String difficulty,
                                                      int questionCount) {
        if (!player.isLoggedIn()) {
            return "ERROR You must login first.";
        }

        if (player.isInGame()) {
            return "ERROR You are already in a game.";
        }

        Map<String, String> teamsMap = new HashMap<>();
        teamsMap.put(player.getUsername(), "SOLO");
        GameSession session = new GameSession(
                Collections.singletonList(player),
                "SINGLE",
                category,
                difficulty,
                questionCount,
                teamsMap
        );

        activeGames.add(session);
        session.start();

        return "GAME_STARTING single player";
    }

    public static synchronized String startTeamGame(ClientHandler creator, String opponentTeamName) {
        if (!creator.isLoggedIn()) {
            return "ERROR You must login first.";
        }

        String teamName = userToTeam.get(creator.getUsername());
        if (teamName == null) {
            return "ERROR You are not in any team.";
        }

        TeamLobby teamA = teams.get(teamName);
        TeamLobby teamB = teams.get(normalizeTeamName(opponentTeamName));

        if (teamA == null || teamB == null) {
            return "ERROR Both teams must exist.";
        }

        if (!teamA.getCreatorUsername().equals(creator.getUsername())) {
            return "ERROR Only team creator can start a team game.";
        }

        if (teamA.getName().equals(teamB.getName())) {
            return "ERROR Opponent team must be different.";
        }

        int minPlayers = GameConfigRepo.getConfig().getMinPlayersPerTeam();
        int maxPlayers = GameConfigRepo.getConfig().getMaxPlayersPerTeam();

        if (teamA.getMembers().size() < minPlayers || teamB.getMembers().size() < minPlayers) {
            return "ERROR Teams do not meet minimum players per team=" + minPlayers;
        }

        if (teamA.getMembers().size() > maxPlayers || teamB.getMembers().size() > maxPlayers) {
            return "ERROR Teams exceed maximum players per team=" + maxPlayers;
        }

        if (teamA.getMembers().size() != teamB.getMembers().size()) {
            return "ERROR Team sizes are not equal. teamA=" + teamA.getMembers().size()
                    + " teamB=" + teamB.getMembers().size();
        }

        for (ClientHandler member : teamA.getMembers()) {
            if (member.isInGame()) {
                return "ERROR Team " + teamA.getName() + " has players already in game.";
            }
        }

        for (ClientHandler member : teamB.getMembers()) {
            if (member.isInGame()) {
                return "ERROR Team " + teamB.getName() + " has players already in game.";
            }
        }

        Collection<ClientHandler> participants = new ArrayList<>();
        participants.addAll(teamA.getMembers());
        participants.addAll(teamB.getMembers());

        Map<String, String> teamMapping = new LinkedHashMap<>();
        for (ClientHandler member : teamA.getMembers()) {
            teamMapping.put(member.getUsername(), teamA.getName());
        }
        for (ClientHandler member : teamB.getMembers()) {
            teamMapping.put(member.getUsername(), teamB.getName());
        }

        GameSession session = new GameSession(
                participants,
                "MULTI",
                teamA.getCategory(),
                teamA.getDifficulty(),
                teamA.getQuestionCount(),
                teamMapping
        );

        activeGames.add(session);
        session.start();

        notifyTeam(teamA, "GAME_STARTING vs " + teamB.getName());
        notifyTeam(teamB, "GAME_STARTING vs " + teamA.getName());

        return "TEAM_GAME_STARTED " + teamA.getName() + " vs " + teamB.getName();
    }

    public static synchronized String submitAnswer(ClientHandler player, String answer) {
        GameSession session = findSessionByUser(player.getUsername());
        if (session == null) {
            return "ERROR You are not in an active game.";
        }

        session.submitAnswer(player, answer);
        return "ANSWER_RECEIVED";
    }

    public static synchronized String quitCurrentGame(ClientHandler player, boolean sendMessages) {
        GameSession session = findSessionByUser(player.getUsername());
        if (session == null) {
            return "INFO You are not in a game.";
        }

        session.removePlayer(player);
        player.attachGame(null);
        if (sendMessages) {
            player.sendMessage("INFO You left the current game.");
            player.sendMessage(menuText());
        }
        return "GAME_LEFT";
    }

    public static synchronized List<String> getHistory(String username) {
        return ScoreHistoryRepo.getHistory(username);
    }

    public static synchronized void onSessionFinished(GameSession session) {
        activeGames.remove(session);
    }

    public static String menuText() {
        return "MENU:\n"
                + "1) PLAY_SINGLE <category|ANY> <difficulty|ANY> <questionsCount>\n"
                + "2) CREATE_TEAM <teamName> <category|ANY> <difficulty|ANY> <questionsCount>\n"
                + "3) JOIN_TEAM <teamName>\n"
                + "4) LEAVE_TEAM\n"
                + "5) MY_TEAM\n"
                + "6) START_TEAM_GAME <opponentTeamName> (team creator only)\n"
                + "7) HISTORY\n"
                + "8) MENU\n"
                + "Use '-' to quit current game anytime or QUIT to disconnect.";
    }

    private static GameSession findSessionByUser(String username) {
        synchronized (activeGames) {
            for (GameSession session : activeGames) {
                if (session.containsPlayer(username)) {
                    return session;
                }
            }
        }
        return null;
    }

    private static void notifyTeam(TeamLobby team, String message) {
        Set<ClientHandler> members = team.getMembers();
        for (ClientHandler member : members) {
            member.sendMessage(message);
        }
    }

    private static String normalizeTeamName(String teamName) {
        if (teamName == null) {
            return "";
        }
        return teamName.trim().toUpperCase(Locale.ROOT);
    }
}
