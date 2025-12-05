package com.http.protocol;

/**
 * HTTP状态码枚举
enum 是一种枚举类型
 */
public enum HttpStatus {
    // 定义了一些枚举常量
    // 2xx 成功状态码
    OK(200, "OK"),
    // 3xx 重定向状态码
    // 301 永久重定向，资源已永久移动到新位置
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    // 302 临时重定向，资源临时移动到其他位置
    FOUND(302, "Found"),
    // 304 资源未修改，客户端可以使用缓存版本
    NOT_MODIFIED(304, "Not Modified"),
    // 4xx 客户端错误状态码
    // 400 请求语法错误
    BAD_REQUEST(400, "Bad Request"),
    // 未授权，需身份验证
    UNAUTHORIZED(401, "Unauthorized"),
    // 资源未找到， 页面未存在
    NOT_FOUND(404, "Not Found"),
    // 405 请求方法不被允许
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    // 5xx 服务器错误状态码
    // 500 服务器内部错误
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    // 503 服务不可用
    SERVICE_UNAVAILABLE(503, "Service Unavailable");
    // 有这两个类型
    private final int code;
    private final String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
    // 获取两个信息的对外接口
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据状态码获取对应的HttpStatus枚举
     * @param code 状态码
     * @return HttpStatus枚举，如果找不到则返回null
     */
    public static HttpStatus fromCode(int code) {
        for (HttpStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return code + " " + message;
    }
}
