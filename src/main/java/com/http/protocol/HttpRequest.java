package com.http.protocol;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求类
 * 表示一个HTTP请求消息，包含请求行、请求头和请求体
 */
public class HttpRequest {
    
    private String method; //请求方法
    private String uri; // 资源分配符
    private String version; // http版本
    private Map<String, String> headers; //请求头信息
    private byte[] body; //请求体内容

    public HttpRequest() {
        this.headers = new HashMap<>();
        this.body = new byte[0];
        this.version = "HTTP/1.1";
    }

    public HttpRequest(String method, String uri) {
        this();
        this.method = method;
        this.uri = uri;
    }

    /**
     * 从InputStream解析HTTP请求
     * @param input 输入流
     * @return 解析后的HttpRequest对象
     * @throws IOException 如果读取或解析失败
     */
    public static HttpRequest parse(InputStream input) throws IOException {
        HttpRequest request = new HttpRequest();
        // InputStreamReader用于将输入流转换为字符流
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        
        // 解析请求行
        // 第一行格式 "POST /api/login HTTP/1.1\r\n"
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Invalid HTTP request: empty request line");
        }
        
        String[] requestParts = requestLine.split(" ");
        if (requestParts.length != 3) {
            throw new IOException("Invalid HTTP request line: " + requestLine);
        }
        
        request.method = requestParts[0];
        request.uri = requestParts[1];
        request.version = requestParts[2];
        
        // 解析请求头
        /* 请求头是元数据部分 ， 示例如下
         Host: www.example.com
        User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/91.0
        Accept: text/html,application/json
        * */
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String headerName = line.substring(0, colonIndex).trim();
                String headerValue = line.substring(colonIndex + 1).trim();
                request.headers.put(headerName, headerValue);
            }
        }
        //请求头和请求体以空行分割
        // 解析请求体
        String contentLengthStr = request.headers.get("Content-Length");
        //读取长度 ，循环读取是确保读到完整数据的方式
        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr);
                if (contentLength > 0) {
                    char[] bodyChars = new char[contentLength];
                    int totalRead = 0;
                    while (totalRead < contentLength) {
                        int read = reader.read(bodyChars, totalRead, contentLength - totalRead);
                        if (read == -1) {
                            break;
                        }
                        totalRead += read;
                    }
                    request.body = new String(bodyChars, 0, totalRead).getBytes(StandardCharsets.UTF_8);
                }
            } catch (NumberFormatException e) {
                throw new IOException("Invalid Content-Length header: " + contentLengthStr);
            }
        }
        
        return request;
    }

    /**
     * 将HTTP请求转换为字节数组
     * @return 请求的字节数组表示
     */
    public byte[] toBytes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
            // 写入请求行
            String requestLine = method + " " + uri + " " + version + "\r\n";
            output.write(requestLine.getBytes(StandardCharsets.UTF_8));
            
            // 写入请求头
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String headerLine = header.getKey() + ": " + header.getValue() + "\r\n";
                output.write(headerLine.getBytes(StandardCharsets.UTF_8));
            }
            
            // 如果有请求体，确保有Content-Length头
            if (body != null && body.length > 0) {
                if (!headers.containsKey("Content-Length")) {
                    String contentLength = "Content-Length: " + body.length + "\r\n";
                    output.write(contentLength.getBytes(StandardCharsets.UTF_8));
                }
            }
            
            // 写入空行（分隔头和体）
            output.write("\r\n".getBytes(StandardCharsets.UTF_8));
            
            // 写入请求体
            if (body != null && body.length > 0) {
                output.write(body);
            }
            
        } catch (IOException e) {
            // ByteArrayOutputStream不会抛出IOException
            throw new RuntimeException(e);
        }
        
        return output.toByteArray();
    }

    // 外部可能用到的接口
    
    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getHeader(String name) {
        return headers.get(name);
    }

    public void setHeader(String name, String value) {
        headers.put(name, value);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public String getBodyAsString() {
        if (body == null || body.length == 0) {
            return "";
        }
        return new String(body, StandardCharsets.UTF_8);
    }

    public void setBody(String bodyString) {
        if (bodyString == null) {
            this.body = new byte[0];
        } else {
            this.body = bodyString.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Override
    public String toString() {
        return method + " " + uri + " " + version;
    }
}
