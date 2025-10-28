package com.http.server;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserRegistry manages user registration, authentication, and session management.
 * Thread-safe implementation using ConcurrentHashMap for concurrent access.
 */
public class UserRegistry {
    private final ConcurrentHashMap<String, User> users;
    private final ConcurrentHashMap<String, Session> sessions;

    public UserRegistry() {
        this.users = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * Register a new user with the given username and password.
     * This method is synchronized to ensure atomic check-and-insert operation.
     *
     * @param username the username to register
     * @param password the password for the user
     * @return true if registration successful, false if username already exists
     */
    public synchronized boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        
        // In a real application, password should be hashed with a proper algorithm (e.g., BCrypt)
        // For this implementation, we'll store it as-is (as per design document note)
        User user = new User(username, password);
        users.put(username, user);
        return true;
    }

    /**
     * Authenticate a user and create a session.
     *
     * @param username the username to authenticate
     * @param password the password to verify
     * @return session token if authentication successful, null otherwise
     */
    public String login(String username, String password) {
        User user = users.get(username);
        
        if (user == null) {
            return null;
        }
        
        if (!user.getPasswordHash().equals(password)) {
            return null;
        }
        
        // Generate unique session token
        String token = UUID.randomUUID().toString();
        Session session = new Session(token, username);
        sessions.put(token, session);
        
        return token;
    }

    /**
     * Validate if a session token is valid and active.
     *
     * @param token the session token to validate
     * @return true if session is valid, false otherwise
     */
    public boolean validateSession(String token) {
        Session session = sessions.get(token);
        
        if (session == null) {
            return false;
        }
        
        // Update last access time
        session.updateLastAccessTime();
        return true;
    }

    /**
     * Get session by token.
     *
     * @param token the session token
     * @return Session object if found, null otherwise
     */
    public Session getSession(String token) {
        return sessions.get(token);
    }

    /**
     * Remove a session (logout).
     *
     * @param token the session token to remove
     */
    public void removeSession(String token) {
        sessions.remove(token);
    }

    /**
     * Get user by username.
     *
     * @param username the username
     * @return User object if found, null otherwise
     */
    public User getUser(String username) {
        return users.get(username);
    }
}
