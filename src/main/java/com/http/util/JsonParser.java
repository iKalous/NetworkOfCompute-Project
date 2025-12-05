package com.http.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单的 JSON 解析器 用于处理注册和登录请求/响应格式
 * 支持基本的 JSON 对象解析和构建字符串键值对
 */
public class JsonParser {

    /**
     * 将简单的 JSON 对象字符串解析为 Map
     * 支持格式: {"key1":"value1","key2":"value2"}
     *
     * @param json 要解析的 JSON 字符串
     * @return 包含键值对的 Map
     * @throws IllegalArgumentException 如果 JSON 格式无效
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

        // 移除外层大括号
        String content = trimmed.substring(1, trimmed.length() - 1).trim();

        // 空对象
        if (content.isEmpty()) {
            return result;
        }

        // 按逗号分割（简单处理非嵌套对象）
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
     * 构建注册/登录成功的 JSON 响应对象
     *
     * @param success 操作是否成功
     * @param message 响应中包含的消息
     * @return JSON 字符串
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
     * 构建带 token 的登录成功 JSON 响应对象
     *
     * @param success 操作是否成功
     * @param message 响应中包含的消息
     * @param token 会话 token
     * @return JSON 字符串
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
     * 从字符串键值对的 map 构建简单的 JSON 对象
     *
     * @param data 包含键值对的 Map
     * @return JSON 字符串
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
     * 从字符串值中移除引号
     *
     * @param str 可能被引号包围的字符串
     * @return 去除引号的字符串
     */
    private static String unquote(String str) {
        if (str.startsWith("\"") && str.endsWith("\"") && str.length() >= 2) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }

    /**
     * 在字符串值周围添加引号，转义内部引号
     *
     * @param str 要加引号的字符串
     * @return 加引号的字符串
     */
    private static String quote(String str) {
        if (str == null) {
            return "\"\"";
        }
        // 转义内部引号和反斜杠
        String escaped = str.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * 从解析的 JSON map 中提取字符串值
     *
     * @param map 解析后的 JSON map
     * @param key 要提取的键
     * @return 值 如果未找到则返回 null
     */
    public static String getString(Map<String, String> map, String key) {
        return map.get(key);
    }

    /**
     * 从解析的 JSON map 中提取必需的字符串值
     *
     * @param map 解析后的 JSON map
     * @param key 要提取的键
     * @return 值
     * @throws IllegalArgumentException 如果找不到键
     */
    public static String getRequiredString(Map<String, String> map, String key) {
        String value = map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Required field '" + key + "' not found in JSON");
        }
        return value;
    }
}
