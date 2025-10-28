package com.http.server;

import java.time.LocalDateTime;

/**
 * Session data model representing an active user session.
 */
public class Session {
    private final String token;
    private final String username;
    private final LocalDateTime createdAt;
    private LocalDateTime lastAccessTime;

    public Session(String token, String username) {
        this.token = token;
        this.username = username;
        this.createdAt = LocalDateTime.now();
        this.lastAccessTime = LocalDateTime.now();
    }

    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateLastAccessTime() {
        this.lastAccessTime = LocalDateTime.now();
    }
}
