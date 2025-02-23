package org.example.db;

public class User {

    public static int id;

    private final int userId;
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.userId = id++;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return String.format("{\"userId\":%d,\"username\":\"%s\",\"password\":\"%s\"}", userId, username, password);
    }

}
