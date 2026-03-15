package org.example.Auth;

import org.example.Models.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class UserRepository {
    private static final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

    public static void loadUsers(String path) {

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split(",");

                String name = parts[0];
                String username = parts[1];
                String password = parts[2];

                users.put(username, new User(name, username, password));
            }

        } catch (IOException e) {
            System.out.println("Could not load users file");
            e.printStackTrace();
        }
    }

    public static User getUser(String username) {
        return users.get(username);
    }

    public static boolean userExists(String username) {
        return users.containsKey(username);
    }

    public static void addUser(User user) {
        users.put(user.getUsername(), user);
    }
}
