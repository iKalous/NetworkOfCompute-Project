# HTTP Socket Server - 交互式测试指南

本指南将引导你完成所有组件的交互式测试。

## 当前状态

✅ **服务器已启动** - 运行在 http://localhost:8080
✅ **curl测试已完成** - 所有API端点工作正常

## 测试进度

### 已完成的测试 ✅

1. ✅ **注册新用户** - alice/alice123 注册成功
2. ✅ **重复注册** - 正确返回错误
3. ✅ **用户登录** - 登录成功并获得token
4. ✅ **错误密码** - 正确拒绝
5. ✅ **获取HTML** - index.html 返回正确
6. ✅ **获取JSON** - data.json 返回正确
7. ✅ **404错误** - 不存在的文件正确返回404

---

## 接下来的测试

### 测试 A: 命令行客户端 (CLI)

**步骤：**

1. 打开一个新的命令提示符窗口
2. 进入项目目录：
   ```cmd
   cd D:\ProjectI\NetworkOfCompute
   ```

3. 启动CLI客户端：
   ```cmd
   mvn exec:java "-Dexec.mainClass=com.http.client.ClientMain"
   ```

4. 等待客户端启动，你会看到：
   ```
   Starting HTTP Client in CLI mode...
   Use 'java ClientMain gui' to start in GUI mode
   
   HTTP Client CLI
   Type 'exit' or 'quit' to exit
   
   Enter URL:
   ```

#### 测试 A1: 注册新用户 (charlie)

5. 输入URL：
   ```
   http://localhost:8080/api/register
   ```

6. 输入Method：
   ```
   POST
   ```

7. 输入Headers（每行一个，输入空行结束）：
   ```
   Content-Type: application/json
   
   ```
   （注意：输入完后按Enter输入空行）

8. 输入Body（输入空行结束）：
   ```
   {"username":"charlie","password":"charlie123"}
   
   ```
   （注意：输入完后按Enter输入空行）

9. **预期结果**：
   ```
   Response:
   Status: 200 OK
   Headers:
   Connection: close
   Content-Type: application/json
   Content-Length: 52
   
   Body:
   {"success":true,"message":"Registration successful"}
   ```

#### 测试 A2: 登录用户 (charlie)

10. 输入URL：
    ```
    http://localhost:8080/api/login
    ```

11. 输入Method：
    ```
    POST
    ```

12. 输入Headers：
    ```
    Content-Type: application/json
    
    ```

13. 输入Body：
    ```
    {"username":"charlie","password":"charlie123"}
    
    ```

14. **预期结果**：应该看到登录成功和token

#### 测试 A3: 获取静态资源

15. 输入URL：
    ```
    http://localhost:8080/test.txt
    ```

16. 输入Method：
    ```
    GET
    ```

17. 输入Headers（直接按Enter输入空行）：
    ```
    
    ```

18. **预期结果**：应该看到test.txt的内容

19. 输入 `exit` 退出客户端

---

### 测试 B: GUI客户端

**步骤：**

1. 打开另一个新的命令提示符窗口
2. 进入项目目录：
   ```cmd
   cd D:\ProjectI\NetworkOfCompute
   ```

3. 启动GUI客户端：
   ```cmd
   mvn exec:java "-Dexec.mainClass=com.http.client.ClientMain" "-Dexec.args=gui"
   ```

4. 等待GUI窗口打开

#### 测试 B1: 注册新用户 (david)

5. 在GUI窗口中：
   - **URL输入框**：输入 `http://localhost:8080/api/register`
   - **Method下拉框**：选择 `POST`
   - **Headers文本区**：输入
     ```
     Content-Type: application/json
     ```
   - **Body文本区**：输入
     ```
     {"username":"david","password":"david123"}
     ```
   - 点击 **Send** 按钮

6. **预期结果**：在响应区域看到：
   ```
   Status: 200 OK
   
   Headers:
   Connection: close
   Content-Type: application/json
   Content-Length: 52
   
   Body:
   {"success":true,"message":"Registration successful"}
   ```

#### 测试 B2: 登录用户 (david)

7. 在GUI窗口中：
   - **URL输入框**：输入 `http://localhost:8080/api/login`
   - **Method下拉框**：选择 `POST`
   - **Headers文本区**：保持
     ```
     Content-Type: application/json
     ```
   - **Body文本区**：输入
     ```
     {"username":"david","password":"david123"}
     ```
   - 点击 **Send** 按钮

8. **预期结果**：看到登录成功和token

#### 测试 B3: 获取PNG图片

9. 在GUI窗口中：
   - **URL输入框**：输入 `http://localhost:8080/logo.png`
   - **Method下拉框**：选择 `GET`
   - **Headers文本区**：清空（或保持空）
   - **Body文本区**：清空
   - 点击 **Send** 按钮

10. **预期结果**：
    ```
    Status: 200 OK
    
    Headers:
    Connection: keep-alive
    Content-Type: image/png
    Content-Length: [图片大小]
    
    Body:
    [二进制数据]
    ```

#### 测试 B4: 测试404错误

11. 在GUI窗口中：
    - **URL输入框**：输入 `http://localhost:8080/notfound.html`
    - **Method下拉框**：选择 `GET`
    - 点击 **Send** 按钮

12. **预期结果**：
    ```
    Status: 404 Not Found
    
    Body:
    404 Not Found
    ```

13. 关闭GUI窗口

---

### 测试 C: 自动化测试脚本

#### Windows测试脚本

1. 打开命令提示符
2. 进入examples目录：
   ```cmd
   cd D:\ProjectI\NetworkOfCompute\examples
   ```

3. 运行测试脚本：
   ```cmd
   test-api.bat
   ```

4. **预期结果**：脚本会自动执行10个测试，显示每个测试的结果

#### Linux/Mac测试脚本（如果适用）

```bash
cd examples
chmod +x test-api.sh
./test-api.sh
```

---

### 测试 D: 并发测试

测试服务器处理多个并发请求的能力。

1. 打开命令提示符
2. 运行端到端测试：
   ```cmd
   mvn test -Dtest=EndToEndTest
   ```

3. **预期结果**：
   - 所有5个测试应该通过
   - 测试3会创建10个并发客户端
   - 所有客户端应该成功完成注册、登录和资源访问

---

## 测试检查清单

完成每个测试后，在相应的框中打勾：

### API测试 (curl)
- [x] 注册新用户
- [x] 重复注册（错误处理）
- [x] 用户登录
- [x] 错误密码（错误处理）
- [x] 获取HTML文件
- [x] 获取JSON文件
- [x] 404错误处理

### CLI客户端测试
- [ ] 注册新用户 (charlie)
- [ ] 登录用户 (charlie)
- [ ] 获取静态资源 (test.txt)

### GUI客户端测试
- [ ] 注册新用户 (david)
- [ ] 登录用户 (david)
- [ ] 获取PNG图片
- [ ] 404错误处理

### 自动化测试
- [ ] Windows批处理脚本
- [ ] 端到端测试套件

### 额外测试
- [ ] 测试用户名太短（应返回400）
- [ ] 测试密码太短（应返回400）
- [ ] 测试不支持的HTTP方法（应返回405）
- [ ] 测试多种MIME类型（HTML, JSON, TXT, PNG）

---

## 故障排除

### 问题1: 连接被拒绝
**解决方案**：确保服务器正在运行
```cmd
netstat -an | findstr 8080
```

### 问题2: 端口已被占用
**解决方案**：使用不同的端口
```cmd
mvn exec:java "-Dexec.mainClass=com.http.server.ServerMain" "-Dexec.args=9090"
```

### 问题3: JSON格式错误
**解决方案**：确保JSON格式正确，使用双引号

### 问题4: Maven命令失败
**解决方案**：确保在项目根目录，并且已编译项目
```cmd
mvn clean compile
```

---

## 测试完成后

1. 停止服务器（在服务器窗口按 Ctrl+C）
2. 查看测试报告：`target/surefire-reports/`
3. 检查服务器日志确认所有请求都被正确处理

---

## 性能测试（可选）

如果想测试服务器性能：

```cmd
REM 安装Apache Bench
REM 然后运行：
ab -n 1000 -c 10 http://localhost:8080/index.html
```

这会发送1000个请求，并发度为10。

---

## 报告问题

如果发现任何问题，请记录：
1. 测试步骤
2. 预期结果
3. 实际结果
4. 错误消息（如果有）
5. 服务器日志

---

**祝测试顺利！** 🚀
