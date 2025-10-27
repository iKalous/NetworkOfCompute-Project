package com.http.protocol;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestTest {

    @Test
    void testParseGetRequest() throws IOException {
        String rawRequest = "GET /index.html HTTP/1.1\r\n" +
                           "Host: localhost:8080\r\n" +
                           "User-Agent: TestClient\r\n" +
                           "Connection: keep-alive\r\n" +
                           "\r\n";
        
        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.parse(input);
        
        assertEquals("GET", request.getMethod());
        assertEquals("/index.html", request.getUri());
        assertEquals("HTTP/1.1", request.getVersion());
        assertEquals("localhost:8080", request.getHeader("Host"));
        assertEquals("TestClient", request.getHeader("User-Agent"));
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals(0, request.getBody().length);
    }

    @Test
    void testParsePostRequestWithBody() throws IOException {
        String body = "{\"username\":\"test\",\"password\":\"pass123\"}";
        String rawRequest = "POST /api/login HTTP/1.1\r\n" +
                           "Host: localhost:8080\r\n" +
                           "Content-Type: application/json\r\n" +
                           "Content-Length: " + body.length() + "\r\n" +
                           "\r\n" +
                           body;
        
        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        HttpRequest request = HttpRequest.parse(input);
        
        assertEquals("POST", request.getMethod());
        assertEquals("/api/login", request.getUri());
        assertEquals("application/json", request.getHeader("Content-Type"));
        assertEquals(body, request.getBodyAsString());
    }

    @Test
    void testToBytesGetRequest() {
        HttpRequest request = new HttpRequest("GET", "/test.html");
        request.setHeader("Host", "example.com");
        request.setHeader("Connection", "close");
        
        byte[] bytes = request.toBytes();
        String result = new String(bytes, StandardCharsets.UTF_8);
        
        assertTrue(result.contains("GET /test.html HTTP/1.1"));
        assertTrue(result.contains("Host: example.com"));
        assertTrue(result.contains("Connection: close"));
    }

    @Test
    void testToBytesPostRequestWithBody() {
        HttpRequest request = new HttpRequest("POST", "/api/register");
        request.setHeader("Host", "localhost");
        request.setHeader("Content-Type", "application/json");
        request.setBody("{\"username\":\"user1\"}");
        
        byte[] bytes = request.toBytes();
        String result = new String(bytes, StandardCharsets.UTF_8);
        
        assertTrue(result.contains("POST /api/register HTTP/1.1"));
        assertTrue(result.contains("Content-Type: application/json"));
        assertTrue(result.contains("{\"username\":\"user1\"}"));
    }

    @Test
    void testInvalidRequestLine() {
        String rawRequest = "INVALID\r\n\r\n";
        ByteArrayInputStream input = new ByteArrayInputStream(rawRequest.getBytes(StandardCharsets.UTF_8));
        
        assertThrows(IOException.class, () -> HttpRequest.parse(input));
    }
}
