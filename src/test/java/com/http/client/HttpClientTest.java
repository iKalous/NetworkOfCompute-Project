package com.http.client;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpClient单元测试
 */
class HttpClientTest {
    
    private ServerSocket testServer;
    private int testPort;
    private Thread serverThread;
    private volatile boolean serverRunning;
    
    @BeforeEach
    void setUp() throws IOException {
        // 创建测试服务器
        testServer = new ServerSocket(0); // 使用随机端口
        testPort = testServer.getLocalPort();
        serverRunning = true;
    }
    
    @AfterEach
    void tearDown() throws IOException {
        serverRunning = false;
        if (testServer != null && !testServer.isClosed()) {
            testServer.close();
        }
        if (serverThread != null && serverThread.isAlive()) {
            serverThread.interrupt();
            try {
                serverThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    @Test
    void testBasicGetRequest() throws Exception {
        // 准备测试响应
        String responseBody = "Hello, World!";
        CountDownLatch latch = new CountDownLatch(1);
        
        // 启动测试服务器
        serverThread = new Thread(() -> {
            try {
                Socket client = testServer.accept();
                latch.countDown();
                
                // 读取请求（简单读取，不解析）
                client.getInputStream().read(new byte[1024]);
                
                // 发送响应
                HttpResponse response = new HttpResponse(HttpStatus.OK);
                response.setHeader("Content-Type", "text/plain");
                response.setBody(responseBody);
                
                OutputStream out = client.getOutputStream();
                out.write(response.toBytes());
                out.flush();
                
                client.close();
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        
        // 发送请求
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("GET", "http://localhost:" + testPort + "/test");
        request.setHeader("User-Agent", "TestClient/1.0");
        
        HttpResponse response = client.send(request);
        
        // 验证响应
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody, response.getBodyAsString());
        
        // 等待服务器线程完成
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testPostRequest() throws Exception {
        String requestBody = "{\"username\":\"test\",\"password\":\"pass123\"}";
        String responseBody = "{\"success\":true}";
        CountDownLatch latch = new CountDownLatch(1);
        
        serverThread = new Thread(() -> {
            try {
                Socket client = testServer.accept();
                latch.countDown();
                
                // 读取请求
                client.getInputStream().read(new byte[2048]);
                
                // 发送响应
                HttpResponse response = new HttpResponse(HttpStatus.OK);
                response.setHeader("Content-Type", "application/json");
                response.setBody(responseBody);
                
                OutputStream out = client.getOutputStream();
                out.write(response.toBytes());
                out.flush();
                
                client.close();
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        
        // 发送POST请求
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("POST", "http://localhost:" + testPort + "/api/register");
        request.setHeader("Content-Type", "application/json");
        request.setBody(requestBody);
        
        HttpResponse response = client.send(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals(responseBody, response.getBodyAsString());
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testRedirect301() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        
        serverThread = new Thread(() -> {
            try {
                // 第一个请求 - 返回301重定向
                Socket client1 = testServer.accept();
                latch.countDown();
                
                client1.getInputStream().read(new byte[1024]);
                
                HttpResponse redirect = new HttpResponse(HttpStatus.MOVED_PERMANENTLY);
                redirect.setHeader("Location", "http://localhost:" + testPort + "/new-location");
                
                OutputStream out1 = client1.getOutputStream();
                out1.write(redirect.toBytes());
                out1.flush();
                client1.close();
                
                // 第二个请求 - 返回200
                Socket client2 = testServer.accept();
                latch.countDown();
                
                client2.getInputStream().read(new byte[1024]);
                
                HttpResponse finalResponse = new HttpResponse(HttpStatus.OK);
                finalResponse.setBody("Redirected content");
                
                OutputStream out2 = client2.getOutputStream();
                out2.write(finalResponse.toBytes());
                out2.flush();
                client2.close();
                
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        
        // 发送请求
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("GET", "http://localhost:" + testPort + "/old-location");
        
        HttpResponse response = client.send(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Redirected content", response.getBodyAsString());
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testRedirect302() throws Exception {
        CountDownLatch latch = new CountDownLatch(2);
        
        serverThread = new Thread(() -> {
            try {
                // 第一个请求 - 返回302重定向
                Socket client1 = testServer.accept();
                latch.countDown();
                
                client1.getInputStream().read(new byte[1024]);
                
                HttpResponse redirect = new HttpResponse(HttpStatus.FOUND);
                redirect.setHeader("Location", "http://localhost:" + testPort + "/temp-location");
                
                OutputStream out1 = client1.getOutputStream();
                out1.write(redirect.toBytes());
                out1.flush();
                client1.close();
                
                // 第二个请求 - 返回200
                Socket client2 = testServer.accept();
                latch.countDown();
                
                client2.getInputStream().read(new byte[1024]);
                
                HttpResponse finalResponse = new HttpResponse(HttpStatus.OK);
                finalResponse.setBody("Temporary redirect content");
                
                OutputStream out2 = client2.getOutputStream();
                out2.write(finalResponse.toBytes());
                out2.flush();
                client2.close();
                
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        
        // 发送请求
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("GET", "http://localhost:" + testPort + "/original");
        
        HttpResponse response = client.send(request);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertEquals("Temporary redirect content", response.getBodyAsString());
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testNotModified304() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        serverThread = new Thread(() -> {
            try {
                Socket client = testServer.accept();
                latch.countDown();
                
                client.getInputStream().read(new byte[1024]);
                
                // 返回304 Not Modified
                HttpResponse response = new HttpResponse(HttpStatus.NOT_MODIFIED);
                response.setHeader("ETag", "\"12345\"");
                
                OutputStream out = client.getOutputStream();
                out.write(response.toBytes());
                out.flush();
                client.close();
                
            } catch (IOException e) {
                if (serverRunning) {
                    e.printStackTrace();
                }
            }
        });
        serverThread.start();
        
        // 发送请求
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("GET", "http://localhost:" + testPort + "/cached");
        request.setHeader("If-None-Match", "\"12345\"");
        
        HttpResponse response = client.send(request);
        
        assertNotNull(response);
        assertEquals(304, response.getStatusCode());
        assertEquals("", response.getBodyAsString()); // 304不应该有body
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    void testMaxRedirects() throws Exception {
        serverThread = new Thread(() -> {
            try {
                // 持续返回重定向
                for (int i = 0; i < 10; i++) {
                    Socket client = testServer.accept();
                    client.getInputStream().read(new byte[1024]);
                    
                    HttpResponse redirect = new HttpResponse(HttpStatus.FOUND);
                    redirect.setHeader("Location", "http://localhost:" + testPort + "/redirect" + i);
                    
                    OutputStream out = client.getOutputStream();
                    out.write(redirect.toBytes());
                    out.flush();
                    client.close();
                }
            } catch (IOException e) {
                if (serverRunning) {
                    // 预期会因为客户端停止而中断
                }
            }
        });
        serverThread.start();
        
        // 发送请求
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("GET", "http://localhost:" + testPort + "/start");
        
        // 应该抛出异常，因为超过最大重定向次数
        IOException exception = assertThrows(IOException.class, () -> {
            client.send(request);
        });
        
        assertTrue(exception.getMessage().contains("Too many redirects"));
    }
    
    @Test
    void testConnectionTimeout() {
        // 使用一个不存在的IP地址来触发连接超时
        // 使用192.0.2.1（TEST-NET-1，保留用于文档和示例）
        HttpClient client = new HttpClient();
        HttpRequest request = new HttpRequest("GET", "http://192.0.2.1:9999/test");
        
        // 应该在10秒内超时
        assertThrows(IOException.class, () -> {
            client.send(request);
        });
    }
}
