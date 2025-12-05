package com.http.server;

import java.time.LocalDateTime;

/**
 * 用户数据模型，表示系统中的注册用户
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
