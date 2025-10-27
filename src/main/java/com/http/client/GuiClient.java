package com.http.client;

import com.http.protocol.HttpRequest;
import com.http.protocol.HttpResponse;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * GUI HTTP客户端
 * 提供图形用户界面用于发送HTTP请求
 */
public class GuiClient extends JFrame implements ClientInterface {
    
    private final HttpClient client;
    
    // 输入组件
    private JTextField urlField;
    private JComboBox<String> methodBox;
    private JTextArea headersArea;
    private JTextArea bodyArea;
    private JButton sendButton;
    
    // 响应显示组件
    private JLabel statusLabel;
    private JTextArea responseHeadersArea;
    private JTextArea responseBodyArea;
    
    public GuiClient(HttpClient client) {
        this.client = client;
        initializeUI();
    }
    
    /**
     * 初始化用户界面
     */
    private void initializeUI() {
        setTitle("HTTP GUI Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 创建输入面板
        JPanel inputPanel = createInputPanel();
        
        // 创建响应面板
        JPanel responsePanel = createResponsePanel();
        
        // 使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputPanel, responsePanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.5);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    /**
     * 创建输入面板
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Request"));
        
        // URL和方法面板
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        
        // URL输入
        JPanel urlPanel = new JPanel(new BorderLayout(5, 0));
        urlPanel.add(new JLabel("URL:"), BorderLayout.WEST);
        urlField = new JTextField("http://localhost:8080/");
        urlPanel.add(urlField, BorderLayout.CENTER);
        
        // 方法选择
        JPanel methodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        methodPanel.add(new JLabel("Method:"));
        methodBox = new JComboBox<>(new String[]{"GET", "POST"});
        methodBox.setPreferredSize(new Dimension(100, 25));
        methodPanel.add(methodBox);
        
        // 发送按钮
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(100, 25));
        sendButton.addActionListener(e -> handleSendRequest());
        methodPanel.add(sendButton);
        
        topPanel.add(urlPanel, BorderLayout.CENTER);
        topPanel.add(methodPanel, BorderLayout.EAST);
        
        // 请求头面板
        JPanel headersPanel = new JPanel(new BorderLayout(5, 5));
        headersPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        headersPanel.add(new JLabel("Headers (Name: Value, one per line):"), BorderLayout.NORTH);
        headersArea = new JTextArea(4, 40);
        headersArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        headersArea.setText("Content-Type: application/json");
        JScrollPane headersScroll = new JScrollPane(headersArea);
        headersPanel.add(headersScroll, BorderLayout.CENTER);
        
        // 请求体面板
        JPanel bodyPanel = new JPanel(new BorderLayout(5, 5));
        bodyPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        bodyPanel.add(new JLabel("Body:"), BorderLayout.NORTH);
        bodyArea = new JTextArea(6, 40);
        bodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        bodyArea.setLineWrap(true);
        bodyArea.setWrapStyleWord(true);
        JScrollPane bodyScroll = new JScrollPane(bodyArea);
        bodyPanel.add(bodyScroll, BorderLayout.CENTER);
        
        // 组装输入面板
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.add(headersPanel, BorderLayout.NORTH);
        centerPanel.add(bodyPanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建响应面板
     */
    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Response"));
        
        // 状态标签
        statusLabel = new JLabel("Status: (no response yet)");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(statusLabel, BorderLayout.NORTH);
        
        // 响应头面板
        JPanel responseHeadersPanel = new JPanel(new BorderLayout(5, 5));
        responseHeadersPanel.add(new JLabel("Headers:"), BorderLayout.NORTH);
        responseHeadersArea = new JTextArea(6, 40);
        responseHeadersArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        responseHeadersArea.setEditable(false);
        responseHeadersArea.setBackground(new Color(245, 245, 245));
        JScrollPane responseHeadersScroll = new JScrollPane(responseHeadersArea);
        responseHeadersPanel.add(responseHeadersScroll, BorderLayout.CENTER);
        
        // 响应体面板
        JPanel responseBodyPanel = new JPanel(new BorderLayout(5, 5));
        responseBodyPanel.add(new JLabel("Body:"), BorderLayout.NORTH);
        responseBodyArea = new JTextArea(10, 40);
        responseBodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        responseBodyArea.setEditable(false);
        responseBodyArea.setBackground(new Color(245, 245, 245));
        responseBodyArea.setLineWrap(true);
        responseBodyArea.setWrapStyleWord(true);
        JScrollPane responseBodyScroll = new JScrollPane(responseBodyArea);
        responseBodyPanel.add(responseBodyScroll, BorderLayout.CENTER);
        
        // 使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                                               responseHeadersPanel, 
                                               responseBodyPanel);
        splitPane.setDividerLocation(150);
        splitPane.setResizeWeight(0.3);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 处理发送请求按钮点击事件
     */
    private void handleSendRequest() {
        // 在后台线程中发送请求，避免阻塞UI
        SwingWorker<HttpResponse, Void> worker = new SwingWorker<>() {
            @Override
            protected HttpResponse doInBackground() throws Exception {
                // 禁用发送按钮
                SwingUtilities.invokeLater(() -> {
                    sendButton.setEnabled(false);
                    sendButton.setText("Sending...");
                    statusLabel.setText("Status: Sending request...");
                });
                
                // 获取输入
                String url = urlField.getText().trim();
                String method = (String) methodBox.getSelectedItem();
                
                if (url.isEmpty()) {
                    throw new IllegalArgumentException("URL cannot be empty");
                }
                
                // 创建请求
                HttpRequest request = new HttpRequest(method, url);
                request.setHeader("User-Agent", "GuiClient/1.0");
                request.setHeader("Connection", "close");
                
                // 解析并添加自定义请求头
                String headersText = headersArea.getText().trim();
                if (!headersText.isEmpty()) {
                    String[] headerLines = headersText.split("\n");
                    for (String line : headerLines) {
                        line = line.trim();
                        if (!line.isEmpty()) {
                            int colonIndex = line.indexOf(':');
                            if (colonIndex > 0) {
                                String headerName = line.substring(0, colonIndex).trim();
                                String headerValue = line.substring(colonIndex + 1).trim();
                                request.setHeader(headerName, headerValue);
                            }
                        }
                    }
                }
                
                // 如果是POST请求，添加请求体
                if ("POST".equals(method)) {
                    String body = bodyArea.getText().trim();
                    if (!body.isEmpty()) {
                        request.setBody(body);
                    }
                }
                
                // 发送请求
                return client.send(request);
            }
            
            @Override
            protected void done() {
                try {
                    HttpResponse response = get();
                    displayResponse(response);
                } catch (Exception e) {
                    // 显示错误
                    statusLabel.setText("Status: Error - " + e.getMessage());
                    statusLabel.setForeground(Color.RED);
                    responseHeadersArea.setText("");
                    responseBodyArea.setText("Error: " + e.getMessage());
                    
                    if (e.getCause() != null) {
                        responseBodyArea.append("\n\nCause: " + e.getCause().getMessage());
                    }
                } finally {
                    // 重新启用发送按钮
                    sendButton.setEnabled(true);
                    sendButton.setText("Send");
                }
            }
        };
        
        worker.execute();
    }
    
    @Override
    public void start() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
    
    @Override
    public void displayResponse(HttpResponse response) {
        SwingUtilities.invokeLater(() -> {
            // 显示状态
            String statusText = "Status: " + response.getStatusCode() + " " + response.getStatusMessage();
            statusLabel.setText(statusText);
            
            // 根据状态码设置颜色
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                statusLabel.setForeground(new Color(0, 128, 0)); // 绿色
            } else if (response.getStatusCode() >= 300 && response.getStatusCode() < 400) {
                statusLabel.setForeground(new Color(255, 140, 0)); // 橙色
            } else {
                statusLabel.setForeground(Color.RED); // 红色
            }
            
            // 显示响应头
            StringBuilder headersText = new StringBuilder();
            if (response.getHeaders().isEmpty()) {
                headersText.append("(no headers)");
            } else {
                response.getHeaders().forEach((name, value) -> 
                    headersText.append(name).append(": ").append(value).append("\n")
                );
            }
            responseHeadersArea.setText(headersText.toString());
            responseHeadersArea.setCaretPosition(0);
            
            // 显示响应体
            String body = response.getBodyAsString();
            if (body.isEmpty()) {
                responseBodyArea.setText("(empty body)");
            } else {
                // 检查Content-Type来决定如何显示
                String contentType = response.getHeader("Content-Type");
                
                if (contentType != null && (contentType.startsWith("text/") || 
                                           contentType.contains("json") || 
                                           contentType.contains("xml"))) {
                    // 文本内容，直接显示
                    responseBodyArea.setText(body);
                } else {
                    // 二进制内容，显示大小
                    responseBodyArea.setText("(binary content, " + response.getBody().length + " bytes)");
                }
            }
            responseBodyArea.setCaretPosition(0);
        });
    }
}
