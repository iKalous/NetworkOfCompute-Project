package com.http.client;

import com.http.protocol.HttpResponse;

/**
 * 客户端界面接口
 * 定义客户端的基本操作
 */
public interface ClientInterface {
    
    /**
     * 启动客户端界面
     */
    void start();
    
    /**
     * 显示HTTP响应
     * @param response HTTP响应对象
     */
    void displayResponse(HttpResponse response);
}
