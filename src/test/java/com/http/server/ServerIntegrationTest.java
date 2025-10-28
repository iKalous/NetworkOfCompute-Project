package com.http.server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ServerIntegrationTest - 服务器集成测试
 * 测试完整的HTTP服务器功能，包括静态资源、API端点和错误处理
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServerIntegrationTest {
    
    private static HttpServer server;
    private static final int TEST_PORT = 8888;
    private static final String TEST_HOST = "localhost";
    private static final String STATIC_RESOURCES_PATH = "src/main/resources/static";
    
    @BeforeAll
    static void startServer() throws Exception {
        // 创建UserRegistry实例
        UserRegistry userRegistry = new UserRegistry();
        
        // 创建RequestRouter并注册所有handler
        RequestRouter router = new RequestRouter();
        router.registerHandler("/api/register", new RegisterHandler(userRegistry));
        router.registerHandler("/api/login", new LoginHandler(userRegistry));
        
        // 设置StaticResourceHandler为默认handler
        StaticResourceHandler staticHandler = new StaticResourceHandler(STATIC_RESOURCES_PATH);
        router.setDefaultHandler(staticHandler);
        
        // 创建并启动服务器
        server = new HttpServer(TEST_PORT, router);
        server.start();
        
        // 等待服务器启动
        Thread.sleep(500);
    }
    
    @AfterAll
    static void stopServer() {
        if (server != null) {
            server.stop();
        }
    }
    
    /**
     * 发送原始HTTP请求并接收响应
     */
    private String sendRawRequest(String request) throws IOException {
        try (Socket socket = new Socket(TEST_HOST, TEST_PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            
            // 发送请求
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();
            
            // 读取响应
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            // 设置超时避免阻塞
            socket.setSoTimeout(2000);
            
            try {
                while ((bytesRead = in.read(buffer)) != -1) {
                    response.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // 超时或连接关闭，这是正常的
            }
            
            return response.toString(StandardCharsets.UTF_8);
        }
    }
    
    @Test
    @Order(1)
    @DisplayName("测试GET静态资源返回200")
    void testGetStaticResource() throws IOException {
        String request = "GET /index.html HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        // 验证响应包含200状态码
        assertTrue(response.contains("HTTP/1.1 200 OK"), 
                  "Response should contain 200 OK status");
        assertTrue(response.contains("Content-Type: text/html"), 
                  "Response should contain text/html content type");
    }
    
    @Test
    @Order(2)
    @DisplayName("测试POST注册功能")
    void testPostRegister() throws IOException {
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"password123\"}";
        String request = "POST /api/register HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: " + jsonBody.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        jsonBody;
        
        String response = sendRawRequest(request);
        
        // 验证响应包含200状态码
        assertTrue(response.contains("HTTP/1.1 200 OK"), 
                  "Registration should return 200 OK");
        assertTrue(response.contains("\"success\":true"), 
                  "Response should indicate success");
        assertTrue(response.contains("Registration successful"), 
                  "Response should contain success message");
    }
    
    @Test
    @Order(3)
    @DisplayName("测试POST登录功能")
    void testPostLogin() throws IOException {
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"password123\"}";
        String request = "POST /api/login HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: " + jsonBody.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        jsonBody;
        
        String response = sendRawRequest(request);
        
        // 验证响应包含200状态码和token
        assertTrue(response.contains("HTTP/1.1 200 OK"), 
                  "Login should return 200 OK");
        assertTrue(response.contains("\"success\":true"), 
                  "Response should indicate success");
        assertTrue(response.contains("\"token\":"), 
                  "Response should contain token");
    }
    
    @Test
    @Order(4)
    @DisplayName("测试404错误 - 资源不存在")
    void testNotFoundError() throws IOException {
        String request = "GET /nonexistent.html HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        // 验证响应包含404状态码
        assertTrue(response.contains("HTTP/1.1 404 Not Found"), 
                  "Response should contain 404 Not Found status");
    }
    
    @Test
    @Order(5)
    @DisplayName("测试405错误 - 方法不支持")
    void testMethodNotAllowedError() throws IOException {
        String request = "DELETE /api/register HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        // 验证响应包含405状态码
        assertTrue(response.contains("HTTP/1.1 405 Method Not Allowed"), 
                  "Response should contain 405 Method Not Allowed status");
        assertTrue(response.contains("Allow: GET, POST"), 
                  "Response should contain Allow header");
    }
    
    @Test
    @Order(6)
    @DisplayName("测试400错误 - 注册时用户名已存在")
    void testBadRequestDuplicateUser() throws IOException {
        // 尝试注册已存在的用户
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"password123\"}";
        String request = "POST /api/register HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: " + jsonBody.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        jsonBody;
        
        String response = sendRawRequest(request);
        
        // 验证响应包含400状态码
        assertTrue(response.contains("HTTP/1.1 400 Bad Request"), 
                  "Duplicate registration should return 400 Bad Request");
        assertTrue(response.contains("\"success\":false"), 
                  "Response should indicate failure");
        assertTrue(response.contains("already exists"), 
                  "Response should mention username already exists");
    }
    
    @Test
    @Order(7)
    @DisplayName("测试401错误 - 登录失败")
    void testUnauthorizedLogin() throws IOException {
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"wrongpassword\"}";
        String request = "POST /api/login HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: application/json\r\n" +
                        "Content-Length: " + jsonBody.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        jsonBody;
        
        String response = sendRawRequest(request);
        
        // 验证响应包含401状态码
        assertTrue(response.contains("HTTP/1.1 401 Unauthorized"), 
                  "Wrong password should return 401 Unauthorized");
        assertTrue(response.contains("\"success\":false"), 
                  "Response should indicate failure");
    }
    
    @Test
    @Order(8)
    @DisplayName("测试长连接 - 多个请求")
    void testPersistentConnection() throws IOException {
        try (Socket socket = new Socket(TEST_HOST, TEST_PORT);
             OutputStream out = socket.getOutputStream();
             InputStream in = socket.getInputStream()) {
            
            socket.setSoTimeout(5000);
            
            // 第一个请求 - 使用keep-alive
            String request1 = "GET /index.html HTTP/1.1\r\n" +
                            "Host: localhost\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n";
            
            out.write(request1.getBytes(StandardCharsets.UTF_8));
            out.flush();
            
            // 读取第一个响应
            String response1 = readResponse(in);
            assertTrue(response1.contains("HTTP/1.1 200 OK"), 
                      "First request should return 200 OK");
            assertTrue(response1.contains("Connection: keep-alive"), 
                      "Response should indicate keep-alive");
            
            // 第二个请求 - 在同一连接上
            String request2 = "GET /index.html HTTP/1.1\r\n" +
                            "Host: localhost\r\n" +
                            "Connection: close\r\n" +
                            "\r\n";
            
            out.write(request2.getBytes(StandardCharsets.UTF_8));
            out.flush();
            
            // 读取第二个响应
            String response2 = readResponse(in);
            assertTrue(response2.contains("HTTP/1.1 200 OK"), 
                      "Second request should return 200 OK");
        }
    }
    
    /**
     * 读取HTTP响应直到遇到Content-Length指定的长度
     */
    private String readResponse(InputStream in) throws IOException {
        ByteArrayOutputStream response = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        
        // 读取响应头
        StringBuilder headers = new StringBuilder();
        int b;
        int consecutiveCRLF = 0;
        
        while ((b = in.read()) != -1) {
            response.write(b);
            headers.append((char) b);
            
            // 检测\r\n\r\n（响应头结束）
            if (b == '\r' || b == '\n') {
                consecutiveCRLF++;
                if (consecutiveCRLF == 4) {
                    break;
                }
            } else {
                consecutiveCRLF = 0;
            }
        }
        
        // 解析Content-Length
        String headerStr = headers.toString();
        int contentLength = 0;
        String[] lines = headerStr.split("\r\n");
        for (String line : lines) {
            if (line.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(line.substring(15).trim());
                break;
            }
        }
        
        // 读取响应体
        if (contentLength > 0) {
            byte[] body = new byte[contentLength];
            int totalRead = 0;
            while (totalRead < contentLength) {
                bytesRead = in.read(body, totalRead, contentLength - totalRead);
                if (bytesRead == -1) break;
                totalRead += bytesRead;
            }
            response.write(body, 0, totalRead);
        }
        
        return response.toString(StandardCharsets.UTF_8);
    }
}
