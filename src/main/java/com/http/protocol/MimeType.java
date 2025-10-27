package com.http.protocol;

import java.util.Map;

/**
 * MIME类型工具类
 * 提供文件扩展名到MIME类型的映射
 */
public class MimeType {
    
    private static final Map<String, String> MIME_TYPES = Map.of(
        ".html", "text/html",
        ".txt", "text/plain",
        ".json", "application/json",
        ".png", "image/png"
    );
    
    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    /**
     * 根据文件名获取对应的MIME类型
     * @param filename 文件名
     * @return MIME类型字符串，如果无法确定则返回默认类型
     */
    public static String getByExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return DEFAULT_MIME_TYPE;
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return DEFAULT_MIME_TYPE;
        }
        
        String extension = filename.substring(lastDotIndex).toLowerCase();
        return MIME_TYPES.getOrDefault(extension, DEFAULT_MIME_TYPE);
    }

    /**
     * 获取默认MIME类型
     * @return 默认MIME类型
     */
    public static String getDefault() {
        return DEFAULT_MIME_TYPE;
    }
}
