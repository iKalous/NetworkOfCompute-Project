package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * 请求路由器
 * 负责将HTTP请求路由到相应的处理器
 */
public class RequestRouter {
    
    private final Map<String, RequestHandler> routes;
    private RequestHandler defaultHandler;

    public RequestRouter() {
        this.routes = new HashMap<>();
    }

    /**
     * 注册路径处理器
     * @param path 请求路径
     * @param handler 处理器
     */
    public void registerHandler(String path, RequestHandler handler) {
        routes.put(path, handler);
    }

    /**
     * 设置默认处理器（用于未匹配的路径）
     * @param handler 默认处理器
     */
    public void setDefaultHandler(RequestHandler handler) {
        this.defaultHandler = handler;
    }

    /**
     * 路由请求到相应的处理器
     * @param request HTTP请求
     * @return HTTP响应
     */
    public HttpResponse route(HttpRequest request) {
        // 检查请求方法是否为GET或POST
        String method = request.getMethod();
        if (!"GET".equalsIgnoreCase(method) && !"POST".equalsIgnoreCase(method)) {
            // 不支持的方法返回405
            HttpResponse response = new HttpResponse(HttpStatus.METHOD_NOT_ALLOWED);
            response.setBody("405 Method Not Allowed: " + method);
            response.setHeader("Content-Type", "text/plain");
            response.setHeader("Allow", "GET, POST");
            return response;
        }
        
        String uri = request.getUri();
        
        // 尝试精确匹配
        RequestHandler handler = routes.get(uri);
        
        if (handler != null) {
            return handler.handle(request);
        }
        
        // 如果有默认处理器，使用默认处理器
        if (defaultHandler != null) {
            return defaultHandler.handle(request);
        }
        
        // 未找到处理器，返回404
        HttpResponse response = new HttpResponse(HttpStatus.NOT_FOUND);
        response.setBody("404 Not Found: " + uri);
        response.setHeader("Content-Type", "text/plain");
        return response;
    }
}
