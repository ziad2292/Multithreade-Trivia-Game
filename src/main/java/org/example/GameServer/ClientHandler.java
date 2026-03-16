package org.example.GameServer;

import org.example.Auth.AuthService;
import org.example.LookUp.QuestionRepository;
import org.example.Storage.ScoreRepository;//M
import org.example.Models.Question;//M

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;//M

//Handle Communication with one specific client
public class ClientHandler implements Runnable{
    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username; //Maya

    public ClientHandler(Socket socket){
        this.socket = socket;
        ConnectionManager.addClient(this);
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

    private void handleMessage(String message) throws IOException {
        //TODO: send message to game logic
        String[] parts = message.split(" ");

        //M
        if (parts.length == 0) {
            sendMessage("Invalid Command");
            return;
        }

        String command = parts[0];

        switch (command) {

            case "LOGIN":

                if (parts.length != 3) {
                    sendMessage("INVALID_COMMAND");
                    return;
                }

                String loginResult =
                        AuthService.login(parts[1], parts[2]);

                sendMessage(loginResult);

                //Maya
                if ("Success".equals(loginResult)) {
                    username = parts[1];
                    showGameMenu();
                }
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

            case "QUIT":

                sendMessage("DISCONNECTED");
                cleanup();
                break;

            default:

                sendMessage("UNKNOWN_COMMAND");
        }
    }

    private void cleanup() {
        try {
            ConnectionManager.removeClient(this);
            socket.close();
        } catch (IOException ignored) {}
    }

    //Maya
    //First->hn present menu for single w multiplayer w quit
    private void showGameMenu() throws IOException {
            sendMessage("Choose Mode");
            sendMessage("1. Single Player");
            sendMessage("2. Multi Player");
            sendMessage("- Quit");

            String choice = input.readLine();

            if(choice == null) return; //Client disconnected
            
            switch (choice) {
                case "1":
                    startSinglePlayer();
                    break;

                case "2":
                    startMultiPlayer(username);

                case "-":
                    sendMessage("Disconnected");
                    cleanup();
                    break;

                default:
                    sendMessage("Invalid Choice");
                    showGameMenu();
            }
    }

    public void sendquestion(Question question) throws IOException {
        sendMessage("QUESTION: " + question.getText());
        sendMessage("A) " + question.getChoices().get(0));
        sendMessage("B) " + question.getChoices().get(1));
        sendMessage("C) " + question.getChoices().get(2));
        sendMessage("D) " + question.getChoices().get(3));
    }

    public String readAnswer() throws IOException {
        sendMessage("Your answer: ");
        return input.readLine(); // Assuming input is a BufferedReader for reading client input
    }

    private void startSinglePlayer() throws IOException {
        sendMessage("Start Single Player...");

        // 1. Load questions
        List<Question> questions = QuestionRepository.getRandomQuestions(5);

        int score = 0;

        for (Question q : questions) {
            sendquestion(q);
            String answer = readAnswer();

            if (answer == null) break;

            if (answer.equalsIgnoreCase(q.getCorrectAnswer())) {
                score++;
                sendMessage("Correct");
            } else {
                sendMessage("Wrong. Correct: " + q.getCorrectAnswer());
            }
        }

        ScoreRepository.updateScore(username, score);
        sendMessage("Game Over! Your score: " + score);
        showGameMenu();
    }

    private void startMultiPlayer(String username) throws IOException {
        sendMessage("Starting multiplayer game for: " + username);
        //Implement multiplayer game logic
    }

}




