package com.example.water_flow;

public class User {
    public String email;
    public String uid;
    public String password;
    public long lastLogin;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String email, String uid, String password, long lastLogin) {
        this.email = email;
        this.uid = uid;
        this.password = password;
        this.lastLogin = lastLogin;
    }
}