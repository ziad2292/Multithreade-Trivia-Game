package org.example.Models;

public class Admin {
    private final String name;
    private final String username;
    private final String password;

    public Admin(String name, String username, String password) {
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getName() { return name; }

    public String getUsername() { return username; }

    public String getPassword() { return password; }
}
