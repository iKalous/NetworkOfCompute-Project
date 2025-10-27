package com.http.server;

import java.time.LocalDateTime;

/**
 * User data model representing a registered user in the system.
 */
public class User {
    private final String username;
    private final String passwordHash;
    private final LocalDateTime createdAt;

    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
