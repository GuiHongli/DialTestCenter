# 国际化 (i18n) 使用指南

本项目已集成中英文国际化支持，使用 `react-i18next` 和 `i18next` 库实现。

## 功能特性

- ✅ 中英文双语支持
- ✅ 语言切换功能（右上角语言切换按钮）
- ✅ 自动语言检测（基于浏览器设置和本地存储）
- ✅ 类型安全的翻译函数
- ✅ Ant Design 组件库国际化
- ✅ 动态语言包加载

## 项目结构

```
src/
├── i18n/
│   └── index.ts              # i18n 配置文件
├── locales/
│   ├── zh.json              # 中文语言包
│   └── en.json              # 英文语言包
├── contexts/
│   └── I18nContext.tsx      # 国际化 Context 和 Provider
└── hooks/
    └── useTranslation.ts    # 翻译 Hook
```

## 使用方法

### 1. 在组件中使用翻译

```tsx
import { useTranslation } from '../hooks/useTranslation'

const MyComponent = () => {
  const { translateCommon, translateUserRole } = useTranslation()
  
  return (
    <div>
      <h1>{translateUserRole('title')}</h1>
      <button>{translateCommon('save')}</button>
    </div>
  )
}
```

### 2. 可用的翻译函数

- `translateCommon(key)` - 通用翻译（如：保存、取消、确定等）
- `translateApp(key)` - 应用相关翻译
- `translateNavigation(key)` - 导航相关翻译
- `translateHome(key)` - 首页相关翻译
- `translateUserRole(key)` - 用户角色管理相关翻译
- `translateTestCaseSet(key)` - 测试用例集相关翻译
- `translateLanguage(key)` - 语言相关翻译

### 3. 带参数的翻译

```tsx
// 在语言包中定义
{
  "userRole": {
    "table": {
      "pagination": "第 {{start}}-{{end}} 条/共 {{total}} 条"
    }
  }
}

// 在组件中使用
translateUserRole('table.pagination', { start: 1, end: 10, total: 100 })
```

### 4. 语言切换

```tsx
import { useLanguage } from '../hooks/useTranslation'

const LanguageSwitcher = () => {
  const { currentLanguage, setLanguage, toggleLanguage } = useLanguage()
  
  return (
    <div>
      <button onClick={() => setLanguage('zh')}>中文</button>
      <button onClick={() => setLanguage('en')}>English</button>
      <button onClick={toggleLanguage}>切换语言</button>
    </div>
  )
}
```

## 语言包结构

### 中文语言包 (zh.json)

```json
{
  "common": {
    "confirm": "确定",
    "cancel": "取消",
    "save": "保存"
  },
  "userRole": {
    "title": "用户角色管理",
    "table": {
      "id": "ID",
      "username": "用户名"
    }
  }
}
```

### 英文语言包 (en.json)

```json
{
  "common": {
    "confirm": "Confirm",
    "cancel": "Cancel", 
    "save": "Save"
  },
  "userRole": {
    "title": "User Role Management",
    "table": {
      "id": "ID",
      "username": "Username"
    }
  }
}
```

## 添加新的翻译

### 1. 在语言包中添加新的键值对

在 `src/locales/zh.json` 和 `src/locales/en.json` 中添加对应的翻译：

```json
// zh.json
{
  "newFeature": {
    "title": "新功能",
    "description": "这是一个新功能"
  }
}

// en.json
{
  "newFeature": {
    "title": "New Feature",
    "description": "This is a new feature"
  }
}
```

### 2. 在组件中使用

```tsx
const { t } = useTranslation()

// 使用通用翻译函数
const title = t('newFeature.title')
const description = t('newFeature.description')

// 或者创建专门的翻译函数
const translateNewFeature = (key: string) => t(`newFeature.${key}`)
```

## 配置说明

### 语言检测顺序

1. localStorage 中保存的语言设置
2. 浏览器语言设置
3. HTML 标签的 lang 属性
4. 默认语言（中文）

### 语言存储

用户选择的语言会保存在 `localStorage` 中，键名为 `i18nextLng`。

### Ant Design 国际化

项目已配置 Ant Design 组件库的国际化，会根据当前语言自动切换组件的中英文显示。

## 最佳实践

1. **命名规范**：使用有意义的键名，如 `userRole.table.id` 而不是 `t1`
2. **分组管理**：按功能模块分组管理翻译键
3. **参数化**：对于动态内容使用参数，如 `{{count}} 条记录`
4. **一致性**：保持中英文翻译的一致性
5. **测试**：确保所有语言包都有对应的翻译

## 故障排除

### 翻译不显示

1. 检查语言包中是否存在对应的键
2. 检查键名是否正确
3. 检查翻译函数调用是否正确

### 语言切换不生效

1. 检查 `I18nProvider` 是否正确包装了应用
2. 检查语言包是否正确加载
3. 检查浏览器控制台是否有错误信息

### TypeScript 类型错误

1. 确保导入了正确的翻译函数
2. 检查键名是否存在于语言包中
3. 检查参数类型是否正确
