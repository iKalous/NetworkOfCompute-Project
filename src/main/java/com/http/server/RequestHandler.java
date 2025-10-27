package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;

/**
 * 请求处理器接口
 * 定义处理HTTP请求的标准接口
 */
public interface RequestHandler {
    
    /**
     * 处理HTTP请求并生成响应
     * @param request HTTP请求对象
     * @return HTTP响应对象
     */
    HttpResponse handle(HttpRequest request);
}
