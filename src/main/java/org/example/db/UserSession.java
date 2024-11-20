package org.example.db;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserSession {

    public static final Map<String, User> userSession = new ConcurrentHashMap<>();

    public static String startSession(User user) {
        String sessionUUID = UUID.randomUUID().toString();
        userSession.put(sessionUUID, user);

        return sessionUUID;
    }

    public static User getUserBySession(String sessionUUID) {
        return userSession.get(sessionUUID);
    }

}
