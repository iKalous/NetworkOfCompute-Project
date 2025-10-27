#!/bin/bash

# HTTP Socket Server API测试脚本
# 使用curl命令测试服务器的注册和登录功能

# 服务器地址
SERVER="http://localhost:8080"

echo "========================================="
echo "HTTP Socket Server API 测试脚本"
echo "========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试1: 注册新用户
echo -e "${YELLOW}测试1: 注册新用户${NC}"
echo "POST $SERVER/api/register"
echo "Body: {\"username\":\"alice\",\"password\":\"alice123\"}"
echo ""

RESPONSE=$(curl -s -X POST $SERVER/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}')

echo "响应: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ 注册成功${NC}"
else
    echo -e "${RED}✗ 注册失败${NC}"
fi
echo ""

# 测试2: 尝试重复注册（应该失败）
echo -e "${YELLOW}测试2: 尝试重复注册（应该失败）${NC}"
echo "POST $SERVER/api/register"
echo "Body: {\"username\":\"alice\",\"password\":\"alice123\"}"
echo ""

RESPONSE=$(curl -s -X POST $SERVER/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}')

echo "响应: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q '"success":false'; then
    echo -e "${GREEN}✓ 正确返回失败（用户名已存在）${NC}"
else
    echo -e "${RED}✗ 应该返回失败${NC}"
fi
echo ""

# 测试3: 登录用户
echo -e "${YELLOW}测试3: 登录用户${NC}"
echo "POST $SERVER/api/login"
echo "Body: {\"username\":\"alice\",\"password\":\"alice123\"}"
echo ""

RESPONSE=$(curl -s -X POST $SERVER/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"alice123"}')

echo "响应: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q '"token"'; then
    TOKEN=$(echo "$RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
    echo -e "${GREEN}✓ 登录成功${NC}"
    echo "Token: $TOKEN"
else
    echo -e "${RED}✗ 登录失败${NC}"
fi
echo ""

# 测试4: 使用错误密码登录（应该失败）
echo -e "${YELLOW}测试4: 使用错误密码登录（应该失败）${NC}"
echo "POST $SERVER/api/login"
echo "Body: {\"username\":\"alice\",\"password\":\"wrongpassword\"}"
echo ""

RESPONSE=$(curl -s -X POST $SERVER/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"alice","password":"wrongpassword"}')

echo "响应: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q '"success":false'; then
    echo -e "${GREEN}✓ 正确返回失败（密码错误）${NC}"
else
    echo -e "${RED}✗ 应该返回失败${NC}"
fi
echo ""

# 测试5: 注册第二个用户
echo -e "${YELLOW}测试5: 注册第二个用户${NC}"
echo "POST $SERVER/api/register"
echo "Body: {\"username\":\"bob\",\"password\":\"bob123\"}"
echo ""

RESPONSE=$(curl -s -X POST $SERVER/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"bob123"}')

echo "响应: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ 注册成功${NC}"
else
    echo -e "${RED}✗ 注册失败${NC}"
fi
echo ""

# 测试6: 登录第二个用户
echo -e "${YELLOW}测试6: 登录第二个用户${NC}"
echo "POST $SERVER/api/login"
echo "Body: {\"username\":\"bob\",\"password\":\"bob123\"}"
echo ""

RESPONSE=$(curl -s -X POST $SERVER/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"bob","password":"bob123"}')

echo "响应: $RESPONSE"
echo ""

if echo "$RESPONSE" | grep -q '"token"'; then
    echo -e "${GREEN}✓ 登录成功${NC}"
else
    echo -e "${RED}✗ 登录失败${NC}"
fi
echo ""

# 测试7: 获取静态资源
echo -e "${YELLOW}测试7: 获取静态资源${NC}"
echo "GET $SERVER/index.html"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $SERVER/index.html)

echo "HTTP状态码: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" = "200" ]; then
    echo -e "${GREEN}✓ 成功获取静态资源${NC}"
else
    echo -e "${RED}✗ 获取静态资源失败${NC}"
fi
echo ""

# 测试8: 获取不存在的资源（应该返回404）
echo -e "${YELLOW}测试8: 获取不存在的资源（应该返回404）${NC}"
echo "GET $SERVER/nonexistent.html"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $SERVER/nonexistent.html)

echo "HTTP状态码: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" = "404" ]; then
    echo -e "${GREEN}✓ 正确返回404${NC}"
else
    echo -e "${RED}✗ 应该返回404${NC}"
fi
echo ""

# 测试9: 测试用户名太短（应该返回400）
echo -e "${YELLOW}测试9: 测试用户名太短（应该返回400）${NC}"
echo "POST $SERVER/api/register"
echo "Body: {\"username\":\"ab\",\"password\":\"password123\"}"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST $SERVER/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"ab","password":"password123"}')

echo "HTTP状态码: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ 正确返回400（用户名太短）${NC}"
else
    echo -e "${RED}✗ 应该返回400${NC}"
fi
echo ""

# 测试10: 测试密码太短（应该返回400）
echo -e "${YELLOW}测试10: 测试密码太短（应该返回400）${NC}"
echo "POST $SERVER/api/register"
echo "Body: {\"username\":\"charlie\",\"password\":\"12345\"}"
echo ""

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST $SERVER/api/register \
  -H "Content-Type: application/json" \
  -d '{"username":"charlie","password":"12345"}')

echo "HTTP状态码: $HTTP_CODE"
echo ""

if [ "$HTTP_CODE" = "400" ]; then
    echo -e "${GREEN}✓ 正确返回400（密码太短）${NC}"
else
    echo -e "${RED}✗ 应该返回400${NC}"
fi
echo ""

echo "========================================="
echo "测试完成"
echo "========================================="
