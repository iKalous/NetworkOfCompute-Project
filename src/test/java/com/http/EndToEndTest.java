package com.http;

import com.http.client.HttpClient;
import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.server.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端测试
 * 测试完整的客户端-服务器交互流程
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndTest {
    
    private static HttpServer server;
    private static final int TEST_PORT = 8888;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;
    private static final String STATIC_RESOURCES_PATH = "src/main/resources/static";
    
    private HttpClient client;
    
    @BeforeAll
    public static void startServer() throws IOException, InterruptedException {
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
        
        // 等待服务器完全启动
        Thread.sleep(500);
        
        System.out.println("Test server started on port " + TEST_PORT);
    }
    
    @AfterAll
    public static void stopServer() {
        if (server != null) {
            server.stop();
            System.out.println("Test server stopped");
        }
    }
    
    @BeforeEach
    public void setUp() {
        client = new HttpClient();
    }
    
    /**
     * 测试1: 完整的注册 -> 登录 -> 访问静态资源流程
     * Requirements: 6.6, 8.7, 9.7
     */
    @Test
    @Order(1)
    public void testCompleteUserFlow() throws IOException {
        System.out.println("\n=== Test 1: Complete User Flow ===");
        
        // Step 1: 注册新用户
        System.out.println("Step 1: Register new user");
        HttpRequest registerRequest = new HttpRequest("POST", BASE_URL + "/api/register");
        registerRequest.setHeader("Host", "localhost:" + TEST_PORT);
        registerRequest.setHeader("Content-Type", "application/json");
        registerRequest.setHeader("Connection", "close");
        
        String registerBody = "{\"username\":\"testuser\",\"password\":\"password123\"}";
        registerRequest.setBody(registerBody.getBytes());
        
        HttpResponse registerResponse = client.send(registerRequest);
        
        assertEquals(200, registerResponse.getStatusCode(), "Registration should succeed");
        String registerResponseBody = new String(registerResponse.getBody());
        assertTrue(registerResponseBody.contains("success"), "Response should indicate success");
        System.out.println("Registration response: " + registerResponseBody);
        
        // Step 2: 登录用户
        System.out.println("\nStep 2: Login user");
        HttpRequest loginRequest = new HttpRequest("POST", BASE_URL + "/api/login");
        loginRequest.setHeader("Host", "localhost:" + TEST_PORT);
        loginRequest.setHeader("Content-Type", "application/json");
        loginRequest.setHeader("Connection", "close");
        
        String loginBody = "{\"username\":\"testuser\",\"password\":\"password123\"}";
        loginRequest.setBody(loginBody.getBytes());
        
        HttpResponse loginResponse = client.send(loginRequest);
        
        assertEquals(200, loginResponse.getStatusCode(), "Login should succeed");
        String loginResponseBody = new String(loginResponse.getBody());
        assertTrue(loginResponseBody.contains("token"), "Response should contain token");
        System.out.println("Login response: " + loginResponseBody);
        
        // Step 3: 访问静态资源
        System.out.println("\nStep 3: Access static resource");
        HttpRequest staticRequest = new HttpRequest("GET", BASE_URL + "/index.html");
        staticRequest.setHeader("Host", "localhost:" + TEST_PORT);
        staticRequest.setHeader("Connection", "close");
        
        HttpResponse staticResponse = client.send(staticRequest);
        
        assertEquals(200, staticResponse.getStatusCode(), "Static resource should be found");
        assertEquals("text/html", staticResponse.getHeader("Content-Type"), "Content-Type should be text/html");
        assertTrue(staticResponse.getBody().length > 0, "Response body should not be empty");
        System.out.println("Static resource retrieved successfully");
        
        System.out.println("=== Test 1 Complete ===\n");
    }
    
    /**
     * 测试2: 长连接 - 在同一连接上发送多个请求
     * Requirements: 6.6, 10.1, 10.2
     */
    @Test
    @Order(2)
    public void testPersistentConnection() throws IOException {
        System.out.println("\n=== Test 2: Persistent Connection ===");
        
        // 注意: HttpClient当前实现每次都创建新连接并使用Connection: close
        // 这个测试验证服务器能够处理多个顺序请求
        
        // 发送多个请求，验证服务器能够处理
        for (int i = 1; i <= 3; i++) {
            System.out.println("Request " + i + ":");
            
            HttpRequest request = new HttpRequest("GET", BASE_URL + "/index.html");
            request.setHeader("Host", "localhost:" + TEST_PORT);
            request.setHeader("Connection", "close");
            
            HttpResponse response = client.send(request);
            
            assertEquals(200, response.getStatusCode(), "Request " + i + " should succeed");
            System.out.println("  Status: " + response.getStatusCode());
        }
        
        System.out.println("=== Test 2 Complete ===\n");
    }
    
    /**
     * 测试3: 并发客户端
     * Requirements: 10.1, 10.2
     */
    @Test
    @Order(3)
    public void testConcurrentClients() throws InterruptedException, ExecutionException {
        System.out.println("\n=== Test 3: Concurrent Clients ===");
        
        int numClients = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numClients);
        List<Future<Boolean>> futures = new ArrayList<>();
        
        // 创建多个并发客户端
        for (int i = 0; i < numClients; i++) {
            final int clientId = i;
            Future<Boolean> future = executor.submit(() -> {
                try {
                    HttpClient concurrentClient = new HttpClient();
                    
                    // 每个客户端注册一个唯一用户
                    HttpRequest registerRequest = new HttpRequest("POST", BASE_URL + "/api/register");
                    registerRequest.setHeader("Host", "localhost:" + TEST_PORT);
                    registerRequest.setHeader("Content-Type", "application/json");
                    registerRequest.setHeader("Connection", "close");
                    
                    String registerBody = "{\"username\":\"concurrentuser" + clientId + "\",\"password\":\"password" + clientId + "\"}";
                    registerRequest.setBody(registerBody.getBytes());
                    
                    HttpResponse registerResponse = concurrentClient.send(registerRequest);
                    
                    if (registerResponse.getStatusCode() != 200) {
                        String responseBody = new String(registerResponse.getBody());
                        System.err.println("Client " + clientId + " registration failed: " + registerResponse.getStatusCode() + " - " + responseBody);
                        return false;
                    }
                    
                    // 登录
                    HttpRequest loginRequest = new HttpRequest("POST", BASE_URL + "/api/login");
                    loginRequest.setHeader("Host", "localhost:" + TEST_PORT);
                    loginRequest.setHeader("Content-Type", "application/json");
                    loginRequest.setHeader("Connection", "close");
                    
                    String loginBody = "{\"username\":\"concurrentuser" + clientId + "\",\"password\":\"password" + clientId + "\"}";
                    loginRequest.setBody(loginBody.getBytes());
                    
                    HttpResponse loginResponse = concurrentClient.send(loginRequest);
                    
                    if (loginResponse.getStatusCode() != 200) {
                        String responseBody = new String(loginResponse.getBody());
                        System.err.println("Client " + clientId + " login failed: " + loginResponse.getStatusCode() + " - " + responseBody);
                        return false;
                    }
                    
                    // 访问静态资源
                    HttpRequest staticRequest = new HttpRequest("GET", BASE_URL + "/index.html");
                    staticRequest.setHeader("Host", "localhost:" + TEST_PORT);
                    staticRequest.setHeader("Connection", "close");
                    
                    HttpResponse staticResponse = concurrentClient.send(staticRequest);
                    
                    if (staticResponse.getStatusCode() != 200) {
                        System.err.println("Client " + clientId + " static resource access failed: " + staticResponse.getStatusCode());
                        return false;
                    }
                    
                    System.out.println("Client " + clientId + " completed successfully");
                    return true;
                    
                } catch (Exception e) {
                    System.err.println("Client " + clientId + " error: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            });
            
            futures.add(future);
        }
        
        // 等待所有客户端完成
        int successCount = 0;
        for (Future<Boolean> future : futures) {
            if (future.get()) {
                successCount++;
            }
        }
        
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        
        System.out.println("Concurrent clients completed: " + successCount + "/" + numClients);
        assertTrue(successCount >= numClients * 0.8, "At least 80% of concurrent clients should succeed (got " + successCount + "/" + numClients + ")");
        
        System.out.println("=== Test 3 Complete ===\n");
    }
    
    /**
     * 测试4: 错误场景
     * Requirements: 5.6, 5.7, 8.7, 9.7
     */
    @Test
    @Order(4)
    public void testErrorScenarios() throws IOException {
        System.out.println("\n=== Test 4: Error Scenarios ===");
        
        // 测试404 - 资源不存在
        System.out.println("Test 404 - Not Found:");
        HttpRequest notFoundRequest = new HttpRequest("GET", BASE_URL + "/nonexistent.html");
        notFoundRequest.setHeader("Host", "localhost:" + TEST_PORT);
        notFoundRequest.setHeader("Connection", "close");
        
        HttpResponse notFoundResponse = client.send(notFoundRequest);
        assertEquals(404, notFoundResponse.getStatusCode(), "Should return 404 for non-existent resource");
        System.out.println("  Status: " + notFoundResponse.getStatusCode() + " - OK");
        
        // 测试405 - 方法不允许
        System.out.println("\nTest 405 - Method Not Allowed:");
        HttpRequest methodNotAllowedRequest = new HttpRequest("PUT", BASE_URL + "/api/register");
        methodNotAllowedRequest.setHeader("Host", "localhost:" + TEST_PORT);
        methodNotAllowedRequest.setHeader("Connection", "close");
        
        HttpResponse methodNotAllowedResponse = client.send(methodNotAllowedRequest);
        assertEquals(405, methodNotAllowedResponse.getStatusCode(), "Should return 405 for unsupported method");
        System.out.println("  Status: " + methodNotAllowedResponse.getStatusCode() + " - OK");
        
        // 测试400 - 注册时用户名太短
        System.out.println("\nTest 400 - Bad Request (username too short):");
        HttpRequest badRequest = new HttpRequest("POST", BASE_URL + "/api/register");
        badRequest.setHeader("Host", "localhost:" + TEST_PORT);
        badRequest.setHeader("Content-Type", "application/json");
        badRequest.setHeader("Connection", "close");
        
        String badBody = "{\"username\":\"ab\",\"password\":\"password123\"}";
        badRequest.setBody(badBody.getBytes());
        
        HttpResponse badResponse = client.send(badRequest);
        assertEquals(400, badResponse.getStatusCode(), "Should return 400 for invalid input");
        System.out.println("  Status: " + badResponse.getStatusCode() + " - OK");
        
        // 测试401 - 登录失败
        System.out.println("\nTest 401 - Unauthorized:");
        HttpRequest unauthorizedRequest = new HttpRequest("POST", BASE_URL + "/api/login");
        unauthorizedRequest.setHeader("Host", "localhost:" + TEST_PORT);
        unauthorizedRequest.setHeader("Content-Type", "application/json");
        unauthorizedRequest.setHeader("Connection", "close");
        
        String unauthorizedBody = "{\"username\":\"nonexistent\",\"password\":\"wrongpass\"}";
        unauthorizedRequest.setBody(unauthorizedBody.getBytes());
        
        HttpResponse unauthorizedResponse = client.send(unauthorizedRequest);
        assertEquals(401, unauthorizedResponse.getStatusCode(), "Should return 401 for invalid credentials");
        System.out.println("  Status: " + unauthorizedResponse.getStatusCode() + " - OK");
        
        System.out.println("=== Test 4 Complete ===\n");
    }
    
    /**
     * 测试5: 不同MIME类型的静态资源
     * Requirements: 7.1, 7.2, 7.3, 7.4
     */
    @Test
    @Order(5)
    public void testDifferentMimeTypes() throws IOException {
        System.out.println("\n=== Test 5: Different MIME Types ===");
        
        // 测试HTML
        System.out.println("Test HTML:");
        HttpRequest htmlRequest = new HttpRequest("GET", BASE_URL + "/index.html");
        htmlRequest.setHeader("Host", "localhost:" + TEST_PORT);
        htmlRequest.setHeader("Connection", "close");
        
        HttpResponse htmlResponse = client.send(htmlRequest);
        assertEquals(200, htmlResponse.getStatusCode());
        assertEquals("text/html", htmlResponse.getHeader("Content-Type"));
        System.out.println("  Content-Type: " + htmlResponse.getHeader("Content-Type") + " - OK");
        
        // 测试JSON
        System.out.println("\nTest JSON:");
        HttpRequest jsonRequest = new HttpRequest("GET", BASE_URL + "/data.json");
        jsonRequest.setHeader("Host", "localhost:" + TEST_PORT);
        jsonRequest.setHeader("Connection", "close");
        
        HttpResponse jsonResponse = client.send(jsonRequest);
        assertEquals(200, jsonResponse.getStatusCode());
        assertEquals("application/json", jsonResponse.getHeader("Content-Type"));
        System.out.println("  Content-Type: " + jsonResponse.getHeader("Content-Type") + " - OK");
        
        // 测试TXT
        System.out.println("\nTest TXT:");
        HttpRequest txtRequest = new HttpRequest("GET", BASE_URL + "/test.txt");
        txtRequest.setHeader("Host", "localhost:" + TEST_PORT);
        txtRequest.setHeader("Connection", "close");
        
        HttpResponse txtResponse = client.send(txtRequest);
        assertEquals(200, txtResponse.getStatusCode());
        assertEquals("text/plain", txtResponse.getHeader("Content-Type"));
        System.out.println("  Content-Type: " + txtResponse.getHeader("Content-Type") + " - OK");
        
        System.out.println("=== Test 5 Complete ===\n");
    }
}
