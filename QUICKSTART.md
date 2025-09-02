# Dial Test Center 快速启动指南

## 🚀 一键启动

使用我们提供的启动脚本，可以一键启动整个项目：

```bash
./start.sh
```

这个脚本会自动：
1. 检查环境依赖
2. 启动后端Spring Boot服务
3. 启动前端React开发服务器
4. 显示访问地址

## 📋 环境要求

### 必需软件
- **JDK 21** - Java开发环境
- **Node.js 18+** - 前端开发环境
- **PostgreSQL 12+** - 数据库

### 可选软件
- **Maven 3.8+** - 如果使用IDE开发
- **Git** - 版本控制

## 🔧 手动启动步骤

### 1. 启动后端服务

```bash
cd backend

# 首次运行，下载Maven Wrapper
mvn -N wrapper:wrapper

# 启动Spring Boot应用
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 2. 启动前端服务

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:3000` 启动

### 3. 数据库设置

```bash
# 创建数据库
createdb dialtestcenter

# 初始化表结构（可选）
psql -d dialtestcenter -f database/init.sql
```

## 🌐 访问地址

- **前端应用**: http://localhost:3000
- **后端API**: http://localhost:8080/api
- **健康检查**: http://localhost:8080/api/users/health

## 📱 功能特性

### 前端功能
- ✅ 响应式布局设计
- ✅ 用户管理界面
- ✅ 数据统计展示
- ✅ 现代化UI组件

### 后端功能
- ✅ RESTful API接口
- ✅ 用户CRUD操作
- ✅ 数据验证
- ✅ 健康检查端点

## 🧪 运行测试

### 后端测试
```bash
cd backend
./mvnw test
```

### 前端测试
```bash
cd frontend
npm test
```

## 🐛 常见问题

### 端口被占用
如果8080或3000端口被占用，可以修改配置文件：

- 后端端口：修改 `backend/src/main/resources/application.properties` 中的 `server.port`
- 前端端口：修改 `frontend/vite.config.ts` 中的 `server.port`

### 数据库连接失败
确保PostgreSQL服务正在运行，并检查连接配置：
- 主机：localhost
- 端口：5432
- 数据库：dialtestcenter
- 用户名：postgres
- 密码：password

## 📚 开发指南

### 添加新的API端点
1. 在 `backend/src/main/java/com/dialtest/center/controller` 中创建新的控制器
2. 在 `backend/src/main/java/com/dialtest/center/entity` 中定义实体类
3. 编写相应的测试用例

### 添加新的前端页面
1. 在 `frontend/src/pages` 中创建新的页面组件
2. 在 `frontend/src/components` 中创建可复用组件
3. 在 `frontend/src/App.tsx` 中添加路由

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情
