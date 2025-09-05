# 拨测控制中心用户角色管理实现

基于设计文档生成的完整用户角色管理系统实现。

## 功能特性

- ✅ 用户-角色关系数据管理
- ✅ 用户角色管理页面
- ✅ 权限验证和拦截机制
- ✅ 内置管理员账号初始化
- ✅ REST API接口
- ✅ 前端React组件
- ✅ 完整的测试用例

## 技术架构

### 后端技术栈
- Spring Boot 3.4.5
- Spring Data JPA
- PostgreSQL
- JUnit 4 + Mockito

### 前端技术栈
- React 18 + TypeScript
- Ant Design
- Vite

## 项目结构

```
backend/src/main/java/com/dialtest/center/
├── annotation/
│   └── RequireRole.java              # 权限控制注解
├── config/
│   ├── AdminInitializer.java         # 管理员初始化器
│   └── WebConfig.java                # Web配置类
├── controller/
│   └── UserRoleController.java       # 用户角色控制器
├── entity/
│   ├── Role.java                     # 角色枚举
│   └── UserRole.java                 # 用户角色实体
├── interceptor/
│   └── PermissionInterceptor.java    # 权限拦截器
├── repository/
│   └── UserRoleRepository.java       # 数据访问层
└── service/
    └── UserRoleService.java          # 业务服务层

frontend/src/
├── components/
│   ├── UserRoleForm.tsx              # 用户角色表单组件
│   └── UserRoleManagement.tsx        # 用户角色管理页面
├── hooks/
│   └── usePermission.ts              # 权限检查Hook
├── services/
│   └── userRoleService.ts            # 前端服务类
└── types/
    └── userRole.ts                   # 类型定义
```

## 快速开始

### 1. 数据库初始化

```bash
# 创建数据库
createdb dialtestcenter

# 执行初始化脚本
psql -d dialtestcenter -f database/init.sql
```

### 2. 启动后端服务

```bash
cd backend
./mvnw spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动。

### 3. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端服务将在 `http://localhost:3000` 启动。

## API接口

### 用户角色管理API

| 方法 | 路径 | 描述 | 权限要求 |
|------|------|------|----------|
| GET | `/api/user-roles` | 获取用户角色列表 | ADMIN/OPERATOR/BROWSER |
| POST | `/api/user-roles` | 创建用户角色 | ADMIN |
| PUT | `/api/user-roles/{id}` | 更新用户角色 | ADMIN |
| DELETE | `/api/user-roles/{id}` | 删除用户角色 | ADMIN |
| POST | `/api/user-roles/check-permission` | 检查用户权限 | 无 |

### 请求头

所有API请求需要包含用户名信息：
```
X-Username: admin
```

## 角色权限说明

| 角色 | 描述 | 权限 |
|------|------|------|
| ADMIN | 管理员 | 拥有所有权限 |
| OPERATOR | 操作员 | 可以执行拨测任务相关的所有操作 |
| BROWSER | 浏览者 | 仅查看 |
| EXECUTOR | 执行机 | 执行机注册使用 |

## 配置说明

### 后端配置 (application.properties)

```properties
# 权限管理配置
app.admin.default-username=admin
app.admin.auto-create=true

# 跨域配置
spring.web.cors.allowed-origins=http://localhost:3000,http://localhost:5173
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

### 数据库配置

```properties
# 数据库配置 - PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/dialtestcenter
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.postgresql.Driver
```

## 使用示例

### 1. 权限注解使用

```java
@RequireRole({Role.ADMIN, Role.OPERATOR})
@GetMapping("/api/dialtest/tasks")
public ResponseEntity<List<Task>> getTasks() {
    // 只有ADMIN或OPERATOR可以访问
}
```

### 2. 前端权限检查

```typescript
const { canManageUsers, hasAdminRole } = usePermission();
const currentUserRoles = ['ADMIN'];

if (canManageUsers(currentUserRoles)) {
    // 显示管理功能
}
```

### 3. 创建用户角色

```typescript
const userRoleData = {
  username: 'newuser',
  role: 'OPERATOR'
};

await UserRoleService.createUserRole(userRoleData);
```

## 测试

### 运行后端测试

```bash
cd backend
./mvnw test
```

### 运行前端测试

```bash
cd frontend
npm test
```

## 部署说明

### 生产环境配置

1. 修改数据库连接配置
2. 设置正确的跨域域名
3. 配置日志级别
4. 设置安全认证机制

### Docker部署

```dockerfile
# 后端Dockerfile
FROM openjdk:21-jdk-slim
COPY target/dial-test-center-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 注意事项

1. **认证机制**：当前实现使用请求头传递用户名，生产环境需要集成完整的认证系统
2. **权限缓存**：建议在生产环境中添加权限缓存机制
3. **审计日志**：可以扩展添加操作审计日志功能
4. **多租户支持**：当前设计支持后续扩展多租户功能

## 扩展功能

- [ ] 集成JWT认证
- [ ] 添加权限缓存
- [ ] 实现操作审计日志
- [ ] 支持多租户
- [ ] 添加权限管理页面
- [ ] 实现角色继承机制

## 故障排除

### 常见问题

1. **数据库连接失败**
   - 检查PostgreSQL服务是否启动
   - 验证数据库连接配置

2. **权限验证失败**
   - 检查请求头是否包含正确的用户名
   - 验证用户角色是否正确配置

3. **跨域问题**
   - 检查CORS配置
   - 确认前端域名在允许列表中

## 联系支持

如有问题，请查看设计文档或联系开发团队。
