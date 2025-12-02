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
 * HTTP响应类
 * 表示一个HTTP响应消息，包含状态行、响应头和响应体
 */
public class HttpResponse {
    
    private int statusCode;
    private String statusMessage;
    private String version;
    private Map<String, String> headers;
    private byte[] body;

    public HttpResponse() {
        this.headers = new HashMap<>();
        this.body = new byte[0];
        this.version = "HTTP/1.1";
    }

    public HttpResponse(HttpStatus status) {
        this();
        this.statusCode = status.getCode();
        this.statusMessage = status.getMessage();
    }

    public HttpResponse(int statusCode, String statusMessage) {
        this();
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /**
     * 从InputStream解析HTTP响应
     * @param input 输入流
     * @return 解析后的HttpResponse对象
     * @throws IOException 如果读取或解析失败
     */
    public static HttpResponse parse(InputStream input) throws IOException {
        HttpResponse response = new HttpResponse();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        
        // 解析状态行
        // 格式："HTTP/1.1 200 OK\r\n"
        String statusLine = reader.readLine();
        if (statusLine == null || statusLine.isEmpty()) {
            throw new IOException("Invalid HTTP response: empty status line");
        }
        
        String[] statusParts = statusLine.split(" ", 3);
        if (statusParts.length < 2) {
            throw new IOException("Invalid HTTP status line: " + statusLine);
        }
        
        response.version = statusParts[0];
        //把状态码抓出来
        try {
            response.statusCode = Integer.parseInt(statusParts[1]);
        } catch (NumberFormatException e) {
            throw new IOException("Invalid status code: " + statusParts[1]);
        }
        response.statusMessage = statusParts.length > 2 ? statusParts[2] : "";
        
        // 解析响应头
        //格式："Content-Type: text/html\r\n" +
        // "Content-Length: " + body.length() + "\r\n" +
        // "Connection: keep-alive\r\n" +
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String headerName = line.substring(0, colonIndex).trim();
                //trim会移除开头和结尾的所有空格
                String headerValue = line.substring(colonIndex + 1).trim();
                response.headers.put(headerName, headerValue);
            }
        }
        
        // 解析响应体
        String contentLengthStr = response.headers.get("Content-Length");
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
                    response.body = new String(bodyChars, 0, totalRead).getBytes(StandardCharsets.UTF_8);
                }
            } catch (NumberFormatException e) {
                throw new IOException("Invalid Content-Length header: " + contentLengthStr);
            }
        }
        
        return response;
    }

    /**
     * 将HTTP响应转换为字节数组
     * @return 响应的字节数组表示
     */
    public byte[] toBytes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try {
            // 写入状态行
            String statusLine = version + " " + statusCode + " " + statusMessage + "\r\n";
            output.write(statusLine.getBytes(StandardCharsets.UTF_8));
            
            // 写入响应头
            for (Map.Entry<String, String> header : headers.entrySet()) {
                String headerLine = header.getKey() + ": " + header.getValue() + "\r\n";
                output.write(headerLine.getBytes(StandardCharsets.UTF_8));
            }
            
            // 如果有响应体，确保有Content-Length头
            if (body != null && body.length > 0) {
                if (!headers.containsKey("Content-Length")) {
                    String contentLength = "Content-Length: " + body.length + "\r\n";
                    output.write(contentLength.getBytes(StandardCharsets.UTF_8));
                }
            }
            
            // 写入空行（分隔头和体）
            output.write("\r\n".getBytes(StandardCharsets.UTF_8));
            
            // 写入响应体
            if (body != null && body.length > 0) {
                output.write(body);
            }
            
        } catch (IOException e) {
            // ByteArrayOutputStream不会抛出IOException
            throw new RuntimeException(e);
        }
        
        return output.toByteArray();
    }

    // Getters and Setters
    
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public void setStatus(HttpStatus status) {
        this.statusCode = status.getCode();
        this.statusMessage = status.getMessage();
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
        return version + " " + statusCode + " " + statusMessage;
    }
}
