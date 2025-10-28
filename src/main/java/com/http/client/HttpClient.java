package com.http.client;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * HTTP客户端
 * 负责发送HTTP请求并接收响应
 */
public class HttpClient {
    
    private static final int CONNECT_TIMEOUT = 10000; // 10秒连接超时
    private static final int READ_TIMEOUT = 30000;    // 30秒读取超时
    private static final int MAX_REDIRECTS = 5;       // 最大重定向次数
    
    /**
     * 发送HTTP请求并接收响应
     * @param request HTTP请求对象
     * @return HTTP响应对象
     * @throws IOException 如果网络通信失败
     */
    public HttpResponse send(HttpRequest request) throws IOException {
        return sendWithRedirect(request, 0);
    }
    
    /**
     * 发送HTTP请求并处理重定向
     * @param request HTTP请求对象
     * @param redirectCount 当前重定向次数
     * @return HTTP响应对象
     * @throws IOException 如果网络通信失败
     */
    private HttpResponse sendWithRedirect(HttpRequest request, int redirectCount) throws IOException {
        // 检查重定向次数限制
        if (redirectCount >= MAX_REDIRECTS) {
            throw new IOException("Too many redirects (max " + MAX_REDIRECTS + ")");
        }
        
        // 解析URI获取主机和端口
        URI uri;
        try {
            String uriString = request.getUri();
            // 如果URI不包含协议，添加http://
            if (!uriString.startsWith("http://") && !uriString.startsWith("https://")) {
                // 如果只是路径，需要从Host头获取主机信息
                String host = request.getHeader("Host");
                if (host != null) {
                    uriString = "http://" + host + uriString;
                } else {
                    throw new IOException("Cannot determine host from URI: " + uriString);
                }
            }
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new IOException("Invalid URI: " + request.getUri(), e);
        }
        
        String host = uri.getHost();
        int port = uri.getPort();
        if (port == -1) {
            port = 80; // 默认HTTP端口
        }
        
        // 更新请求的URI为路径部分
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        if (uri.getQuery() != null) {
            path += "?" + uri.getQuery();
        }
        request.setUri(path);
        
        // 确保Host头存在
        if (request.getHeader("Host") == null) {
            request.setHeader("Host", host + (port != 80 ? ":" + port : ""));
        }
        
        Socket socket = null;
        try {
            // 创建Socket连接
            socket = new Socket();
            socket.connect(new java.net.InetSocketAddress(host, port), CONNECT_TIMEOUT);
            socket.setSoTimeout(READ_TIMEOUT);
            
            // 发送请求
            OutputStream out = socket.getOutputStream();
            out.write(request.toBytes());
            out.flush();
            
            // 接收响应
            InputStream in = socket.getInputStream();
            HttpResponse response = HttpResponse.parse(in);
            
            // 检查是否需要重定向
            int statusCode = response.getStatusCode();
            if (statusCode == 301 || statusCode == 302 || statusCode == 304) {
                return handleRedirect(response, redirectCount);
            }
            
            return response;
            
        } catch (SocketTimeoutException e) {
            throw new IOException("Request timeout", e);
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
    
    /**
     * 处理重定向响应
     * @param response 重定向响应
     * @param redirectCount 当前重定向次数
     * @return 最终的HTTP响应
     * @throws IOException 如果处理失败
     */
    private HttpResponse handleRedirect(HttpResponse response, int redirectCount) throws IOException {
        int statusCode = response.getStatusCode();
        
        // 304 Not Modified - 使用缓存，不需要重新请求
        if (statusCode == 304) {
            return response;
        }
        
        // 301 Moved Permanently 或 302 Found - 需要重定向
        String location = response.getHeader("Location");
        if (location == null || location.isEmpty()) {
            throw new IOException("Redirect response missing Location header");
        }
        
        // 创建新的请求
        HttpRequest redirectRequest = new HttpRequest("GET", location);
        redirectRequest.setHeader("User-Agent", "HttpClient/1.0");
        redirectRequest.setHeader("Connection", "close");
        
        // 递归发送重定向请求
        return sendWithRedirect(redirectRequest, redirectCount + 1);
    }
}
