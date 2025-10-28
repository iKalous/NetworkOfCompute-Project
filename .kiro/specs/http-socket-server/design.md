# Design Document

## Overview

本项目采用纯Java Socket API实现HTTP/1.1协议的客户端和服务器。系统分为三个主要模块：HTTP服务器、HTTP客户端和共享的HTTP协议处理组件。服务器采用多线程架构处理并发请求，支持长连接和多种MIME类型。客户端提供命令行和可选的GUI界面。

**技术栈：**
- JDK 17
- Java Socket API (java.net.Socket, java.net.ServerSocket)
- Java Swing (可选GUI)
- 多线程 (java.util.concurrent)

## Architecture

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      HTTP Client                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ CLI Interface│  │ GUI Interface│  │ HTTP Request │      │
│  │              │  │  (Optional)  │  │   Handler    │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         └──────────────────┴─────────────────┘              │
│                          │                                   │
└──────────────────────────┼───────────────────────────────────┘
                           │
                    Socket Connection
                           │
┌──────────────────────────┼───────────────────────────────────┐
│                          │         HTTP Server               │
│                   ┌──────▼───────┐                           │
│                   │ Server Socket │                          │
│                   │   Listener    │                          │
│                   └──────┬────────┘                          │
│                          │                                   │
│              ┌───────────┴───────────┐                       │
│              │   Thread Pool         │                       │
│              │  (Connection Handler) │                       │
│              └───────────┬───────────┘                       │
│                          │                                   │
│         ┌────────────────┼────────────────┐                 │
│         │                │                │                 │
│    ┌────▼─────┐   ┌─────▼──────┐  ┌──────▼──────┐          │
│    │ Request  │   │  Response  │  │   Router    │          │
│    │  Parser  │   │  Builder   │  │             │          │
│    └────┬─────┘   └─────▲──────┘  └──────┬──────┘          │
│         │               │                 │                 │
│         └───────────────┼─────────────────┘                 │
│                         │                                   │
│         ┌───────────────┴─────────────────┐                │
│         │                                  │                │
│    ┌────▼──────┐                    ┌─────▼──────┐         │
│    │  Static   │                    │    API     │         │
│    │  Resource │                    │  Handlers  │         │
│    │  Handler  │                    │            │         │
│    └───────────┘                    └─────┬──────┘         │
│                                            │                │
│                                     ┌──────▼──────┐         │
│                                     │    User     │         │
│                                     │  Registry   │         │
│                                     │  (Memory)   │         │
│                                     └─────────────┘         │
└─────────────────────────────────────────────────────────────┘
```

### 模块划分

**1. HTTP Protocol Module (共享)**
- HTTP消息解析和构建
- 状态码定义
- MIME类型映射

**2. HTTP Server Module**
- Socket监听和连接管理
- 请求路由
- 静态资源服务
- API端点处理
- 用户注册登录

**3. HTTP Client Module**
- 请求发送
- 响应接收和展示
- 重定向处理
- 用户界面（CLI/GUI）

## Components and Interfaces

### 1. HTTP Protocol Components

#### HttpRequest
```java
public class HttpRequest {
    private String method;           // GET, POST
    private String uri;              // /path/to/resource
    private String version;          // HTTP/1.1
    private Map<String, String> headers;
    private byte[] body;
    
    // 解析原始请求
    public static HttpRequest parse(InputStream input);
    
    // 构建请求字符串
    public byte[] toBytes();
}
```

#### HttpResponse
```java
public class HttpResponse {
    private int statusCode;          // 200, 404, etc.
    private String statusMessage;    // OK, Not Found, etc.
    private String version;          // HTTP/1.1
    private Map<String, String> headers;
    private byte[] body;
    
    // 解析原始响应
    public static HttpResponse parse(InputStream input);
    
    // 构建响应字符串
    public byte[] toBytes();
}
```

#### HttpStatus
```java
public enum HttpStatus {
    OK(200, "OK"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");
}
```

#### MimeType
```java
public class MimeType {
    private static final Map<String, String> MIME_TYPES = Map.of(
        ".html", "text/html",
        ".txt", "text/plain",
        ".json", "application/json",
        ".png", "image/png"
    );
    
    public static String getByExtension(String filename);
}
```

### 2. HTTP Server Components

#### HttpServer
```java
public class HttpServer {
    private final int port;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running;
    
    public void start();
    public void stop();
    private void handleConnection(Socket clientSocket);
}
```

#### ConnectionHandler (Runnable)
```java
public class ConnectionHandler implements Runnable {
    private final Socket socket;
    private final RequestRouter router;
    
    @Override
    public void run() {
        // 处理长连接循环
        // 读取请求 -> 路由 -> 生成响应 -> 发送
        // 检查Connection头决定是否保持连接
    }
}
```

#### RequestRouter
```java
public class RequestRouter {
    private final Map<String, RequestHandler> routes;
    private final StaticResourceHandler staticHandler;
    
    public HttpResponse route(HttpRequest request);
    public void registerHandler(String path, RequestHandler handler);
}
```

#### RequestHandler (Interface)
```java
public interface RequestHandler {
    HttpResponse handle(HttpRequest request);
}
```

#### StaticResourceHandler
```java
public class StaticResourceHandler implements RequestHandler {
    private final String rootDirectory;
    
    @Override
    public HttpResponse handle(HttpRequest request) {
        // 读取文件
        // 设置Content-Type
        // 返回200或404
    }
}
```

#### UserRegistry
```java
public class UserRegistry {
    private final ConcurrentHashMap<String, User> users;
    private final ConcurrentHashMap<String, Session> sessions;
    
    public synchronized boolean register(String username, String password);
    public String login(String username, String password);
    public boolean validateSession(String token);
}
```

#### API Handlers

**RegisterHandler**
```java
public class RegisterHandler implements RequestHandler {
    private final UserRegistry registry;
    
    @Override
    public HttpResponse handle(HttpRequest request) {
        // 解析JSON body
        // 验证输入
        // 调用registry.register()
        // 返回JSON响应
    }
}
```

**LoginHandler**
```java
public class LoginHandler implements RequestHandler {
    private final UserRegistry registry;
    
    @Override
    public HttpResponse handle(HttpRequest request) {
        // 解析JSON body
        // 调用registry.login()
        // 返回JSON响应（包含token）
    }
}
```

### 3. HTTP Client Components

#### HttpClient
```java
public class HttpClient {
    private static final int MAX_REDIRECTS = 5;
    
    public HttpResponse send(HttpRequest request);
    private HttpResponse sendWithRedirect(HttpRequest request, int redirectCount);
    private HttpResponse handleRedirect(HttpResponse response, int redirectCount);
}
```

#### ClientInterface (Interface)
```java
public interface ClientInterface {
    void start();
    void displayResponse(HttpResponse response);
}
```

#### CliClient
```java
public class CliClient implements ClientInterface {
    private final HttpClient client;
    private final Scanner scanner;
    
    @Override
    public void start() {
        // 命令行循环
        // 读取用户输入（URL、方法、头、body）
        // 发送请求
        // 显示响应
    }
}
```

#### GuiClient (Optional)
```java
public class GuiClient extends JFrame implements ClientInterface {
    private JTextField urlField;
    private JComboBox<String> methodBox;
    private JTextArea headersArea;
    private JTextArea bodyArea;
    private JTextArea responseArea;
    private final HttpClient client;
    
    @Override
    public void start() {
        // 初始化Swing组件
        // 设置布局
        // 添加事件监听
    }
}
```

## Data Models

### User
```java
public class User {
    private final String username;
    private final String passwordHash;  // 实际项目中应该加密
    private final LocalDateTime createdAt;
}
```

### Session
```java
public class Session {
    private final String token;
    private final String username;
    private final LocalDateTime createdAt;
    private LocalDateTime lastAccessTime;
}
```

### JSON Data Transfer Objects

**RegisterRequest / LoginRequest**
```json
{
    "username": "user123",
    "password": "password123"
}
```

**RegisterResponse / LoginResponse (Success)**
```json
{
    "success": true,
    "message": "Registration successful",
    "token": "session-token-here"  // 仅登录响应
}
```

**Error Response**
```json
{
    "success": false,
    "message": "Username already exists"
}
```

## Error Handling

### 服务器端错误处理

1. **请求解析错误**
   - 捕获格式错误的HTTP请求
   - 返回400 Bad Request

2. **资源未找到**
   - 静态文件不存在
   - 返回404 Not Found

3. **方法不支持**
   - 请求方法不是GET或POST
   - 返回405 Method Not Allowed

4. **内部错误**
   - 捕获所有未处理异常
   - 返回500 Internal Server Error
   - 记录错误日志

5. **线程池满**
   - 无法创建新线程处理请求
   - 返回503 Service Unavailable

6. **超时处理**
   - Socket读取超时（30秒）
   - 关闭连接

### 客户端错误处理

1. **连接失败**
   - 显示错误消息
   - 提示检查URL和网络

2. **超时**
   - Socket连接超时（10秒）
   - Socket读取超时（30秒）
   - 显示超时消息

3. **重定向循环**
   - 限制最大重定向次数为5
   - 显示重定向循环警告

4. **响应解析错误**
   - 捕获格式错误的响应
   - 显示原始响应内容

## Testing Strategy

### 单元测试

1. **HTTP协议组件测试**
   - HttpRequest解析和构建
   - HttpResponse解析和构建
   - MimeType映射

2. **服务器组件测试**
   - RequestRouter路由逻辑
   - UserRegistry注册登录逻辑
   - StaticResourceHandler文件读取

3. **客户端组件测试**
   - HttpClient请求发送
   - 重定向处理逻辑

### 集成测试

1. **服务器集成测试**
   - 启动服务器
   - 使用Socket发送原始HTTP请求
   - 验证响应状态码和内容

2. **客户端-服务器集成测试**
   - 启动服务器
   - 使用客户端发送请求
   - 验证完整流程

### 功能测试场景

1. **基本GET请求**
   - 请求静态HTML文件
   - 验证200响应和正确的Content-Type

2. **POST注册登录**
   - 注册新用户
   - 登录获取token
   - 验证JSON响应

3. **重定向处理**
   - 配置301/302重定向
   - 验证客户端自动跟随

4. **长连接测试**
   - 发送多个请求在同一连接
   - 验证Connection: keep-alive

5. **并发测试**
   - 多个客户端同时连接
   - 验证服务器正确处理

6. **错误场景测试**
   - 404错误
   - 405错误
   - 400错误（格式错误的请求）

### 测试工具

- JUnit 5 用于单元测试
- 手动测试使用Postman或curl
- 浏览器测试静态资源访问

## Implementation Notes

### 长连接实现要点

1. 检查请求头 `Connection: keep-alive`
2. 在响应中添加 `Connection: keep-alive`
3. 不关闭Socket，继续读取下一个请求
4. 设置Socket超时（30秒）
5. 收到 `Connection: close` 或超时后关闭连接

### 线程安全

1. UserRegistry使用ConcurrentHashMap
2. 注册操作使用synchronized确保原子性
3. 每个连接独立线程，避免共享状态

### 性能考虑

1. 使用线程池（固定大小，如10-50线程）
2. 静态资源缓存（可选优化）
3. 合理的超时设置避免资源泄漏

### 项目结构

```
src/
├── main/
│   └── java/
│       └── com/
│           └── http/
│               ├── protocol/
│               │   ├── HttpRequest.java
│               │   ├── HttpResponse.java
│               │   ├── HttpStatus.java
│               │   └── MimeType.java
│               ├── server/
│               │   ├── HttpServer.java
│               │   ├── ConnectionHandler.java
│               │   ├── RequestRouter.java
│               │   ├── RequestHandler.java
│               │   ├── StaticResourceHandler.java
│               │   ├── UserRegistry.java
│               │   ├── User.java
│               │   ├── Session.java
│               │   ├── RegisterHandler.java
│               │   └── LoginHandler.java
│               ├── client/
│               │   ├── HttpClient.java
│               │   ├── ClientInterface.java
│               │   ├── CliClient.java
│               │   └── GuiClient.java
│               └── util/
│                   ├── JsonParser.java (简单JSON解析)
│                   └── Logger.java
└── resources/
    └── static/
        ├── index.html
        ├── logo.png
        └── data.json
```
