package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;
import com.http.protocol.MimeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 静态资源处理器
 * 从指定根目录读取并提供静态文件服务
 */
public class StaticResourceHandler implements RequestHandler {
    
    private final String rootDirectory;

    /**
     * 构造静态资源处理器
     * @param rootDirectory 静态资源根目录路径
     */
    public StaticResourceHandler(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    /**
     * 处理静态资源请求
     * @param request HTTP请求对象
     * @return HTTP响应对象
     */
    @Override
    public HttpResponse handle(HttpRequest request) {
        HttpResponse response = new HttpResponse();
        
        try {
            // 获取请求的URI
            String uri = request.getUri();
            
            // 如果URI是根路径，默认返回index.html
            if (uri.equals("/") || uri.isEmpty()) {
                uri = "/index.html";
            }
            
            // 构建文件路径
            Path filePath = Paths.get(rootDirectory, uri).normalize();
            Path rootPath = Paths.get(rootDirectory).normalize();
            
            // 安全检查：防止路径遍历攻击
            if (!filePath.startsWith(rootPath)) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setBody("404 Not Found");
                response.setHeader("Content-Type", "text/plain");
                return response;
            }
            
            // 检查文件是否存在
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                response.setStatus(HttpStatus.NOT_FOUND);
                response.setBody("404 Not Found");
                response.setHeader("Content-Type", "text/plain");
                return response;
            }
            
            // 读取文件内容
            byte[] fileContent = Files.readAllBytes(filePath);
            
            // 根据文件扩展名设置Content-Type
            String filename = filePath.getFileName().toString();
            String mimeType = MimeType.getByExtension(filename);
            
            // 构建成功响应
            response.setStatus(HttpStatus.OK);
            response.setBody(fileContent);
            response.setHeader("Content-Type", mimeType);
            response.setHeader("Content-Length", String.valueOf(fileContent.length));
            
        } catch (IOException e) {
            // 文件读取错误，返回500
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
            response.setBody("500 Internal Server Error");
            response.setHeader("Content-Type", "text/plain");
        }
        
        return response;
    }
}
