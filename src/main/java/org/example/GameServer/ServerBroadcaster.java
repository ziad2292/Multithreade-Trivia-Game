package org.example.GameServer;

//Send messages to multiple clients at once
public class ServerBroadcaster {
    public static void broadcast(String message) {

        for (ClientHandler client : ConnectionManager.getClients()) {
            client.sendMessage(message);
        }

    }
}
