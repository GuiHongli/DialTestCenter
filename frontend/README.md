# Dial Test Center Frontend

拨测控制中心前端应用 - 基于React + TypeScript + Ant Design的现代化Web应用

## 📋 项目概述

Dial Test Center Frontend是一个用于管理拨测用例、用户角色和软件包的前端应用。该应用提供了直观的用户界面，支持多语言切换（中文/英文），并集成了完整的权限管理系统。

## 🚀 技术栈

- **框架**: React 18 + TypeScript
- **构建工具**: Vite + Webpack
- **UI组件库**: Ant Design 5.x
- **路由**: React Router v6
- **状态管理**: React Hooks + Context API
- **国际化**: 自定义i18n解决方案
- **样式**: CSS-in-JS + Ant Design主题
- **开发工具**: ESLint + TypeScript

## 📁 项目结构

```
frontend/
├── src/
│   ├── components/          # 可复用组件
│   │   ├── Layout.tsx      # 主布局组件
│   │   ├── UserManagement.tsx
│   │   ├── UserRoleManagement.tsx
│   │   ├── TestCaseSetManagement.tsx
│   │   ├── SoftwarePackageManagement.tsx
│   │   └── ...
│   ├── pages/              # 页面组件
│   │   ├── Home.tsx
│   │   ├── UserManagement.tsx
│   │   ├── UserRoleManagement.tsx
│   │   ├── TestCaseSetManagement.tsx
│   │   └── SoftwarePackageManagement.tsx
│   ├── services/           # API服务
│   │   ├── userService.ts
│   │   ├── userRoleService.ts
│   │   ├── testCaseSetService.ts
│   │   └── softwarePackageService.ts
│   ├── hooks/              # 自定义Hooks
│   │   ├── useTranslation.ts
│   │   └── usePermission.ts
│   ├── contexts/           # React Context
│   │   └── I18nContext.tsx
│   ├── types/              # TypeScript类型定义
│   │   ├── user.ts
│   │   ├── userRole.ts
│   │   ├── testCaseSet.ts
│   │   └── softwarePackage.ts
│   ├── locales/            # 国际化资源
│   │   ├── zh.json         # 中文翻译
│   │   └── en.json         # 英文翻译
│   ├── utils/              # 工具函数
│   │   └── errorHandler.ts
│   ├── App.tsx             # 应用入口组件
│   ├── main.tsx           # 应用启动文件
│   └── index.css          # 全局样式
├── public/                 # 静态资源
├── package.json           # 项目依赖配置
├── tsconfig.json         # TypeScript配置
├── webpack.config.js     # Webpack配置
└── README.md            # 项目说明文档
```

## 🛠️ 安装与运行

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
# 使用npm
npm install

# 或使用yarn
yarn install
```

### 开发环境运行

```bash
# 启动开发服务器
npm run dev

# 或
yarn dev
```

应用将在 `http://localhost:4396` 启动

### 生产环境构建

```bash
# 构建生产版本
npm run build

# 或
yarn build
```

构建文件将输出到 `dist/` 目录

### 预览生产构建

```bash
# 预览生产构建
npm run preview

# 或
yarn preview
```

## 🌐 功能特性

### 核心功能

- **用户管理**: 用户的增删改查操作
- **角色管理**: 用户角色的权限分配和管理
- **用例集管理**: 拨测用例集的上传、解析和管理
- **软件包管理**: 软件包的上传、存储和管理
- **操作日志**: 系统操作记录的查看和管理

### 界面特性

- **响应式设计**: 支持桌面和移动端适配
- **深色主题**: 统一的深色UI主题
- **侧边栏收起**: 可收起的左侧导航菜单
- **多语言支持**: 中文/英文切换
- **权限控制**: 基于角色的页面访问控制

### 技术特性

- **TypeScript**: 完整的类型安全
- **组件化**: 高度模块化的组件设计
- **国际化**: 完整的多语言支持
- **错误处理**: 统一的错误处理机制
- **加载状态**: 完善的加载和错误状态管理

## 🎨 UI组件

### 主要组件

- **Layout**: 应用主布局，包含侧边栏、头部和内容区域
- **UserManagement**: 用户管理界面
- **UserRoleManagement**: 用户角色管理界面
- **TestCaseSetManagement**: 用例集管理界面
- **SoftwarePackageManagement**: 软件包管理界面

### 设计规范

- **颜色主题**: 深色主题 (#001529)
- **字体**: 系统默认字体栈
- **间距**: 基于8px的间距系统
- **圆角**: 4px统一圆角
- **阴影**: 轻微阴影效果

## 🌍 国际化

### 支持语言

- **中文 (zh)**: 默认语言
- **英文 (en)**: 次要语言

### 语言切换

- 右上角语言切换按钮
- 显示格式: "中 | EN"
- 点击切换语言
- 语言偏好保存在localStorage

### 翻译文件

- `src/locales/zh.json`: 中文翻译
- `src/locales/en.json`: 英文翻译

## 🔐 权限管理

### 权限控制

- 基于用户角色的页面访问控制
- 使用 `usePermission` Hook进行权限检查
- 支持细粒度的功能权限控制

### 角色类型

- **管理员**: 完整系统访问权限
- **操作员**: 基础操作权限
- **查看者**: 只读访问权限

## 📡 API集成

### 服务层

- `userService.ts`: 用户相关API
- `userRoleService.ts`: 用户角色API
- `testCaseSetService.ts`: 用例集API
- `softwarePackageService.ts`: 软件包API
- `operationLogService.ts`: 操作日志API

### 请求配置

- 基础URL: `https://localhost:8087/api`
- 请求超时: 30秒
- 错误处理: 统一错误处理机制

## 🎯 开发指南

### 代码规范

- **函数长度**: 不超过50行
- **文件长度**: 不超过200行
- **组件命名**: PascalCase
- **文件命名**: PascalCase.tsx
- **变量命名**: camelCase

### 组件开发

```typescript
// 组件模板
import React from 'react';
import { ComponentProps } from '../types';

interface Props {
  // 组件属性定义
}

const ComponentName: React.FC<Props> = ({ ...props }) => {
  // 组件逻辑
  
  return (
    <div>
      {/* 组件JSX */}
    </div>
  );
};

export default ComponentName;
```

### 样式规范

- 使用内联样式或CSS模块
- 遵循Ant Design设计规范
- 保持响应式设计
- 使用语义化的CSS类名

### 错误处理

```typescript
// 错误处理示例
try {
  const result = await apiCall();
  // 处理成功结果
} catch (error) {
  console.error('API调用失败:', error);
  // 显示错误信息
}
```

## 🧪 测试

### 测试框架

- **单元测试**: Jest + React Testing Library
- **E2E测试**: Cypress (可选)

### 运行测试

```bash
# 运行单元测试
npm test

# 运行测试并生成覆盖率报告
npm run test:coverage
```

## 📦 构建与部署

### 构建配置

- **开发环境**: 热重载、源码映射
- **生产环境**: 代码压缩、资源优化
- **输出目录**: `dist/`

### 部署步骤

1. 构建生产版本
```bash
npm run build
```

2. 将 `dist/` 目录部署到Web服务器

3. 配置反向代理到后端API

### 环境变量

```bash
# .env.development
VITE_API_BASE_URL=https://localhost:8087/api

# .env.production
VITE_API_BASE_URL=https://api.yourdomain.com/api
```

## 🐛 故障排除

### 常见问题

1. **端口占用**
   ```bash
   # 查找占用端口的进程
   netstat -ano | findstr :3000
   
   # 终止进程
   taskkill /PID <PID> /F
   ```

2. **依赖安装失败**
   ```bash
   # 清除缓存重新安装
   npm cache clean --force
   rm -rf node_modules package-lock.json
   npm install
   ```

3. **构建失败**
   - 检查TypeScript类型错误
   - 确保所有依赖已正确安装
   - 检查Node.js版本兼容性

### 调试技巧

- 使用React Developer Tools
- 启用浏览器开发者工具
- 查看控制台错误信息
- 使用网络面板检查API请求

## 📚 相关文档

- [React官方文档](https://react.dev/)
- [TypeScript官方文档](https://www.typescriptlang.org/)
- [Ant Design组件库](https://ant.design/)
- [Vite构建工具](https://vitejs.dev/)
- [React Router路由](https://reactrouter.com/)

## 🤝 贡献指南

### 开发流程

1. Fork项目
2. 创建功能分支
3. 提交代码变更
4. 创建Pull Request
5. 代码审查
6. 合并到主分支

### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式调整
refactor: 代码重构
test: 测试相关
chore: 构建过程或辅助工具的变动
```

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 联系方式

- 项目维护者: Dial Test Center Team
- 邮箱: support@dialtestcenter.com
- 项目地址: https://github.com/your-org/dial-test-center

---

**注意**: 本项目是Dial Test Center系统的一部分，需要配合后端API使用。请确保后端服务正常运行后再启动前端应用。
