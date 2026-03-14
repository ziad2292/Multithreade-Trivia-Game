package org.example.GameServer;

import org.example.Auth.UserRepository;

public class ServerInitializer {

    public static void initialize() {

        System.out.println("Loading server data...");

        UserRepository.loadUsers("storage/users.txt");

        System.out.println("Server initialization complete");
    }
}
