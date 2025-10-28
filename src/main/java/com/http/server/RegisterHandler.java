package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;
import com.http.util.JsonParser;

import java.util.Map;

/**
 * RegisterHandler处理用户注册请求
 * 端点: /api/register
 * 方法: POST
 * 请求体格式: {"username":"...", "password":"..."}
 */
public class RegisterHandler implements RequestHandler {
    
    private final UserRegistry userRegistry;

    public RegisterHandler(UserRegistry userRegistry) {
        this.userRegistry = userRegistry;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        // 验证请求方法为POST
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, "Only POST method is allowed");
        }

        try {
            // 解析请求body的JSON
            String bodyString = request.getBodyAsString();
            if (bodyString == null || bodyString.trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Request body is required");
            }

            Map<String, String> jsonData = JsonParser.parseObject(bodyString);
            
            // 提取username和password
            String username = JsonParser.getString(jsonData, "username");
            String password = JsonParser.getString(jsonData, "password");

            // 验证username和password不为空
            if (username == null || username.trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Username is required");
            }
            if (password == null || password.trim().isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Password is required");
            }

            // 验证username长度（3-20字符）
            if (username.length() < 3 || username.length() > 20) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Username must be between 3 and 20 characters");
            }

            // 验证password长度（至少6字符）
            if (password.length() < 6) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters");
            }

            // 调用UserRegistry.register
            boolean success = userRegistry.register(username, password);

            if (success) {
                // 返回成功响应（200）
                return createSuccessResponse("Registration successful");
            } else {
                // 返回失败响应（400）- 用户名已存在
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Username already exists");
            }

        } catch (IllegalArgumentException e) {
            // JSON解析错误
            return createErrorResponse(HttpStatus.BAD_REQUEST, "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            // 其他未预期的错误
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    /**
     * 创建成功响应
     */
    private HttpResponse createSuccessResponse(String message) {
        HttpResponse response = new HttpResponse(HttpStatus.OK);
        String jsonBody = JsonParser.buildResponse(true, message);
        response.setBody(jsonBody);
        response.setHeader("Content-Type", "application/json");
        response.setHeader("Content-Length", String.valueOf(jsonBody.length()));
        return response;
    }

    /**
     * 创建错误响应
     */
    private HttpResponse createErrorResponse(HttpStatus status, String message) {
        HttpResponse response = new HttpResponse(status);
        String jsonBody = JsonParser.buildResponse(false, message);
        response.setBody(jsonBody);
        response.setHeader("Content-Type", "application/json");
        response.setHeader("Content-Length", String.valueOf(jsonBody.length()));
        return response;
    }
}
