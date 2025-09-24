# X-Username 认证功能使用说明

## 概述

前端现在支持自动在API请求中携带 `X-Username` 认证头，用户名从cookie中获取。

## 功能特性

- ✅ 自动从cookie获取 `xUsername`
- ✅ 所有API请求自动携带 `X-Username` 头
- ✅ 支持文件上传请求的认证
- ✅ 如果cookie不存在，使用默认用户名 'admin'
- ✅ 提供cookie管理工具函数

## 使用方法

### 1. 设置用户名Cookie

```typescript
import { setXUsernameCookie } from '../utils/cookieUtils';

// 设置用户名为 'john'
setXUsernameCookie('john');

// 设置用户名并指定有效期（30天）
setXUsernameCookie('john', 30);
```

### 2. 清除用户名Cookie

```typescript
import { removeXUsernameCookie } from '../utils/cookieUtils';

// 清除用户名cookie
removeXUsernameCookie();
```

### 3. 检查Cookie状态

```typescript
import { hasXUsernameCookie, getXUsernameFromCookie } from '../utils/cookieUtils';

// 检查是否有用户名cookie
if (hasXUsernameCookie()) {
  console.log('用户已登录');
}

// 获取当前用户名
const username = getXUsernameFromCookie();
console.log('当前用户:', username);
```

### 4. API调用（自动携带认证头）

```typescript
import TestCaseSetService from '../services/testCaseSetService';

// 所有API调用都会自动携带X-Username头
const testCases = await TestCaseSetService.getTestCaseSets(1, 10);
const testCase = await TestCaseSetService.getTestCaseSet(1);
await TestCaseSetService.deleteTestCaseSet(1);
```

### 5. 手动创建API请求

```typescript
import { createApiRequestConfig, createFileUploadConfig } from '../utils/apiUtils';

// GET请求（自动携带X-Username）
const config = createApiRequestConfig('GET');
const response = await fetch('/api/test-cases', config);

// POST请求（自动携带X-Username）
const config = createApiRequestConfig('POST', { name: 'test' });
const response = await fetch('/api/test-cases', config);

// 文件上传（自动携带X-Username）
const formData = new FormData();
formData.append('file', file);
const config = createFileUploadConfig(formData);
const response = await fetch('/api/upload', config);
```

## 测试组件

使用 `AuthTestComponent` 来测试认证功能：

```typescript
import AuthTestComponent from '../components/AuthTestComponent';

// 在页面中使用
<AuthTestComponent />
```

## 后端接口要求

后端接口需要支持 `X-Username` 请求头：

```yaml
parameters:
  - name: "X-Username"
    in: "header"
    required: true
    type: "string"
    description: "操作用户名"
```

## 默认行为

- 如果cookie中不存在 `xUsername`，系统会自动使用 `'admin'` 作为默认用户名
- 所有API请求都会自动携带 `X-Username` 头
- Cookie默认有效期为30天

## 安全注意事项

- Cookie中的用户名是明文存储的，仅用于开发测试
- 生产环境建议使用更安全的认证机制（如JWT Token）
- 可以通过 `removeXUsernameCookie()` 清除认证信息
