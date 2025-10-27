package com.http.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class UserRegistryTest {

    private UserRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new UserRegistry();
    }

    @Test
    void testRegisterSuccess() {
        boolean result = registry.register("testuser", "password123");
        
        assertTrue(result);
        assertNotNull(registry.getUser("testuser"));
        assertEquals("testuser", registry.getUser("testuser").getUsername());
    }

    @Test
    void testRegisterDuplicateUsername() {
        registry.register("testuser", "password123");
        boolean result = registry.register("testuser", "differentpassword");
        
        assertFalse(result);
    }

    @Test
    void testLoginSuccess() {
        registry.register("testuser", "password123");
        String token = registry.login("testuser", "password123");
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testLoginUserNotFound() {
        String token = registry.login("nonexistent", "password123");
        
        assertNull(token);
    }

    @Test
    void testLoginWrongPassword() {
        registry.register("testuser", "password123");
        String token = registry.login("testuser", "wrongpassword");
        
        assertNull(token);
    }

    @Test
    void testValidateSessionValid() {
        registry.register("testuser", "password123");
        String token = registry.login("testuser", "password123");
        
        assertTrue(registry.validateSession(token));
    }

    @Test
    void testValidateSessionInvalid() {
        assertFalse(registry.validateSession("invalid-token"));
    }

    @Test
    void testGetSession() {
        registry.register("testuser", "password123");
        String token = registry.login("testuser", "password123");
        
        Session session = registry.getSession(token);
        assertNotNull(session);
        assertEquals("testuser", session.getUsername());
        assertEquals(token, session.getToken());
    }

    @Test
    void testRemoveSession() {
        registry.register("testuser", "password123");
        String token = registry.login("testuser", "password123");
        
        assertTrue(registry.validateSession(token));
        
        registry.removeSession(token);
        assertFalse(registry.validateSession(token));
    }

    @Test
    void testConcurrentRegistration() throws InterruptedException {
        int threadCount = 10;
        String username = "testuser";
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (registry.register(username, "password123")) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // Only one thread should successfully register the username
        assertEquals(1, successCount.get());
        assertNotNull(registry.getUser(username));
    }

    @Test
    void testConcurrentRegistrationDifferentUsers() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    if (registry.register("user" + userId, "password" + userId)) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        // All threads should successfully register different usernames
        assertEquals(threadCount, successCount.get());
    }

    @Test
    void testMultipleLoginsSameUser() {
        registry.register("testuser", "password123");
        
        String token1 = registry.login("testuser", "password123");
        String token2 = registry.login("testuser", "password123");
        
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
        
        assertTrue(registry.validateSession(token1));
        assertTrue(registry.validateSession(token2));
    }
}
