# 拨测控制中心 - 前后端一体化部署指南

## 概述

本项目已配置为前后端一体化部署模式，前端 React 应用打包后集成到后端 Spring Boot 应用中，通过单一 JAR 文件提供服务。

## 架构说明

### 部署架构
```
┌─────────────────────────────────────┐
│           Spring Boot 应用            │
│  ┌─────────────────────────────────┐ │
│  │       后端 API 服务              │ │
│  │  - 用户管理 API                 │ │
│  │  - 用例集管理 API               │ │
│  │  - 软件包管理 API               │ │
│  │  - 操作日志 API                 │ │
│  └─────────────────────────────────┘ │
│  ┌─────────────────────────────────┐ │
│  │       前端静态资源               │ │
│  │  - index.html                   │ │
│  │  - bundle.js                    │ │
│  │  - CSS/JS 资源                  │ │
│  └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

### 文件结构
```
backend/src/main/resources/static/
├── index.html          # 前端入口页面
├── bundle.js           # 前端打包文件
└── ...                 # 其他静态资源
```

## 构建和部署

### 方法一：使用构建脚本（推荐）

#### Windows 系统
```bash
# 构建并启动
build-integrated.bat

# 仅启动（需要先构建）
start-integrated.bat
```

#### Linux/Mac 系统
```bash
# 构建并启动
./build-integrated.sh

# 仅启动（需要先构建）
./start-integrated.sh
```

### 方法二：手动构建

#### 1. 构建前端
```bash
cd frontend
npm install
npm run build
```

#### 2. 构建后端
```bash
cd backend
mvn clean package -DskipTests
```

#### 3. 启动应用
```bash
java -jar backend/target/dial-test-center-1.0.0.jar
```

## 配置说明

### 前端配置

#### Webpack 配置 (`frontend/webpack.config.js`)
```javascript
output: {
  path: path.resolve(__dirname, '../backend/src/main/resources/static'),
  filename: 'bundle.js',
  clean: true,
  publicPath: '/',
}
```

#### HTML 模板配置
```javascript
new HtmlWebpackPlugin({
  template: './index.html',
  filename: '../backend/src/main/resources/static/index.html',
})
```

### 后端配置

#### Web 配置 (`backend/src/main/java/.../config/WebConfig.java`)
- 静态资源处理：`/static/**` → `classpath:/static/`
- 前端路由处理：所有非 API 请求返回 `index.html`
- 缓存策略：静态资源缓存 3600 秒

#### 安全配置 (`backend/src/main/java/.../config/SecurityConfig.java`)
- 静态资源访问：`/`, `/index.html`, `/static/**` 允许访问
- API 访问：`/api/**` 允许访问
- CORS 配置：支持跨域请求

## 访问方式

### 应用访问
- **URL**: http://localhost:8080
- **前端页面**: 自动加载 React 应用
- **API 接口**: http://localhost:8080/api/...

### API 接口
- **用户管理**: `/api/users/**`
- **用户角色**: `/api/user-roles/**`
- **用例集管理**: `/api/testcasesets/**`
- **软件包管理**: `/api/software-packages/**`
- **操作日志**: `/api/operation-logs/**`

## 开发模式

### 前端开发
```bash
cd frontend
npm run dev
```
- 前端开发服务器：http://localhost:3000
- 后端 API 代理：`/api` → `http://localhost:8080/api`

### 后端开发
```bash
cd backend
mvn spring-boot:run
```
- 后端服务：http://localhost:8080
- 前端静态文件：需要先构建前端

## 部署优势

### 1. 简化部署
- **单一 JAR 文件**：包含前后端所有资源
- **无依赖部署**：只需 Java 运行环境
- **一键启动**：`java -jar` 命令启动

### 2. 运维便利
- **统一端口**：前后端使用同一端口 (8080)
- **统一日志**：前后端日志统一管理
- **统一监控**：应用健康检查统一

### 3. 性能优化
- **静态资源缓存**：浏览器缓存优化
- **CDN 友好**：静态资源可独立缓存
- **压缩优化**：Webpack 自动压缩

## 故障排除

### 常见问题

#### 1. 前端页面无法访问
**症状**: 访问 http://localhost:8080 显示 404
**解决**: 
- 检查 `backend/src/main/resources/static/index.html` 是否存在
- 重新运行构建脚本

#### 2. API 接口无法访问
**症状**: 前端调用 API 返回 404
**解决**:
- 检查后端服务是否正常启动
- 检查 API 路径是否正确 (`/api/...`)

#### 3. 静态资源加载失败
**症状**: 页面样式异常，JS 文件加载失败
**解决**:
- 检查 `backend/src/main/resources/static/` 目录
- 重新构建前端

### 日志查看
```bash
# 查看应用日志
java -jar backend/target/dial-test-center-1.0.0.jar

# 或查看 Spring Boot 日志
tail -f logs/application.log
```

## 生产环境部署

### 1. 环境要求
- **Java**: JDK 8 或更高版本
- **内存**: 建议 2GB 以上
- **磁盘**: 建议 1GB 以上

### 2. 启动命令
```bash
# 基本启动
java -jar dial-test-center-1.0.0.jar

# 指定端口
java -jar dial-test-center-1.0.0.jar --server.port=8080

# 指定配置文件
java -jar dial-test-center-1.0.0.jar --spring.config.location=application-prod.properties

# 后台运行
nohup java -jar dial-test-center-1.0.0.jar > app.log 2>&1 &
```

### 3. 健康检查
```bash
# 应用健康检查
curl http://localhost:8080/actuator/health

# 前端页面检查
curl http://localhost:8080/

# API 接口检查
curl http://localhost:8080/api/users
```

## 总结

通过前后端一体化部署，本项目实现了：
- ✅ **简化部署**：单一 JAR 文件部署
- ✅ **统一服务**：前后端同一端口访问
- ✅ **开发便利**：支持独立开发和一体化部署
- ✅ **运维友好**：统一日志、监控、管理

这种部署方式特别适合中小型项目和企业内部系统，既保持了开发的灵活性，又简化了部署的复杂性。
