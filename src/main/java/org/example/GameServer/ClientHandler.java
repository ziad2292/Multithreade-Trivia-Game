package org.example.GameServer;

import org.example.Auth.AuthService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//Handle Communication with one specific client
// ToDo : handle set username in login
// ToDo : handle GameSession comm
public class ClientHandler implements Runnable{
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private volatile boolean connected = true;
    private String username;
    private boolean admin;
    private volatile GameSession activeGame;

    public ClientHandler(Socket socket){
        this.socket = socket;
        ConnectionManager.addClient(this);
    }
    public String getUsername() {
        return username;
    }



    @Override
    public void run() {
        try {

            input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            output = new PrintWriter(socket.getOutputStream(), true);

            String message;

            while ((message = input.readLine()) != null) {

                System.out.println("Received: " + message);

                handleMessage(message);
            }

        } catch (IOException e) {
            System.out.println("Client disconnected");
        }
        finally {
            cleanup();
        }
    }

    public void sendMessage(String message){
        if (output != null) {
            output.println(message);
        }
    }

    public boolean isLoggedIn() {
        return username != null;
    }

    public boolean isInGame() {
        return activeGame != null;
    }



    public void attachGame(GameSession gameSession) {
        this.activeGame = gameSession;
    }

    private void handleMessage(String message) {
        //TODO: send message to game logic
        String[] parts = message.split(" ");

        String command = parts[0];
        if ("ANSWER".equals(command)) {
            if (parts.length < 2) {
                sendMessage("ERROR Usage: ANSWER <A|B|C|D>");
                return;
            }

            String submitResult = GameManager.submitAnswer(this, parts[1]);
            if (submitResult.startsWith("ERROR") || submitResult.startsWith("INFO")) {
                sendMessage(submitResult);
            }
            return;
        }



        switch (command) {

            case "LOGIN":

                if (parts.length != 3) {
                    sendMessage("INVALID_COMMAND");
                    return;
                }

                String loginResult =
                        AuthService.login(parts[1], parts[2]);

                sendMessage(loginResult);
                if (loginResult.startsWith("200")) {
                    username = parts[1];
                    GameManager.onLogin(username, this);
                }

                break;


            case "LOGIN AS ADMIN":

                if (parts.length != 3) {
                    sendMessage("INVALID_COMMAND");
                    return;
                }

                String loginAsAdminResult =
                        AuthService.loginAsAdmin(parts[1], parts[2]);

                sendMessage(loginAsAdminResult);
                if (loginAsAdminResult.startsWith("200")) {
                    username = parts[1];
                    //Add you logic (Asmaa)
                    admin = true;

                }

                break;
            case "JOIN_PUBLIC":

                sendMessage(GameManager.joinPublicRoom(this));
                break;

            case "LEAVE_PUBLIC":

                sendMessage(GameManager.leavePublicRoom(this));
                break;

            case "PLAY_RANDOM":

                if (parts.length != 2) {
                    sendMessage("ERROR Usage: PLAY_RANDOM <questionsCount>");
                    return;
                }

                int randomQuestions = parseQuestionCount(parts[1]);
                if (randomQuestions <= 0) {
                    sendMessage("ERROR questionsCount must be a positive number.");
                    return;
                }

                sendMessage(GameManager.startSingleGame(this, "ANY", "ANY", randomQuestions));
                break;


            case "REGISTER":

                if (parts.length != 4) {
                    sendMessage("INVALID_COMMAND");
                    return;
                }

                String registerResult =
                        AuthService.register(parts[1], parts[2], parts[3]);

                sendMessage(registerResult);

                break;

            case "MENU":

                if (!isLoggedIn()) {
                    sendMessage("ERROR Please login first.");
                } else {
                    sendMessage(GameManager.menuText());
                }

                break;
            case "PLAY_SINGLE":

                if (parts.length != 4) {
                    sendMessage("ERROR Usage: PLAY_SINGLE <category|ANY> <difficulty|ANY> <questionsCount>");
                    return;
                }

                int singleQuestions = parseQuestionCount(parts[3]);
                if (singleQuestions <= 0) {
                    sendMessage("ERROR questionsCount must be a positive number.");
                    return;
                }

                sendMessage(GameManager.startSingleGame(this, parts[1], parts[2], singleQuestions));
                break;

            case "CREATE_TEAM":

                if (parts.length != 5) {
                    sendMessage("ERROR Usage: CREATE_TEAM <teamName> <category|ANY> <difficulty|ANY> <questionsCount>");
                    return;
                }

                int teamQuestions = parseQuestionCount(parts[4]);
                if (teamQuestions <= 0) {
                    sendMessage("ERROR questionsCount must be a positive number.");
                    return;
                }

                sendMessage(GameManager.createTeam(this, parts[1], parts[2], parts[3], teamQuestions));
                break;

            case "JOIN_TEAM":

                if (parts.length != 2) {
                    sendMessage("ERROR Usage: JOIN_TEAM <teamName>");
                    return;
                }

                sendMessage(GameManager.joinTeam(this, parts[1]));
                break;

            case "LEAVE_TEAM":

                sendMessage(GameManager.leaveTeam(this, false));
                break;

            case "MY_TEAM":

                sendMessage(GameManager.myTeamInfo(this));
                break;

            case "START_TEAM_GAME":

                if (parts.length != 2) {
                    sendMessage("ERROR Usage: START_TEAM_GAME <opponentTeamName>");
                    return;
                }

                sendMessage(GameManager.startTeamGame(this, parts[1]));
                break;

            case "HISTORY":

                if (!isLoggedIn()) {
                    sendMessage("ERROR Please login first.");
                    return;
                }

                sendMessage("HISTORY_BEGIN");
                for (String item : GameManager.getHistory(username)) {
                    sendMessage(item);
                }
                sendMessage("HISTORY_END");
                break;

            case "ADMIN_PANEL":

                if (!isLoggedIn()) {
                    sendMessage("ERROR Please login first.");
                    return;
                }

                if (!admin) {
                    sendMessage("ERROR Access denied. Admin user only.");
                    return;
                }

                sendMessage(GameManager.getAdminPanelText());
                break;


            case "QUIT":

                sendMessage("DISCONNECTED");
                cleanup();
                break;

            default:

                sendMessage("UNKNOWN_COMMAND");
        }
    }
    private int parseQuestionCount(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void cleanup() {
        try {
            ConnectionManager.removeClient(this);
            socket.close();
        } catch (IOException ignored) {}
    }
}


