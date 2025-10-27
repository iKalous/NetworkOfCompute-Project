package com.http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * HTTP服务器
 * 基于Java Socket API实现的HTTP/1.1服务器
 */
public class HttpServer {
    
    private final int port;
    private final RequestRouter router;
    private final ExecutorService threadPool;
    private ServerSocket serverSocket;
    private volatile boolean running;
    private Thread acceptThread;
    
    private static final int DEFAULT_THREAD_POOL_SIZE = 20;

    /**
     * 创建HTTP服务器
     * @param port 监听端口
     * @param router 请求路由器
     */
    public HttpServer(int port, RequestRouter router) {
        this(port, router, DEFAULT_THREAD_POOL_SIZE);
    }

    /**
     * 创建HTTP服务器
     * @param port 监听端口
     * @param router 请求路由器
     * @param threadPoolSize 线程池大小
     */
    public HttpServer(int port, RequestRouter router, int threadPoolSize) {
        this.port = port;
        this.router = router;
        this.threadPool = Executors.newFixedThreadPool(threadPoolSize);
        this.running = false;
    }

    /**
     * 启动服务器
     * @throws IOException 如果无法绑定端口
     */
    public void start() throws IOException {
        if (running) {
            throw new IllegalStateException("Server is already running");
        }
        
        serverSocket = new ServerSocket(port);
        running = true;
        
        System.out.println("HTTP Server started on port " + port);
        
        // 创建接受连接的线程
        acceptThread = new Thread(() -> {
            while (running) {
                try {
                    // 接受客户端连接
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());
                    
                    // 为每个连接创建ConnectionHandler任务并提交到线程池
                    ConnectionHandler handler = new ConnectionHandler(clientSocket, router);
                    threadPool.execute(handler);
                    
                } catch (SocketException e) {
                    // ServerSocket关闭时会抛出SocketException
                    if (running) {
                        System.err.println("Socket error: " + e.getMessage());
                    }
                } catch (IOException e) {
                    if (running) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        }, "ServerAcceptThread");
        
        acceptThread.start();
    }

    /**
     * 停止服务器
     */
    public void stop() {
        if (!running) {
            return;
        }
        
        System.out.println("Stopping HTTP Server...");
        running = false;
        
        // 关闭ServerSocket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // 等待接受线程结束
        if (acceptThread != null) {
            try {
                acceptThread.join(5000); // 等待最多5秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 关闭线程池
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Thread pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        System.out.println("HTTP Server stopped");
    }

    /**
     * 检查服务器是否正在运行
     * @return true如果服务器正在运行
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * 获取服务器监听的端口
     * @return 端口号
     */
    public int getPort() {
        return port;
    }
}
