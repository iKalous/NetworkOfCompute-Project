package com.http.server;

import java.io.IOException;

/**
 * ServerMain - HTTP服务器主程序
 * 启动HTTP服务器并配置所有路由和处理器
 */
public class ServerMain {
    
    private static final int DEFAULT_PORT = 8080;
    private static final String STATIC_RESOURCES_PATH = "src/main/resources/static";
    
    public static void main(String[] args) {
        // 解析端口参数
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number: " + args[0]);
                System.err.println("Usage: java ServerMain [port]");
                System.exit(1);
            }
        }
        
        // 创建UserRegistry实例
        UserRegistry userRegistry = new UserRegistry();
        
        // 创建RequestRouter并注册所有handler
        RequestRouter router = new RequestRouter();
        
        // 注册API端点
        router.registerHandler("/api/register", new RegisterHandler(userRegistry));
        router.registerHandler("/api/login", new LoginHandler(userRegistry));
        
        // 设置StaticResourceHandler为默认handler
        StaticResourceHandler staticHandler = new StaticResourceHandler(STATIC_RESOURCES_PATH);
        router.setDefaultHandler(staticHandler);
        
        // 创建HttpServer实例
        HttpServer server = new HttpServer(port, router);
        
        // 添加优雅关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutdown signal received...");
            server.stop();
        }, "ShutdownHook"));
        
        // 启动服务器
        try {
            server.start();
            System.out.println("Server is running on http://localhost:" + port);
            System.out.println("API Endpoints:");
            System.out.println("  POST http://localhost:" + port + "/api/register");
            System.out.println("  POST http://localhost:" + port + "/api/login");
            System.out.println("Static resources served from: " + STATIC_RESOURCES_PATH);
            System.out.println("Press Ctrl+C to stop the server");
            
            // 保持主线程运行
            Thread.currentThread().join();
            
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (InterruptedException e) {
            System.out.println("Server interrupted");
            Thread.currentThread().interrupt();
        }
    }
}
