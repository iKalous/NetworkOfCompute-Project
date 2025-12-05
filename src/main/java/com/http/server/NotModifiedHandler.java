package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;

/**
 * 304 Not Modified处理器
 * 用于处理条件请求，当资源未修改时返回304状态码
 */
public class NotModifiedHandler implements RequestHandler {

    private final String etag;
    private final String lastModified;
    private final RequestHandler actualHandler;

    /**
     * 构造304处理器
     * 
     * @param etag          资源的ETag值
     * @param lastModified  资源的最后修改时间
     * @param actualHandler 实际处理请求的处理器（当资源已修改时使用）
     */
    public NotModifiedHandler(String etag, String lastModified, RequestHandler actualHandler) {
        this.etag = etag;
        this.lastModified = lastModified;
        this.actualHandler = actualHandler;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        // 检查If-None-Match头（ETag匹配）
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return createNotModifiedResponse();
        }

        // 检查If-Modified-Since头（时间匹配）
        String ifModifiedSince = request.getHeader("If-Modified-Since");
        if (ifModifiedSince != null && ifModifiedSince.equals(lastModified)) {
            return createNotModifiedResponse();
        }

        // 资源已修改，返回实际内容
        if (actualHandler != null) {
            HttpResponse response = actualHandler.handle(request);
            // 添加缓存相关头
            if (etag != null) {
                response.setHeader("ETag", etag);
            }
            if (lastModified != null) {
                response.setHeader("Last-Modified", lastModified);
            }
            return response;
        }

        // 没有实际处理器，返回404
        HttpResponse response = new HttpResponse(HttpStatus.NOT_FOUND);
        response.setBody("404 Not Found");
        response.setHeader("Content-Type", "text/plain");
        return response;
    }

    /**
     * 创建304 Not Modified响应
     */
    private HttpResponse createNotModifiedResponse() {
        HttpResponse response = new HttpResponse(HttpStatus.NOT_MODIFIED);
        // 304响应不应包含响应体
        if (etag != null) {
            response.setHeader("ETag", etag);
        }
        if (lastModified != null) {
            response.setHeader("Last-Modified", lastModified);
        }
        return response;
    }
}
