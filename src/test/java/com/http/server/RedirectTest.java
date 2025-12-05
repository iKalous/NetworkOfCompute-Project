package com.http.server;

import org.junit.jupiter.api.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RedirectTest - 重定向状态码测试
 * 测试HTTP 3xx重定向状态码：301、302、304
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedirectTest {
    
    private static HttpServer server;
    private static final int TEST_PORT = 8889;
    private static final String TEST_HOST = "localhost";
    private static final String STATIC_RESOURCES_PATH = "src/main/resources/static";
    
    // 用于304测试的ETag和Last-Modified值
    private static final String TEST_ETAG = "\"abc123\"";
    private static final String TEST_LAST_MODIFIED = "Wed, 21 Oct 2024 07:28:00 GMT";
    
    @BeforeAll
    static void startServer() throws Exception {
        RequestRouter router = new RequestRouter();
        
        // 注册301永久重定向处理器
        router.registerHandler("/old-page", 
            RedirectHandler.movedPermanently("/new-page"));
        
        // 注册302临时重定向处理器
        router.registerHandler("/temp-redirect", 
            RedirectHandler.found("/temporary-location"));
        
        // 注册带完整URL的重定向
        router.registerHandler("/external-redirect", 
            RedirectHandler.found("https://example.com/target"));
        
        // 注册304 Not Modified处理器
        StaticResourceHandler staticHandler = new StaticResourceHandler(STATIC_RESOURCES_PATH);
        router.registerHandler("/cached-resource", 
            new NotModifiedHandler(TEST_ETAG, TEST_LAST_MODIFIED, staticHandler));
        
        // 设置默认处理器
        router.setDefaultHandler(staticHandler);
        
        server = new HttpServer(TEST_PORT, router);
        server.start();
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
            
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();
            
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            
            socket.setSoTimeout(2000);
            
            try {
                while ((bytesRead = in.read(buffer)) != -1) {
                    response.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                // 超时或连接关闭
            }
            
            return response.toString(StandardCharsets.UTF_8);
        }
    }
    
    // ==================== 301 永久重定向测试 ====================
    
    @Test
    @Order(1)
    @DisplayName("测试301永久重定向 - 基本功能")
    void test301MovedPermanently() throws IOException {
        String request = "GET /old-page HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 301 Moved Permanently"), 
                  "Response should contain 301 Moved Permanently status");
        assertTrue(response.contains("Location: /new-page"), 
                  "Response should contain Location header pointing to new page");
    }
    
    @Test
    @Order(2)
    @DisplayName("测试301永久重定向 - 响应体内容")
    void test301ResponseBody() throws IOException {
        String request = "GET /old-page HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("301 Moved Permanently"), 
                  "Response body should contain redirect message");
        assertTrue(response.contains("Content-Type: text/plain"), 
                  "Response should have text/plain content type");
    }
    
    // ==================== 302 临时重定向测试 ====================
    
    @Test
    @Order(3)
    @DisplayName("测试302临时重定向 - 基本功能")
    void test302Found() throws IOException {
        String request = "GET /temp-redirect HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 302 Found"), 
                  "Response should contain 302 Found status");
        assertTrue(response.contains("Location: /temporary-location"), 
                  "Response should contain Location header");
    }
    
    @Test
    @Order(4)
    @DisplayName("测试302重定向到外部URL")
    void test302ExternalRedirect() throws IOException {
        String request = "GET /external-redirect HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 302 Found"), 
                  "Response should contain 302 Found status");
        assertTrue(response.contains("Location: https://example.com/target"), 
                  "Response should contain external URL in Location header");
    }
    
    @Test
    @Order(5)
    @DisplayName("测试302临时重定向 - POST请求")
    void test302WithPostRequest() throws IOException {
        String body = "test=data";
        String request = "POST /temp-redirect HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Content-Type: application/x-www-form-urlencoded\r\n" +
                        "Content-Length: " + body.length() + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n" +
                        body;
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 302 Found"), 
                  "POST request should also receive 302 redirect");
        assertTrue(response.contains("Location: /temporary-location"), 
                  "Response should contain Location header");
    }
    
    // ==================== 304 Not Modified测试 ====================
    
    @Test
    @Order(6)
    @DisplayName("测试304 Not Modified - If-None-Match匹配")
    void test304WithIfNoneMatch() throws IOException {
        String request = "GET /cached-resource HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "If-None-Match: " + TEST_ETAG + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 304 Not Modified"), 
                  "Response should contain 304 Not Modified when ETag matches");
        assertTrue(response.contains("ETag: " + TEST_ETAG), 
                  "Response should include ETag header");
    }
    
    @Test
    @Order(7)
    @DisplayName("测试304 Not Modified - If-Modified-Since匹配")
    void test304WithIfModifiedSince() throws IOException {
        String request = "GET /cached-resource HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "If-Modified-Since: " + TEST_LAST_MODIFIED + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 304 Not Modified"), 
                  "Response should contain 304 Not Modified when Last-Modified matches");
        assertTrue(response.contains("Last-Modified: " + TEST_LAST_MODIFIED), 
                  "Response should include Last-Modified header");
    }
    
    @Test
    @Order(8)
    @DisplayName("测试304 - ETag不匹配时返回200")
    void test200WhenETagNotMatch() throws IOException {
        String request = "GET /cached-resource HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "If-None-Match: \"different-etag\"\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        // 当ETag不匹配时，应该返回实际内容（可能是200或404取决于资源是否存在）
        assertFalse(response.contains("HTTP/1.1 304 Not Modified"), 
                   "Response should NOT be 304 when ETag doesn't match");
    }
    
    @Test
    @Order(9)
    @DisplayName("测试304 - 无条件请求头时返回实际内容")
    void test200WithoutConditionalHeaders() throws IOException {
        String request = "GET /cached-resource HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        // 没有条件请求头时，应该返回实际内容
        assertFalse(response.contains("HTTP/1.1 304 Not Modified"), 
                   "Response should NOT be 304 without conditional headers");
        assertTrue(response.contains("ETag: " + TEST_ETAG), 
                  "Response should include ETag header for caching");
        assertTrue(response.contains("Last-Modified: " + TEST_LAST_MODIFIED), 
                  "Response should include Last-Modified header for caching");
    }
    
    @Test
    @Order(10)
    @DisplayName("测试304响应不包含响应体")
    void test304NoResponseBody() throws IOException {
        String request = "GET /cached-resource HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "If-None-Match: " + TEST_ETAG + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 304 Not Modified"), 
                  "Response should be 304");
        
        // 304响应在头部结束后不应有实质性内容
        int headerEnd = response.indexOf("\r\n\r\n");
        if (headerEnd != -1) {
            String bodyPart = response.substring(headerEnd + 4);
            assertTrue(bodyPart.isEmpty() || bodyPart.trim().isEmpty(), 
                      "304 response should not have a response body");
        }
    }
    
    // ==================== 重定向通用测试 ====================
    
    @Test
    @Order(11)
    @DisplayName("测试重定向响应包含必要的头部")
    void testRedirectHeaders() throws IOException {
        String request = "GET /old-page HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("Location:"), 
                  "Redirect response must contain Location header");
        assertTrue(response.contains("Content-Length:"), 
                  "Response should contain Content-Length header");
    }
    
    @Test
    @Order(12)
    @DisplayName("测试不存在的路径返回404而非重定向")
    void testNonExistentPathReturns404() throws IOException {
        String request = "GET /non-existent-path HTTP/1.1\r\n" +
                        "Host: localhost\r\n" +
                        "Connection: close\r\n" +
                        "\r\n";
        
        String response = sendRawRequest(request);
        
        assertTrue(response.contains("HTTP/1.1 404 Not Found"), 
                  "Non-existent path should return 404, not redirect");
        assertFalse(response.contains("Location:"), 
                   "404 response should not contain Location header");
    }
}
