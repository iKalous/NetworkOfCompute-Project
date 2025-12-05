package com.http.client;

/**
 * HTTP客户端主程序
 * 支持命令行和GUI两种模式
 */
public class ClientMain {
    
    public static void main(String[] args) {
        // 创建 HttpClient 实例
        HttpClient httpClient = new HttpClient();
        
        // 检查命令行参数选择模式
        String mode = "cli"; // 默认CLI模式
        
        if (args.length > 0) {
            mode = args[0].toLowerCase();
        }
        
        ClientInterface client;
        
        if ("gui".equals(mode)) {
            // GUI模式
            System.out.println("Starting HTTP Client in GUI mode...");
            client = new GuiClient(httpClient);
        } else {
            // CLI模式
            System.out.println("Starting HTTP Client in CLI mode...");
            System.out.println("Use 'java ClientMain gui' to start in GUI mode");
            client = new CliClient(httpClient);
        }
        
        // 启动客户端界面
        client.start();
    }
}
