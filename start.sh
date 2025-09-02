#!/bin/bash

echo "🚀 启动 Dial Test Center 项目..."

# 检查Java版本
echo "📋 检查Java版本..."
java -version 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ 错误: 未找到Java或JDK 21"
    echo "请安装JDK 21并确保JAVA_HOME环境变量已设置"
    exit 1
fi

# 检查Node.js版本
echo "📋 检查Node.js版本..."
node --version 2>/dev/null
if [ $? -ne 0 ]; then
    echo "❌ 错误: 未找到Node.js"
    echo "请安装Node.js 18+"
    exit 0
fi

# 检查PostgreSQL
echo "📋 检查PostgreSQL..."
pg_isready -h localhost -p 5432 2>/dev/null
if [ $? -ne 0 ]; then
    echo "⚠️  警告: PostgreSQL未运行或无法连接"
    echo "请确保PostgreSQL服务正在运行，并创建数据库 'dialtestcenter'"
    echo "可以使用以下命令创建数据库:"
    echo "  createdb dialtestcenter"
fi

# 启动后端服务
echo "🔧 启动后端服务..."
cd backend
if [ ! -f "./mvnw" ]; then
    echo "📥 下载Maven Wrapper..."
    mvn -N wrapper:wrapper
fi

echo "🏗️  构建后端项目..."
./mvnw clean compile

echo "🚀 启动Spring Boot应用..."
./mvnw spring-boot:run &
BACKEND_PID=$!

# 等待后端启动
echo "⏳ 等待后端服务启动..."
sleep 10

# 检查后端是否启动成功
if curl -s http://localhost:8080/api/users/health > /dev/null; then
    echo "✅ 后端服务启动成功!"
else
    echo "❌ 后端服务启动失败"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# 启动前端服务
echo "🔧 启动前端服务..."
cd ../frontend

echo "📥 安装前端依赖..."
npm install

echo "🚀 启动前端开发服务器..."
npm run dev &
FRONTEND_PID=$!

# 等待前端启动
echo "⏳ 等待前端服务启动..."
sleep 5

# 检查前端是否启动成功
if curl -s http://localhost:3000 > /dev/null; then
    echo "✅ 前端服务启动成功!"
else
    echo "❌ 前端服务启动失败"
    kill $FRONTEND_PID 2>/dev/null
    exit 1
fi

echo ""
echo "🎉 Dial Test Center 启动完成!"
echo ""
echo "📱 前端地址: http://localhost:3000"
echo "🔧 后端地址: http://localhost:8080/api"
echo "📊 健康检查: http://localhost:8080/api/users/health"
echo ""
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
trap "echo '🛑 正在停止服务...'; kill $BACKEND_PID $FRONTEND_PID 2>/dev/null; exit 0" INT
wait
