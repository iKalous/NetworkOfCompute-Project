@echo off
echo =========================================
echo 测试命令行客户端
echo =========================================
echo.
echo 启动CLI客户端...
echo.
echo 请按照以下步骤测试：
echo.
echo 1. 输入URL: http://localhost:8080/api/register
echo 2. 输入Method: POST
echo 3. 输入Headers:
echo    Content-Type: application/json
echo    (然后按Enter输入空行)
echo 4. 输入Body:
echo    {"username":"bob","password":"bob123"}
echo    (然后按Enter输入空行)
echo.
echo 5. 查看响应，应该显示注册成功
echo.
echo 6. 输入 exit 退出客户端
echo.
pause
mvn exec:java "-Dexec.mainClass=com.http.client.ClientMain"
