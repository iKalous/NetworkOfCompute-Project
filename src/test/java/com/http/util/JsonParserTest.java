package com.http.util;

import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonParserTest {

    @Test
    void testParseObject_ValidJson() {
        String json = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        Map<String, String> result = JsonParser.parseObject(json);
        
        assertEquals(2, result.size());
        assertEquals("testuser", result.get("username"));
        assertEquals("testpass", result.get("password"));
    }

    @Test
    void testParseObject_EmptyObject() {
        String json = "{}";
        Map<String, String> result = JsonParser.parseObject(json);
        
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseObject_WithSpaces() {
        String json = "{ \"key1\" : \"value1\" , \"key2\" : \"value2\" }";
        Map<String, String> result = JsonParser.parseObject(json);
        
        assertEquals(2, result.size());
        assertEquals("value1", result.get("key1"));
        assertEquals("value2", result.get("key2"));
    }

    @Test
    void testParseObject_InvalidJson_NoBraces() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonParser.parseObject("username:testuser");
        });
    }

    @Test
    void testParseObject_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonParser.parseObject(null);
        });
    }

    @Test
    void testBuildResponse_Success() {
        String json = JsonParser.buildResponse(true, "Registration successful");
        
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"message\":\"Registration successful\""));
    }

    @Test
    void testBuildResponse_Failure() {
        String json = JsonParser.buildResponse(false, "Username already exists");
        
        assertTrue(json.contains("\"success\":false"));
        assertTrue(json.contains("\"message\":\"Username already exists\""));
    }

    @Test
    void testBuildResponseWithToken() {
        String json = JsonParser.buildResponseWithToken(true, "Login successful", "abc123token");
        
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"message\":\"Login successful\""));
        assertTrue(json.contains("\"token\":\"abc123token\""));
    }

    @Test
    void testBuildObject() {
        Map<String, String> data = new HashMap<>();
        data.put("username", "testuser");
        data.put("password", "testpass");
        
        String json = JsonParser.buildObject(data);
        
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("\"username\""));
        assertTrue(json.contains("\"testuser\""));
        assertTrue(json.contains("\"password\""));
        assertTrue(json.contains("\"testpass\""));
    }

    @Test
    void testGetString() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        
        assertEquals("value", JsonParser.getString(map, "key"));
        assertNull(JsonParser.getString(map, "nonexistent"));
    }

    @Test
    void testGetRequiredString_Found() {
        Map<String, String> map = new HashMap<>();
        map.put("key", "value");
        
        assertEquals("value", JsonParser.getRequiredString(map, "key"));
    }

    @Test
    void testGetRequiredString_NotFound() {
        Map<String, String> map = new HashMap<>();
        
        assertThrows(IllegalArgumentException.class, () -> {
            JsonParser.getRequiredString(map, "nonexistent");
        });
    }

    @Test
    void testRoundTrip_RegisterRequest() {
        // Build JSON
        Map<String, String> data = new HashMap<>();
        data.put("username", "newuser");
        data.put("password", "securepass");
        String json = JsonParser.buildObject(data);
        
        // Parse it back
        Map<String, String> parsed = JsonParser.parseObject(json);
        
        assertEquals("newuser", parsed.get("username"));
        assertEquals("securepass", parsed.get("password"));
    }
}
