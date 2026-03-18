package org.example.Auth;

import org.example.Models.Admin;
import org.example.Models.User;

public class AuthService {
    public static String login(String username, String password) {

        if (!UserRepository.userExists(username))
            return "404 USER_NOT_FOUND";

        User user = UserRepository.getUser(username);

        if (!user.getPassword().equals(password))
            return "401 UNAUTHORIZED";

        return "200 LOGIN_SUCCESS";
    }

    public static String loginAsAdmin(String username, String password) {

        if (!UserRepository.adminExists(username))
            return "404 USER_NOT_FOUND";

        Admin user = UserRepository.getAdmin(username);

        if (!user.getPassword().equals(password))
            return "401 UNAUTHORIZED";

        return "200 LOGIN_SUCCESS";
    }

    public static String register(String name, String username, String password) {

        if (UserRepository.userExists(username))
            return "USERNAME_TAKEN";

        User user = new User(name, username, password);

        UserRepository.addUser(user);

        return "200 REGISTER_SUCCESS";
    }
}
