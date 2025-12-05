package com.http.client;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * 命令行HTTP客户端
 * 提供交互式命令行界面用于发送HTTP请求
 */
public class CliClient implements ClientInterface {
    
    private final HttpClient client;
    private final Scanner scanner;
    private boolean running;
    
    public CliClient(HttpClient client) {
        this.client = client;
        this.scanner = new Scanner(System.in);
        this.running = false;
    }
    
    @Override
    public void start() {
        running = true;
        printWelcome();
        
        while (running) {
            try {
                System.out.println("\n" + "=".repeat(60));
                System.out.println("Enter command (send/exit/help):");
                System.out.print("> ");
                
                String command = scanner.nextLine().trim().toLowerCase();
                
                switch (command) {
                    case "send":
                        handleSendRequest();
                        break;
                    case "exit":
                    case "quit":
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    case "help":
                        printHelp();
                        break;
                    default:
                        System.out.println("Unknown command. Type 'help' for available commands.");
                }
                
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        
        scanner.close();
    }
    
    /**
     * 处理发送请求的交互流程
     */
    private void handleSendRequest() {
        try {
            // 读取URL
            System.out.println("\nEnter URL (e.g., http://localhost:8080/index.html):");
            System.out.print("> ");
            String url = scanner.nextLine().trim();
            
            if (url.isEmpty()) {
                System.out.println("URL cannot be empty.");
                return;
            }
            
            // 读取请求方法
            System.out.println("\nEnter HTTP method (GET/POST) [default: GET]:");
            System.out.print("> ");
            String method = scanner.nextLine().trim().toUpperCase();
            
            if (method.isEmpty()) {
                method = "GET";
            }
            
            if (!method.equals("GET") && !method.equals("POST")) {
                System.out.println("Only GET and POST methods are supported.");
                return;
            }
            
            // 创建请求
            HttpRequest request = new HttpRequest(method, url);
            request.setHeader("User-Agent", "CliClient/1.0");
            request.setHeader("Connection", "close");
            
            // 读取自定义请求头
            System.out.println("\nEnter custom headers (format: Name: Value, empty line to finish):");
            while (true) {
                System.out.print("> ");
                String headerLine = scanner.nextLine().trim();
                
                if (headerLine.isEmpty()) {
                    break;
                }
                
                int colonIndex = headerLine.indexOf(':');
                if (colonIndex > 0) {
                    String headerName = headerLine.substring(0, colonIndex).trim();
                    String headerValue = headerLine.substring(colonIndex + 1).trim();
                    request.setHeader(headerName, headerValue);
                } else {
                    System.out.println("Invalid header format. Use 'Name: Value'");
                }
            }
            
            // 如果是 POST 请求，读取请求体
            if (method.equals("POST")) {
                System.out.println("\nEnter request body (empty line to finish):");
                StringBuilder bodyBuilder = new StringBuilder();
                
                while (true) {
                    System.out.print("> ");
                    String line = scanner.nextLine();
                    
                    if (line.isEmpty()) {
                        break;
                    }
                    
                    if (bodyBuilder.length() > 0) {
                        bodyBuilder.append("\n");
                    }
                    bodyBuilder.append(line);
                }
                
                String body = bodyBuilder.toString();
                if (!body.isEmpty()) {
                    request.setBody(body);
                    
                    // 如果没有设置 Content-Type，默认使用 application/json
                    if (request.getHeader("Content-Type") == null) {
                        request.setHeader("Content-Type", "application/json");
                    }
                }
            }
            
            // 发送请求
            System.out.println("\nSending request...");
            HttpResponse response = client.send(request);
            
            // 显示响应
            displayResponse(response);
            
        } catch (IOException e) {
            System.err.println("Failed to send request: " + e.getMessage());
        }
    }
    
    @Override
    public void displayResponse(HttpResponse response) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESPONSE");
        System.out.println("=".repeat(60));
        
        // 显示状态行
        System.out.println("\nStatus: " + response.getStatusCode() + " " + response.getStatusMessage());
        
        // 显示响应头
        System.out.println("\nHeaders:");
        if (response.getHeaders().isEmpty()) {
            System.out.println("  (none)");
        } else {
            response.getHeaders().forEach((name, value) -> 
                System.out.println("  " + name + ": " + value)
            );
        }
        
        // 显示响应体
        System.out.println("\nBody:");
        String body = response.getBodyAsString();
        if (body.isEmpty()) {
            System.out.println("  (empty)");
        } else {
            // 检查 Content-Type 来决定如何显示
            String contentType = response.getHeader("Content-Type");
            
            if (contentType != null && (contentType.startsWith("text/") || 
                                       contentType.contains("json") || 
                                       contentType.contains("xml"))) {
                // 文本内容，直接显示
                System.out.println(body);
            } else {
                // 二进制内容，显示大小
                System.out.println("  (binary content, " + response.getBody().length + " bytes)");
            }
        }
        
        System.out.println("\n" + "=".repeat(60));
    }
    
    /**
     * 打印欢迎信息
     */
    private void printWelcome() {
        System.out.println("=".repeat(60));
        System.out.println("HTTP CLI Client");
        System.out.println("=".repeat(60));
        System.out.println("Type 'help' for available commands");
    }
    
    /**
     * 打印帮助信息
     */
    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  send  - Send an HTTP request");
        System.out.println("  exit  - Exit the client");
        System.out.println("  help  - Show this help message");
        System.out.println("\nExample workflow:");
        System.out.println("  1. Type 'send'");
        System.out.println("  2. Enter URL: http://localhost:8080/api/login");
        System.out.println("  3. Enter method: POST");
        System.out.println("  4. Enter headers (optional): Content-Type: application/json");
        System.out.println("  5. Enter body: {\"username\":\"test\",\"password\":\"123456\"}");
    }
}
