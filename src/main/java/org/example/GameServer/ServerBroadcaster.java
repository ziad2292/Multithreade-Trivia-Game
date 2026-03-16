package org.example.GameServer;

import org.example.Models.Question;//M
import java.io.IOException;//M
import java.util.List;//M



//Send messages to multiple clients at once
public class ServerBroadcaster {
    public static void broadcast(String message) {

        for (ClientHandler client : ConnectionManager.getClients()) {
            client.sendMessage(message);
        }

    }

    //Maya
    public static void broadcastQuestion(List<ClientHandler> players, Question question) {
        for (ClientHandler player : players) {
            try {
                player.sendquestion(question);
            } catch (IOException e) {
                System.err.println("Error broadcasting question to player: " + e.getMessage());
            }
        }
    }
}
