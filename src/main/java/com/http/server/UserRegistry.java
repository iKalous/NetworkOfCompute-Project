package com.http.server;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * UserRegistry 管理用户注册、认证和会话管理
 * 使用 ConcurrentHashMap 实现线程安全，支持并发访问
 */
public class UserRegistry {
    private final ConcurrentHashMap<String, User> users;
    private final ConcurrentHashMap<String, Session> sessions;

    public UserRegistry() {
        this.users = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
    }

    /**
     * 使用给定的用户名和密码注册新用户
     * 该方法使用 synchronized 关键字确保原子性的检查和插入操作
     *
     * @param username 要注册的用户名
     * @param password 用户的密码
     * @return 如果注册成功返回 true，如果用户名已存在则返回 false
     */
    public synchronized boolean register(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }

        // 在实际应用中，密码应该使用适当的算法进行哈希处理
        // 在此实现中，我们将按原样存储密码（根据设计文档说明）
        User user = new User(username, password);
        users.put(username, user);
        return true;
    }

    /**
     * 验证用户身份并创建会话
     *
     * @param username 要验证的用户名
     * @param password 要验证的密码
     * @return 如果验证成功则返回会话令牌，否则返回 null
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
     * 验证会话 token 是否有效且处于活动状态。
     *
     * @param token 要验证的会话 token
     * @return 如果会话有效则返回 true，否则返回 false
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
     * 根据令牌获取会话
     *
     * @param token 会话 token
     * @return 如果找到则返回 Session 对象，否则返回 null
     */
    public Session getSession(String token) {
        return sessions.get(token);
    }

    /**
     * 移除会话（注销登录）
     *
     * @param token 要移除的会话 token
     */
    public void removeSession(String token) {
        sessions.remove(token);
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 如果找到则返回 User 对象，否则返回 null
     */
    public User getUser(String username) {
        return users.get(username);
    }
}
