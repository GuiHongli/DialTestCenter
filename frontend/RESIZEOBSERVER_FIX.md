# ResizeObserver 错误修复说明

## 问题描述

在使用 Ant Design 组件库时，经常会遇到以下错误：

```
Uncaught runtime errors:
×
ERROR
ResizeObserver loop completed with undelivered notifications.
```

这个错误通常出现在使用 Layout、Table、Form 等 Ant Design 组件时，特别是在开发环境中。

## 错误原因

这个错误是由于 ResizeObserver API 的异步特性导致的。当 DOM 元素在 ResizeObserver 回调执行期间被修改时，就会出现这个错误。这是 Ant Design 组件库中的一个已知问题，不会影响应用的实际功能。

## 解决方案

我们采用了多层次的解决方案来完全抑制这个错误：

### 1. 错误处理工具 (`src/utils/errorHandler.ts`)

创建了专门的错误处理工具，用于：
- 抑制 console.error 和 console.warn 中的 ResizeObserver 错误
- 处理全局错误事件
- 处理未捕获的 Promise 拒绝

### 2. 主入口文件 (`src/main.tsx`)

在应用启动时初始化错误处理：
```typescript
import { initErrorHandler } from './utils/errorHandler'

// 初始化错误处理，抑制 ResizeObserver 相关错误
initErrorHandler()
```

### 3. Webpack 配置 (`webpack.config.js`)

在开发服务器配置中添加客户端覆盖配置：
```javascript
client: {
  overlay: {
    errors: true,
    warnings: false,
    runtimeErrors: (error) => {
      // 抑制 ResizeObserver 相关的运行时错误
      const errorMessage = error.message || '';
      if (errorMessage.includes('ResizeObserver loop completed with undelivered notifications')) {
        return false;
      }
      return true;
    },
  },
},
```

## 效果

实施这个解决方案后：
- ✅ 开发环境中不再显示 ResizeObserver 错误覆盖层
- ✅ 控制台中不再输出 ResizeObserver 相关错误
- ✅ 应用功能完全正常，不受影响
- ✅ 其他真正的错误仍然会正常显示

## 注意事项

1. 这个解决方案只抑制 ResizeObserver 相关的错误，不会影响其他真正的错误
2. 错误处理工具是可配置的，可以根据需要调整
3. 这个错误只在开发环境中出现，生产环境中通常不会出现

## 相关链接

- [ResizeObserver API 文档](https://developer.mozilla.org/en-US/docs/Web/API/ResizeObserver)
- [Ant Design 相关 Issue](https://github.com/ant-design/ant-design/issues)
