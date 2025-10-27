@echo off
REM HTTP Socket Server API测试脚本 (Windows版本)
REM 使用curl命令测试服务器的注册和登录功能

setlocal enabledelayedexpansion

REM 服务器地址
set SERVER=http://localhost:8080

echo =========================================
echo HTTP Socket Server API 测试脚本
echo =========================================
echo.

REM 测试1: 注册新用户
echo 测试1: 注册新用户
echo POST %SERVER%/api/register
echo Body: {"username":"alice","password":"alice123"}
echo.

curl -s -X POST %SERVER%/api/register -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"alice123\"}"
echo.
echo.

REM 测试2: 尝试重复注册（应该失败）
echo 测试2: 尝试重复注册（应该失败）
echo POST %SERVER%/api/register
echo Body: {"username":"alice","password":"alice123"}
echo.

curl -s -X POST %SERVER%/api/register -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"alice123\"}"
echo.
echo.

REM 测试3: 登录用户
echo 测试3: 登录用户
echo POST %SERVER%/api/login
echo Body: {"username":"alice","password":"alice123"}
echo.

curl -s -X POST %SERVER%/api/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"alice123\"}"
echo.
echo.

REM 测试4: 使用错误密码登录（应该失败）
echo 测试4: 使用错误密码登录（应该失败）
echo POST %SERVER%/api/login
echo Body: {"username":"alice","password":"wrongpassword"}
echo.

curl -s -X POST %SERVER%/api/login -H "Content-Type: application/json" -d "{\"username\":\"alice\",\"password\":\"wrongpassword\"}"
echo.
echo.

REM 测试5: 注册第二个用户
echo 测试5: 注册第二个用户
echo POST %SERVER%/api/register
echo Body: {"username":"bob","password":"bob123"}
echo.

curl -s -X POST %SERVER%/api/register -H "Content-Type: application/json" -d "{\"username\":\"bob\",\"password\":\"bob123\"}"
echo.
echo.

REM 测试6: 登录第二个用户
echo 测试6: 登录第二个用户
echo POST %SERVER%/api/login
echo Body: {"username":"bob","password":"bob123"}
echo.

curl -s -X POST %SERVER%/api/login -H "Content-Type: application/json" -d "{\"username\":\"bob\",\"password\":\"bob123\"}"
echo.
echo.

REM 测试7: 获取静态资源
echo 测试7: 获取静态资源
echo GET %SERVER%/index.html
echo.

curl -s -o nul -w "HTTP状态码: %%{http_code}" %SERVER%/index.html
echo.
echo.

REM 测试8: 获取不存在的资源（应该返回404）
echo 测试8: 获取不存在的资源（应该返回404）
echo GET %SERVER%/nonexistent.html
echo.

curl -s -o nul -w "HTTP状态码: %%{http_code}" %SERVER%/nonexistent.html
echo.
echo.

REM 测试9: 测试用户名太短（应该返回400）
echo 测试9: 测试用户名太短（应该返回400）
echo POST %SERVER%/api/register
echo Body: {"username":"ab","password":"password123"}
echo.

curl -s -o nul -w "HTTP状态码: %%{http_code}" -X POST %SERVER%/api/register -H "Content-Type: application/json" -d "{\"username\":\"ab\",\"password\":\"password123\"}"
echo.
echo.

REM 测试10: 测试密码太短（应该返回400）
echo 测试10: 测试密码太短（应该返回400）
echo POST %SERVER%/api/register
echo Body: {"username":"charlie","password":"12345"}
echo.

curl -s -o nul -w "HTTP状态码: %%{http_code}" -X POST %SERVER%/api/register -H "Content-Type: application/json" -d "{\"username\":\"charlie\",\"password\":\"12345\"}"
echo.
echo.

echo =========================================
echo 测试完成
echo =========================================

pause
