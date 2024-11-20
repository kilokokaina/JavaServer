package org.example.service;

import org.example.db.User;
import org.example.db.UserDB;
import org.example.exceptions.UserNotFound;

public class UserService {

    public static User createUser(String username, String password) {
        var user = new User(username, password);
        UserDB.addUser(user);

        return user;
    }

    public static User findUser(String username) {
        try {
            return UserDB.getUserByUsername(username);
        } catch (UserNotFound e) {
            System.err.printf("[%s]: %s\n", Thread.currentThread().getName(), e.getMessage());
            return null;
        }
    }

}
