# Implementation Plan

- [x] 1. 创建项目结构和基础配置





  - 创建Maven/Gradle项目配置文件，设置JDK 17
  - 创建包结构：protocol、server、client、util
  - 创建resources/static目录用于静态资源
  - _Requirements: 所有需求的基础_

- [x] 2. 实现HTTP协议核心组件





  - _Requirements: 1.1, 1.5, 2.1, 4.3, 5.1, 5.9_

- [x] 2.1 实现HttpStatus枚举


  - 定义所有需要的状态码（200, 301, 302, 304, 400, 401, 404, 405, 500, 503）
  - 包含状态码和状态消息的映射
  - _Requirements: 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_

- [x] 2.2 实现MimeType工具类


  - 创建文件扩展名到MIME类型的映射
  - 支持.html, .txt, .json, .png
  - 提供getByExtension方法
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.8_

- [x] 2.3 实现HttpRequest类


  - 定义请求属性（method, uri, version, headers, body）
  - 实现parse方法从InputStream解析HTTP请求
  - 实现toBytes方法构建请求字节数组
  - 处理请求行、请求头和请求体的解析
  - _Requirements: 1.1, 1.4, 4.3_

- [x] 2.4 实现HttpResponse类


  - 定义响应属性（statusCode, statusMessage, version, headers, body）
  - 实现parse方法从InputStream解析HTTP响应
  - 实现toBytes方法构建响应字节数组
  - 处理状态行、响应头和响应体的构建
  - _Requirements: 2.1, 5.1, 5.9_


- [x] 2.5 编写HTTP协议组件单元测试

  - 测试HttpRequest的解析和构建
  - 测试HttpResponse的解析和构建
  - 测试MimeType映射正确性
  - _Requirements: 2.1, 4.3, 5.1, 7.5_

- [x] 3. 实现用户管理模块





  - _Requirements: 8.6, 8.7, 9.3, 9.4, 9.7, 9.8_

- [x] 3.1 实现User和Session数据模型


  - 创建User类（username, passwordHash, createdAt）
  - 创建Session类（token, username, createdAt, lastAccessTime）
  - _Requirements: 8.6, 9.8_


- [x] 3.2 实现UserRegistry类

  - 使用ConcurrentHashMap存储用户和会话
  - 实现register方法（同步，检查用户名重复）
  - 实现login方法（验证用户名密码，生成token）
  - 实现validateSession方法
  - _Requirements: 8.5, 8.6, 9.3, 9.4, 9.7, 9.8, 10.3_

- [x] 3.3 编写UserRegistry单元测试


  - 测试注册成功和失败场景
  - 测试登录成功和失败场景
  - 测试并发注册的线程安全性
  - _Requirements: 8.5, 8.6, 9.3, 9.4, 10.3_

- [x] 4. 实现简单的JSON工具类





  - _Requirements: 8.2, 8.7, 9.2, 9.7_

- [x] 4.1 实现JsonParser工具类


  - 实现简单的JSON解析方法（解析{"key":"value"}格式）
  - 实现JSON构建方法（构建响应JSON字符串）
  - 支持注册和登录所需的JSON格式
  - _Requirements: 8.2, 8.7, 9.2, 9.7_

- [x] 5. 实现HTTP服务器核心框架





  - _Requirements: 4.1, 4.2, 6.1, 10.1, 10.2_

- [x] 5.1 实现HttpServer类


  - 创建ServerSocket监听指定端口
  - 创建固定大小线程池（如20个线程）
  - 实现start方法（接受连接循环）
  - 实现stop方法（关闭服务器和线程池）
  - 为每个连接创建ConnectionHandler任务
  - _Requirements: 4.1, 4.2, 10.1, 10.2_

- [x] 5.2 实现ConnectionHandler类


  - 实现Runnable接口
  - 实现长连接循环（读取请求直到连接关闭）
  - 设置Socket超时（30秒）
  - 检查Connection头决定是否保持连接
  - 调用RequestRouter处理请求
  - 发送响应
  - _Requirements: 4.2, 6.1, 6.2, 6.3, 6.4, 6.5, 6.6_

- [x] 5.3 实现RequestRouter类


  - 维护路径到RequestHandler的映射
  - 实现registerHandler方法
  - 实现route方法（匹配路径并调用handler）
  - 处理未匹配路径（返回404）
  - _Requirements: 4.3, 5.6_

- [x] 5.4 定义RequestHandler接口


  - 定义handle方法签名
  - _Requirements: 4.3_

- [x] 6. 实现静态资源处理





  - _Requirements: 4.4, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

- [x] 6.1 实现StaticResourceHandler类


  - 实现RequestHandler接口
  - 从指定根目录读取文件
  - 根据文件扩展名设置Content-Type
  - 文件存在返回200，不存在返回404
  - 读取文件内容到响应body
  - _Requirements: 4.4, 7.1, 7.2, 7.3, 7.4, 7.5, 7.6, 7.7, 7.8_

- [x] 6.2 创建示例静态资源文件


  - 创建resources/static/index.html（简单HTML页面）
  - 创建resources/static/data.json（示例JSON数据）
  - 创建resources/static/test.txt（纯文本文件）
  - 添加resources/static/logo.png（PNG图片）
  - _Requirements: 7.1, 7.2, 7.3, 7.4, 7.7_

- [x] 7. 实现API端点处理器




  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7, 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

- [x] 7.1 实现RegisterHandler类





  - 实现RequestHandler接口
  - 验证请求方法为POST，否则返回405
  - 解析请求body的JSON（username, password）
  - 验证username长度（3-20字符）
  - 验证password长度（至少6字符）
  - 调用UserRegistry.register
  - 返回JSON响应（成功200，失败400）
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [x] 7.2 实现LoginHandler类


  - 实现RequestHandler接口
  - 验证请求方法为POST，否则返回405
  - 解析请求body的JSON（username, password）
  - 调用UserRegistry.login
  - 返回JSON响应（成功200带token，失败401）
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7_

- [x] 8. 实现服务器错误处理和主程序





  - _Requirements: 4.6, 5.6, 5.7, 5.8, 10.4_

- [x] 8.1 在ConnectionHandler中添加异常处理


  - 捕获请求解析错误，返回400
  - 捕获所有未处理异常，返回500
  - 记录错误日志到控制台
  - _Requirements: 5.8_

- [x] 8.2 在RequestRouter中处理方法不支持


  - 检查请求方法是否为GET或POST
  - 不支持的方法返回405
  - _Requirements: 4.4, 4.6, 5.7_


- [x] 8.3 创建ServerMain主类

  - 创建UserRegistry实例
  - 创建RequestRouter并注册所有handler
  - 注册/api/register -> RegisterHandler
  - 注册/api/login -> LoginHandler
  - 设置StaticResourceHandler为默认handler
  - 创建HttpServer实例并启动
  - 添加优雅关闭钩子
  - _Requirements: 4.1, 8.1, 9.1_

- [x] 8.4 编写服务器集成测试


  - 启动服务器
  - 使用Socket发送原始HTTP请求
  - 验证GET静态资源返回200
  - 验证POST注册和登录功能
  - 验证404和405错误
  - _Requirements: 4.4, 5.2, 5.6, 5.7, 8.7, 9.7_

- [x] 9. 实现HTTP客户端核心功能





  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 2.1, 2.2, 2.3, 2.4_

- [x] 9.1 实现HttpClient类


  - 实现send方法（发送请求，接收响应）
  - 创建Socket连接到目标服务器
  - 设置连接超时（10秒）和读取超时（30秒）
  - 发送HttpRequest字节数组
  - 读取并解析HttpResponse
  - 关闭Socket连接
  - _Requirements: 1.2, 1.3, 2.1_


- [x] 9.2 实现客户端重定向处理


  - 在HttpClient中实现sendWithRedirect方法
  - 检查响应状态码301、302、304
  - 提取Location头
  - 递归发送新请求（限制最大5次）
  - 304状态特殊处理（不请求body）
  - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 9.3 定义ClientInterface接口


  - 定义start方法
  - 定义displayResponse方法
  - _Requirements: 2.2_

- [x] 9.4 编写HttpClient单元测试


  - 测试基本请求发送
  - 测试重定向处理逻辑
  - 测试超时处理
  - _Requirements: 1.2, 3.1, 3.2, 3.3_

- [x] 10. 实现命令行客户端界面





  - _Requirements: 1.1, 1.4, 2.2, 2.3, 2.4_

- [x] 10.1 实现CliClient类


  - 实现ClientInterface接口
  - 使用Scanner读取用户输入
  - 实现命令循环（输入URL、方法、头、body）
  - 调用HttpClient发送请求
  - 实现displayResponse方法（格式化输出状态码、头、body）
  - 支持退出命令
  - _Requirements: 1.1, 1.4, 2.2, 2.3, 2.4_

- [x] 10.2 创建ClientMain主类


  - 创建HttpClient实例
  - 创建CliClient实例
  - 启动客户端界面
  - _Requirements: 1.1, 2.2_

- [x] 11. 实现GUI客户端界面（可选）




  - _Requirements: 2.2, 2.5_

- [x] 11.1 实现GuiClient类


  - 继承JFrame，实现ClientInterface接口
  - 创建输入面板（URL输入框、方法下拉框、请求头文本区、请求体文本区）
  - 创建发送按钮
  - 创建响应显示面板（状态码标签、响应头文本区、响应体文本区）
  - 实现按钮点击事件（调用HttpClient发送请求）
  - 实现displayResponse方法（更新GUI组件）
  - _Requirements: 2.2, 2.5_

- [x] 11.2 更新ClientMain支持GUI模式


  - 添加命令行参数选择CLI或GUI模式
  - GUI模式下创建GuiClient实例
  - _Requirements: 2.2, 2.5_

- [x] 12. 端到端测试和文档





  - _Requirements: 所有需求_

- [x] 12.1 编写端到端测试场景


  - 启动服务器
  - 使用客户端测试完整流程
  - 测试注册 -> 登录 -> 访问静态资源
  - 测试长连接（多个请求）
  - 测试并发客户端
  - _Requirements: 6.6, 8.7, 9.7, 10.1, 10.2_


- [x] 12.2 创建README文档

  - 项目介绍和功能说明
  - 编译和运行指南
  - 服务器启动方法
  - 客户端使用方法
  - API接口文档（注册和登录）
  - 测试方法说明
  - _Requirements: 所有需求_



- [ ] 12.3 创建示例使用脚本
  - 创建curl命令示例（测试注册和登录）
  - 创建Postman collection（可选）
  - _Requirements: 8.1, 9.1_
