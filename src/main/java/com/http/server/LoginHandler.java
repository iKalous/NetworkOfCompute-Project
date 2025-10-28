package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;
import com.http.util.JsonParser;

import java.util.Map;

/**
 * LoginHandler处理用户登录请求
 * 端点: /api/login
 * 方法: POST
 * 请求体格式: {"username":"...", "password":"..."}
 */
public class LoginHandler implements RequestHandler {
    
    private final UserRegistry userRegistry;

    public LoginHandler(UserRegistry userRegistry) {
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

            // 调用UserRegistry.login
            String token = userRegistry.login(username, password);

            if (token != null) {
                // 返回成功响应（200）带token
                return createSuccessResponseWithToken("Login successful", token);
            } else {
                // 返回失败响应（401）- 用户名不存在或密码错误
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password");
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
     * 创建成功响应（带token）
     */
    private HttpResponse createSuccessResponseWithToken(String message, String token) {
        HttpResponse response = new HttpResponse(HttpStatus.OK);
        String jsonBody = JsonParser.buildResponseWithToken(true, message, token);
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
