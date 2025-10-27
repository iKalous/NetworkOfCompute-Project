# HTTP Socket Server - 使用示例

本目录包含HTTP Socket Server的使用示例和测试脚本。

## 文件说明

### 1. test-api.sh (Linux/Mac)

Bash脚本，使用curl命令测试服务器的所有API功能。

**使用方法**:

```bash
# 确保服务器正在运行
# 在另一个终端启动服务器:
# mvn exec:java -Dexec.mainClass="com.http.server.ServerMain"

# 给脚本添加执行权限
chmod +x test-api.sh

# 运行测试脚本
./test-api.sh
```

**测试内容**:
- ✓ 注册新用户
- ✓ 重复注册（验证错误处理）
- ✓ 用户登录
- ✓ 错误密码登录（验证错误处理）
- ✓ 注册第二个用户
- ✓ 登录第二个用户
- ✓ 获取静态资源
- ✓ 获取不存在的资源（404）
- ✓ 用户名太短（400）
- ✓ 密码太短（400）

### 2. test-api.bat (Windows)

Windows批处理脚本，功能与test-api.sh相同。

**使用方法**:

```cmd
REM 确保服务器正在运行
REM 在另一个命令提示符窗口启动服务器:
REM mvn exec:java -Dexec.mainClass="com.http.server.ServerMain"

REM 运行测试脚本
test-api.bat
```

### 3. HTTP-Socket-Server.postman_collection.json

Postman集合文件，包含所有API端点的预配置请求。

**使用方法**:

1. 打开Postman应用
2. 点击 "Import" 按钮
3. 选择 `HTTP-Socket-Server.postman_collection.json` 文件
4. 导入后会看到三个文件夹：
   - **User Management**: 用户注册和登录相关请求
   - **Static Resources**: 静态资源访问请求
   - **Error Scenarios**: 错误场景测试请求

5. 确保服务器正在运行（默认端口8080）
6. 选择任意请求并点击 "Send" 发送

**集合变量**:
- `baseUrl`: http://localhost:8080 （可以修改为其他地址）

## 快速开始

### 步骤1: 启动服务器

```bash
# 编译项目
mvn clean compile

# 启动服务器（默认端口8080）
mvn exec:java -Dexec.mainClass="com.http.server.ServerMain"

# 或指定端口
mvn exec:java -Dexec.mainClass="com.http.server.ServerMain" -Dexec.args="9090"
```

### 步骤2: 运行测试脚本

**Linux/Mac**:
```bash
cd examples
chmod +x test-api.sh
./test-api.sh
```

**Windows**:
```cmd
cd examples
test-api.bat
```

### 步骤3: 手动测试（使用curl）

#### 注册用户

```bash
curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}'
```

**预期响应**:
```json
{"success":true,"message":"Registration successful"}
```

#### 登录用户

```bash
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}'
```

**预期响应**:
```json
{"success":true,"message":"Login successful","token":"uuid-token-here"}
```

#### 获取静态资源

```bash
# HTML文件
curl http://localhost:8080/index.html

# JSON文件
curl http://localhost:8080/data.json

# 文本文件
curl http://localhost:8080/test.txt

# 查看响应头
curl -i http://localhost:8080/index.html
```

## 使用Postman测试

### 导入集合

1. 启动Postman
2. 点击左上角的 "Import"
3. 选择 "File" 标签
4. 浏览并选择 `HTTP-Socket-Server.postman_collection.json`
5. 点击 "Import"

### 测试流程

推荐按以下顺序测试：

1. **User Management** > **Register User - Success**
   - 注册一个新用户
   - 验证返回200状态码和成功消息

2. **User Management** > **Register User - Duplicate**
   - 尝试注册相同用户名
   - 验证返回400状态码

3. **User Management** > **Login User - Success**
   - 使用注册的用户登录
   - 验证返回200状态码和token

4. **User Management** > **Login User - Wrong Password**
   - 使用错误密码登录
   - 验证返回401状态码

5. **Static Resources** > **Get HTML File**
   - 获取HTML文件
   - 验证Content-Type为text/html

6. **Static Resources** > **Get Nonexistent File - 404**
   - 请求不存在的文件
   - 验证返回404状态码

7. **Error Scenarios** > **Unsupported Method - PUT**
   - 使用不支持的HTTP方法
   - 验证返回405状态码

## 常见问题

### Q: curl命令不可用

**A**: 需要安装curl工具

**Linux**:
```bash
sudo apt-get install curl  # Ubuntu/Debian
sudo yum install curl      # CentOS/RHEL
```

**Mac**:
```bash
brew install curl
```

**Windows**:
- Windows 10/11自带curl
- 或从 https://curl.se/download.html 下载

### Q: 连接被拒绝

**A**: 确保服务器正在运行

```bash
# 检查服务器是否在运行
netstat -an | grep 8080    # Linux/Mac
netstat -an | findstr 8080 # Windows
```

### Q: 端口已被占用

**A**: 使用不同的端口启动服务器

```bash
mvn exec:java -Dexec.mainClass="com.http.server.ServerMain" -Dexec.args="9090"
```

然后修改脚本中的端口号：
```bash
# test-api.sh
SERVER="http://localhost:9090"
```

### Q: Postman导入失败

**A**: 确保使用的是Postman v2.1格式，或尝试：
1. 复制JSON文件内容
2. 在Postman中选择 "Import" > "Raw text"
3. 粘贴内容并导入

## 高级用法

### 并发测试

使用GNU Parallel或xargs进行并发测试：

```bash
# 并发发送10个注册请求
seq 1 10 | parallel -j 10 'curl -X POST http://localhost:8080/api/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"user{}\",\"password\":\"pass{}\"}"'
```

### 性能测试

使用Apache Bench (ab)进行性能测试：

```bash
# 安装ab
sudo apt-get install apache2-utils  # Ubuntu/Debian

# 发送1000个请求，并发10个
ab -n 1000 -c 10 http://localhost:8080/index.html
```

### 使用HTTPie

HTTPie是一个更友好的HTTP客户端：

```bash
# 安装
pip install httpie

# 注册用户
http POST localhost:8080/api/register username=alice password=alice123

# 登录
http POST localhost:8080/api/login username=alice password=alice123

# 获取静态资源
http GET localhost:8080/index.html
```

## 参考资料

- [curl文档](https://curl.se/docs/)
- [Postman文档](https://learning.postman.com/docs/)
- [HTTPie文档](https://httpie.io/docs)
- [Apache Bench文档](https://httpd.apache.org/docs/2.4/programs/ab.html)
