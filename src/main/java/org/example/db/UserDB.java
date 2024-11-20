package org.example.db;

import org.example.exceptions.UserNotFound;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserDB {

    private final static List<User> users = new CopyOnWriteArrayList<>();

    public UserDB() {}

    public static User addUser(User user) {
        users.add(user.getUserId(), user);
        return user;
    }

    public static User getUserByID(int id) throws UserNotFound {
        if (users.get(id) == null) {
            throw new UserNotFound(String.format("No user with id %d", id));
        } else return users.get(id);
    }

    public static User getUserByUsername(String username) throws UserNotFound {
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }

        throw new UserNotFound(String.format("No user with name '%s'", username));
    }

    public static List<User> getAllUsers() {
        return users;
    }

    public static User deleteUser(User user) throws UserNotFound {
        if (users.get(user.getUserId()) == null) {
            throw new UserNotFound(String.format("No user with id %d", user.getUserId()));
        } else {
            var result = users.get(user.getUserId());
            users.remove(result);

            return result;
        }
    }

}
