package com.http.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple JSON parser for handling registration and login request/response formats.
 * Supports basic JSON object parsing and building for string key-value pairs.
 */
public class JsonParser {

    /**
     * Parse a simple JSON object string into a Map.
     * Supports format: {"key1":"value1","key2":"value2"}
     * 
     * @param json JSON string to parse
     * @return Map containing key-value pairs
     * @throws IllegalArgumentException if JSON format is invalid
     */
    public static Map<String, String> parseObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("JSON must start with { and end with }");
        }

        Map<String, String> result = new HashMap<>();
        
        // Remove outer braces
        String content = trimmed.substring(1, trimmed.length() - 1).trim();
        
        // Empty object
        if (content.isEmpty()) {
            return result;
        }

        // Split by comma (simple approach for non-nested objects)
        String[] pairs = content.split(",");
        
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length != 2) {
                throw new IllegalArgumentException("Invalid key-value pair: " + pair);
            }

            String key = unquote(keyValue[0].trim());
            String value = unquote(keyValue[1].trim());
            
            result.put(key, value);
        }

        return result;
    }

    /**
     * Build a JSON response object for registration/login success.
     * 
     * @param success Whether the operation was successful
     * @param message Message to include in response
     * @return JSON string
     */
    public static String buildResponse(boolean success, String message) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":").append(quote(message));
        json.append("}");
        return json.toString();
    }

    /**
     * Build a JSON response object for login success with token.
     * 
     * @param success Whether the operation was successful
     * @param message Message to include in response
     * @param token Session token
     * @return JSON string
     */
    public static String buildResponseWithToken(boolean success, String message, String token) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":").append(success).append(",");
        json.append("\"message\":").append(quote(message)).append(",");
        json.append("\"token\":").append(quote(token));
        json.append("}");
        return json.toString();
    }

    /**
     * Build a simple JSON object from a map of string key-value pairs.
     * 
     * @param data Map containing key-value pairs
     * @return JSON string
     */
    public static String buildObject(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder();
        json.append("{");
        
        boolean first = true;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append(quote(entry.getKey())).append(":");
            json.append(quote(entry.getValue()));
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }

    /**
     * Remove quotes from a string value.
     * 
     * @param str String that may be quoted
     * @return Unquoted string
     */
    private static String unquote(String str) {
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    /**
     * Add quotes around a string value, escaping internal quotes.
     * 
     * @param str String to quote
     * @return Quoted string
     */
    private static String quote(String str) {
        if (str == null) {
            return "\"\"";
        }
        // Escape internal quotes and backslashes
        String escaped = str.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * Extract a string value from a parsed JSON map.
     * 
     * @param map Parsed JSON map
     * @param key Key to extract
     * @return Value or null if not found
     */
    public static String getString(Map<String, String> map, String key) {
        return map.get(key);
    }

    /**
     * Extract a required string value from a parsed JSON map.
     * 
     * @param map Parsed JSON map
     * @param key Key to extract
     * @return Value
     * @throws IllegalArgumentException if key is not found
     */
    public static String getRequiredString(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + key + "' not found in JSON");
        }
        return value;
    }
}
