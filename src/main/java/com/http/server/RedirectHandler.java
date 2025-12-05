package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;

/**
 * 重定向处理器
 * 用于处理需要重定向的请求，支持301、302、304状态码
 */
public class RedirectHandler implements RequestHandler {

    private final HttpStatus redirectStatus;
    private final String targetLocation;

    /**
     * 构造重定向处理器
     * 
     * @param redirectStatus 重定向状态码（301、302等）
     * @param targetLocation 目标重定向地址
     */
    public RedirectHandler(HttpStatus redirectStatus, String targetLocation) {
        this.redirectStatus = redirectStatus;
        this.targetLocation = targetLocation;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        HttpResponse response = new HttpResponse(redirectStatus);

        // 设置Location头，指示重定向目标
        if (targetLocation != null && !targetLocation.isEmpty()) {
            response.setHeader("Location", targetLocation);
        }

        // 设置响应体
        String body = redirectStatus.getCode() + " " + redirectStatus.getMessage();
        response.setBody(body);
        response.setHeader("Content-Type", "text/plain");
        response.setHeader("Content-Length", String.valueOf(body.length()));

        return response;
    }

    /**
     * 创建301永久重定向处理器
     */
    public static RedirectHandler movedPermanently(String targetLocation) {
        return new RedirectHandler(HttpStatus.MOVED_PERMANENTLY, targetLocation);
    }

    /**
     * 创建302临时重定向处理器
     */
    public static RedirectHandler found(String targetLocation) {
        return new RedirectHandler(HttpStatus.FOUND, targetLocation);
    }
}
