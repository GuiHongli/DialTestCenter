# 前端API响应格式适配说明

## 概述
后端API响应格式已统一改为使用 `ApiResponse<T>` 和 `PagedResponse<T>` DTO格式，前端服务层已相应适配。

## 变更内容

### 1. 新增通用工具函数
**文件**: `frontend/src/utils/apiUtils.ts`

- `ApiResponse<T>`: 统一的API响应接口
- `PagedResponse<T>`: 分页响应接口
- `handleApiResponse<T>()`: 处理普通API响应的通用函数
- `handlePagedApiResponse<T>()`: 处理分页API响应的通用函数
- `createApiRequestConfig()`: 创建API请求配置的通用函数

### 2. 更新类型定义
**文件**: `frontend/src/types/user.ts`, `frontend/src/types/userRole.ts`, `frontend/src/types/testCaseSet.ts`

- 统一了 `ApiResponse` 和 `PagedResponse` 接口定义
- 移除了重复的类型定义
- 保持了向后兼容性

### 3. 更新服务层
**文件**: `frontend/src/services/userService.ts`, `frontend/src/services/userRoleService.ts`, `frontend/src/services/testCaseSetService.ts`

- 所有API调用现在使用统一的响应处理函数
- 简化了错误处理逻辑
- 提高了代码复用性

## 新的响应格式

### 成功响应
```json
{
  "success": true,
  "data": { /* 实际数据 */ },
  "message": "操作成功",
  "timestamp": "2025-09-16T10:30:00Z"
}
```

### 分页响应
```json
{
  "success": true,
  "data": {
    "data": [ /* 数据数组 */ ],
    "total": 100,
    "page": 1,
    "pageSize": 10,
    "totalPages": 10,
    "hasNext": true,
    "hasPrevious": false
  },
  "message": "操作成功",
  "timestamp": "2025-09-16T10:30:00Z"
}
```

### 错误响应
```json
{
  "success": false,
  "errorCode": "VALIDATION_ERROR",
  "message": "请求参数错误",
  "timestamp": "2025-09-16T10:30:00Z"
}
```

## 使用示例

### 获取用户列表
```typescript
import { getUsers } from '../services/userService';

const users = await getUsers(1, 10, 'search');
console.log(users.data); // 用户数组
console.log(users.total); // 总数
console.log(users.page); // 当前页
```

### 创建用户
```typescript
import { createUser } from '../services/userService';

const newUser = await createUser({
  username: 'testuser',
  password: 'password123'
});
console.log(newUser); // 创建的用户对象
```

## 错误处理
所有API调用现在统一使用 `handleApiResponse` 和 `handlePagedApiResponse` 函数处理响应，自动检查 `success` 字段并在失败时抛出错误。

## 兼容性
- 保持了现有API接口的向后兼容性
- 组件层无需修改，只需更新服务层调用
- 类型定义更加严格，提供更好的TypeScript支持

## 注意事项
1. 所有API响应现在都包含 `success` 字段
2. 分页数据现在使用 `PagedResponse` 格式
3. 错误信息现在包含 `errorCode` 字段
4. 所有响应都包含 `timestamp` 字段
