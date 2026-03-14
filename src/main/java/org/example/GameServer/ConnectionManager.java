package org.example.GameServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Keep track of all connected clients
public class ConnectionManager {
    private static final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    public static void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("Client added. Total clients: " + clients.size());
    }

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client removed. Total clients: " + clients.size());
    }

    public static List<ClientHandler> getClients() {
        return clients;
    }


}
