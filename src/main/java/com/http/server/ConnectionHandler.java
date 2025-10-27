package com.http.server;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;
import com.http.protocol.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * 连接处理器
 * 处理单个客户端连接，支持HTTP/1.1长连接
 */
public class ConnectionHandler implements Runnable {
    
    private final Socket socket;
    private final RequestRouter router;
    private static final int SOCKET_TIMEOUT = 30000; // 30秒超时

    public ConnectionHandler(Socket socket, RequestRouter router) {
        this.socket = socket;
        this.router = router;
    }

    @Override
    public void run() {
        try {
            // 设置Socket超时
            socket.setSoTimeout(SOCKET_TIMEOUT);
            
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();
            
            // 长连接循环：持续读取请求直到连接关闭
            boolean keepAlive = true;
            
            while (keepAlive && !socket.isClosed()) {
                try {
                    // 解析HTTP请求
                    HttpRequest request = HttpRequest.parse(input);
                    
                    // 路由请求到处理器
                    HttpResponse response = router.route(request);
                    
                    // 检查是否保持连接
                    String connectionHeader = request.getHeader("Connection");
                    if (connectionHeader != null && connectionHeader.equalsIgnoreCase("close")) {
                        keepAlive = false;
                        response.setHeader("Connection", "close");
                    } else {
                        // HTTP/1.1默认保持连接
                        response.setHeader("Connection", "keep-alive");
                    }
                    
                    // 发送响应
                    output.write(response.toBytes());
                    output.flush();
                    
                    // 如果不保持连接，退出循环
                    if (!keepAlive) {
                        break;
                    }
                    
                } catch (SocketTimeoutException e) {
                    // 超时，关闭连接
                    System.out.println("Connection timeout, closing connection");
                    break;
                } catch (IOException e) {
                    // 连接已关闭或读取错误
                    if (!socket.isClosed()) {
                        // 尝试发送400错误响应
                        try {
                            HttpResponse errorResponse = new HttpResponse(HttpStatus.BAD_REQUEST);
                            errorResponse.setBody("400 Bad Request: " + e.getMessage());
                            errorResponse.setHeader("Content-Type", "text/plain");
                            errorResponse.setHeader("Connection", "close");
                            output.write(errorResponse.toBytes());
                            output.flush();
                        } catch (IOException ignored) {
                            // 无法发送错误响应，忽略
                        }
                    }
                    break;
                } catch (Exception e) {
                    // 处理其他未预期的异常
                    System.err.println("Error handling request: " + e.getMessage());
                    e.printStackTrace();
                    
                    try {
                        HttpResponse errorResponse = new HttpResponse(HttpStatus.INTERNAL_SERVER_ERROR);
                        errorResponse.setBody("500 Internal Server Error");
                        errorResponse.setHeader("Content-Type", "text/plain");
                        errorResponse.setHeader("Connection", "close");
                        output.write(errorResponse.toBytes());
                        output.flush();
                    } catch (IOException ignored) {
                        // 无法发送错误响应，忽略
                    }
                    break;
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error setting up connection: " + e.getMessage());
        } finally {
            // 关闭Socket连接
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
}
