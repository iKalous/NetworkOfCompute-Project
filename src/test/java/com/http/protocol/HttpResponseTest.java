package com.http.protocol;

import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @Test
    void testParseSuccessResponse() throws IOException {
        String body = "<html><body>Hello</body></html>";
        String rawResponse = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html\r\n" +
                            "Content-Length: " + body.length() + "\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n" +
                            body;
        
        ByteArrayInputStream input = new ByteArrayInputStream(rawResponse.getBytes(StandardCharsets.UTF_8));
        HttpResponse response = HttpResponse.parse(input);
        
        assertEquals(200, response.getStatusCode());
        assertEquals("OK", response.getStatusMessage());
        assertEquals("HTTP/1.1", response.getVersion());
        assertEquals("text/html", response.getHeader("Content-Type"));
        assertEquals(body, response.getBodyAsString());
    }

    @Test
    void testParseNotFoundResponse() throws IOException {
        String rawResponse = "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "\r\n";
        
        ByteArrayInputStream input = new ByteArrayInputStream(rawResponse.getBytes(StandardCharsets.UTF_8));
        HttpResponse response = HttpResponse.parse(input);
        
        assertEquals(404, response.getStatusCode());
        assertEquals("Not Found", response.getStatusMessage());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void testToBytesWithStatus() {
        HttpResponse response = new HttpResponse(HttpStatus.OK);
        response.setHeader("Content-Type", "text/plain");
        response.setBody("Success");
        
        byte[] bytes = response.toBytes();
        String result = new String(bytes, StandardCharsets.UTF_8);
        
        assertTrue(result.contains("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Content-Type: text/plain"));
        assertTrue(result.contains("Success"));
    }

    @Test
    void testToBytesNotFound() {
        HttpResponse response = new HttpResponse(HttpStatus.NOT_FOUND);
        response.setHeader("Content-Type", "text/html");
        response.setBody("<h1>404 Not Found</h1>");
        
        byte[] bytes = response.toBytes();
        String result = new String(bytes, StandardCharsets.UTF_8);
        
        assertTrue(result.contains("HTTP/1.1 404 Not Found"));
        assertTrue(result.contains("<h1>404 Not Found</h1>"));
    }

    @Test
    void testSetStatus() {
        HttpResponse response = new HttpResponse();
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        
        assertEquals(500, response.getStatusCode());
        assertEquals("Internal Server Error", response.getStatusMessage());
    }

    @Test
    void testInvalidStatusLine() {
        String rawResponse = "INVALID\r\n\r\n";
        ByteArrayInputStream input = new ByteArrayInputStream(rawResponse.getBytes(StandardCharsets.UTF_8));
        
        assertThrows(IOException.class, () -> HttpResponse.parse(input));
    }
}
