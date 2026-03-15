package org.example.GameServer;

import org.example.Auth.UserRepository;

public class ServerInitializer {

    public static void initialize() {

        System.out.println("Loading server data...");

        UserRepository.loadUsers("src/main/java/org/example/Storage/users.txt");

        System.out.println("Server initialization complete");
    }
}
