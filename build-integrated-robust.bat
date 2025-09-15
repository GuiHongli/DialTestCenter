@echo off
chcp 65001 >nul
REM 拨测控制中心 - 健壮的一体化构建脚本 (Windows)
REM 功能：构建前端并集成到后端，实现一体化部署

echo 🚀 开始构建拨测控制中心前后端一体化部署...

REM 检查 Node.js 环境
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到 Node.js，请先安装 Node.js
    pause
    exit /b 1
)

REM 检查 Maven 环境
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo ❌ 错误：未找到 Maven，请先安装 Maven
    pause
    exit /b 1
)

echo ✅ 环境检查通过

REM 1. 清理之前的构建文件
echo 🧹 清理之前的构建文件...
if exist frontend\dist rmdir /s /q frontend\dist
if not exist backend\src\main\resources\static mkdir backend\src\main\resources\static
for /f %%i in ('dir /b backend\src\main\resources\static 2^>nul') do del /q backend\src\main\resources\static\%%i
if exist backend\target rmdir /s /q backend\target

echo ✅ 清理完成

REM 2. 安装前端依赖
echo 📦 安装前端依赖...
cd frontend
if not exist node_modules (
    echo 正在安装前端依赖...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ 前端依赖安装失败
        pause
        exit /b 1
    )
    echo ✅ 前端依赖安装完成
) else (
    echo ✅ 前端依赖已存在，跳过安装
)

REM 3. 构建前端
echo 🔨 构建前端...
npm run build
if %errorlevel% neq 0 (
    echo ❌ 前端构建失败
    pause
    exit /b 1
)
echo ✅ 前端构建成功

REM 4. 检查并处理构建输出
echo 📋 检查构建输出...
set FRONTEND_BUILT=0

REM 检查目标目录中的文件
if exist "..\backend\src\main\resources\static\index.html" (
    echo ✅ index.html 已输出到后端目录
    set FRONTEND_BUILT=1
) else (
    echo ⚠️ index.html 未找到，检查 dist 目录...
    if exist "dist\index.html" (
        echo 从 dist 目录复制 index.html...
        copy "dist\index.html" "..\backend\src\main\resources\static\"
        if %errorlevel% equ 0 (
            echo ✅ index.html 复制成功
            set FRONTEND_BUILT=1
        ) else (
            echo ❌ index.html 复制失败
        )
    ) else (
        echo ❌ dist\index.html 也不存在
    )
)

if exist "..\backend\src\main\resources\static\bundle.js" (
    echo ✅ bundle.js 已输出到后端目录
) else (
    echo ⚠️ bundle.js 未找到，检查 dist 目录...
    if exist "dist\bundle.js" (
        echo 从 dist 目录复制 bundle.js...
        copy "dist\bundle.js" "..\backend\src\main\resources\static\"
        if %errorlevel% equ 0 (
            echo ✅ bundle.js 复制成功
        ) else (
            echo ❌ bundle.js 复制失败
        )
    ) else (
        echo ❌ dist\bundle.js 也不存在
    )
)

REM 如果前端构建失败，尝试继续后端构建
if %FRONTEND_BUILT% equ 0 (
    echo ⚠️ 前端构建可能有问题，但继续后端构建...
)

cd ..

REM 5. 构建后端
echo 🔨 构建后端...
cd backend
echo 正在执行 Maven 构建...
mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo ❌ 后端构建失败
    pause
    exit /b 1
)
echo ✅ 后端构建成功

cd ..

REM 6. 显示构建结果
echo.
echo 🎉 构建完成！
echo.
echo 📁 构建输出：
echo    - 前端静态文件: backend\src\main\resources\static\
if exist backend\src\main\resources\static\index.html (
    echo      ✅ index.html
) else (
    echo      ❌ index.html (缺失)
)
if exist backend\src\main\resources\static\bundle.js (
    echo      ✅ bundle.js
) else (
    echo      ❌ bundle.js (缺失)
)
echo    - 后端 JAR 文件: backend\target\dial-test-center-1.0.0.jar
if exist backend\target\dial-test-center-1.0.0.jar (
    echo      ✅ JAR 文件已生成
) else (
    echo      ❌ JAR 文件未生成
)
echo.
echo 🚀 启动命令：
echo    java -jar backend\target\dial-test-center-1.0.0.jar
echo.
echo 🌐 访问地址：
echo    http://localhost:8080
echo.

REM 7. 询问是否立即启动
set /p choice="是否立即启动应用？(y/n): "
if /i "%choice%"=="y" (
    echo 🚀 启动应用..
)

pause
