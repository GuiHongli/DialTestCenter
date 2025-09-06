# 用例集管理支持 .tar.gz 格式实现总结

## 实现概述

已成功为 Dial Test Center 的用例集管理功能添加了 `.tar.gz` 格式支持，现在系统同时支持 `.zip` 和 `.tar.gz` 两种压缩格式。

## 前端修改

### 1. 服务层更新 (`testCaseSetService.ts`)

- **文件验证逻辑**：更新 `validateTestCaseSetFile` 方法支持 `.zip` 和 `.tar.gz` 格式
- **文件名解析**：更新 `parseFileName` 方法支持不同文件扩展名
- **错误消息**：更新验证错误消息以反映支持的格式

```typescript
// 支持的文件格式
const supportedExtensions = ['.zip', '.tar.gz']

// 文件格式验证
const isValidExtension = supportedExtensions.some(ext => fileName.endsWith(ext))
```

### 2. 上传组件更新 (`TestCaseSetUpload.tsx`)

- **文件接受类型**：更新 `accept` 属性为 `.zip,.tar.gz`
- **类型转换**：添加文件类型转换以解决 TypeScript 类型问题

### 3. 管理组件更新 (`TestCaseSetManagement.tsx`)

- **下载功能**：根据 `fileFormat` 字段确定正确的文件扩展名
- **表格显示**：在用例集名称列显示文件格式标签
- **文件格式标签**：使用不同颜色区分 ZIP 和 TAR.GZ 格式

### 4. 类型定义更新 (`testCaseSet.ts`)

```typescript
export interface TestCaseSet {
  // ... 其他字段
  fileFormat?: 'zip' | 'tar.gz' // 新增文件格式字段
}
```

### 5. 国际化更新

- **中文语言包**：更新支持格式说明为 "支持 .zip 和 .tar.gz 格式"
- **英文语言包**：更新支持格式说明为 "Support .zip and .tar.gz formats"

## 后端修改

### 1. 实体类更新 (`TestCaseSet.java`)

- **新增字段**：添加 `fileFormat` 字段存储文件格式
- **构造函数**：更新构造函数包含文件格式参数
- **Getter/Setter**：添加文件格式的访问器方法
- **toString**：更新字符串表示包含文件格式信息

```java
@Column(name = "file_format", nullable = false, length = 10)
private String fileFormat; // 文件格式：zip 或 tar.gz
```

### 2. 控制器更新 (`TestCaseSetController.java`)

- **下载功能**：根据文件格式设置正确的 Content-Type 和文件扩展名
- **MIME 类型**：
  - ZIP: `application/zip`
  - TAR.GZ: `application/gzip`

```java
if ("tar.gz".equals(fileFormat)) {
    fileExtension = ".tar.gz";
    contentType = MediaType.parseMediaType("application/gzip");
} else if ("zip".equals(fileFormat)) {
    fileExtension = ".zip";
    contentType = MediaType.parseMediaType("application/zip");
}
```

### 3. 服务层更新 (`TestCaseSetService.java`)

- **文件解析**：支持解析 `.zip` 和 `.tar.gz` 文件名
- **格式检测**：根据文件扩展名确定文件格式
- **验证逻辑**：更新文件验证支持两种格式
- **日志记录**：在日志中包含文件格式信息

```java
if (fileName.toLowerCase().endsWith(".tar.gz")) {
    fileFormat = "tar.gz";
    nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".tar.gz"));
} else if (fileName.toLowerCase().endsWith(".zip")) {
    fileFormat = "zip";
    nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".zip"));
}
```

## 数据库修改

### 1. 迁移脚本 (`migration_add_file_format.sql`)

- **添加字段**：为 `test_case_set` 表添加 `file_format` 字段
- **默认值**：设置默认值为 'zip' 以兼容现有数据
- **数据更新**：更新现有记录的文件格式
- **索引创建**：为文件格式字段创建索引以提高查询性能

```sql
ALTER TABLE test_case_set 
ADD COLUMN file_format VARCHAR(10) NOT NULL DEFAULT 'zip';

UPDATE test_case_set 
SET file_format = 'zip' 
WHERE file_format IS NULL OR file_format = '';
```

## 功能特性

### 1. 文件格式支持
- ✅ ZIP 格式 (`.zip`)
- ✅ TAR.GZ 格式 (`.tar.gz`)
- ✅ 自动格式检测
- ✅ 格式验证

### 2. 用户界面
- ✅ 拖拽上传支持两种格式
- ✅ 文件格式标签显示
- ✅ 格式相关的错误提示
- ✅ 中英文国际化支持

### 3. 下载功能
- ✅ 根据文件格式设置正确的 MIME 类型
- ✅ 正确的文件扩展名下载
- ✅ 浏览器兼容性

### 4. 数据管理
- ✅ 文件格式信息存储
- ✅ 向后兼容现有数据
- ✅ 数据库索引优化

## 使用说明

### 1. 上传文件
- 支持拖拽或点击上传 `.zip` 和 `.tar.gz` 文件
- 文件名格式：`用例集名称_版本号.[zip|tar.gz]`
- 文件大小限制：100MB

### 2. 文件格式显示
- 在用例集列表中显示文件格式标签
- ZIP 格式显示蓝色标签
- TAR.GZ 格式显示绿色标签

### 3. 下载文件
- 点击下载按钮获取原始格式的文件
- 浏览器会根据文件格式设置正确的 MIME 类型

## 测试验证

### 1. 前端测试
- [ ] 上传 ZIP 文件
- [ ] 上传 TAR.GZ 文件
- [ ] 文件格式验证
- [ ] 下载功能测试
- [ ] 界面显示测试

### 2. 后端测试
- [ ] 文件解析测试
- [ ] 格式验证测试
- [ ] 数据库存储测试
- [ ] 下载响应测试

### 3. 集成测试
- [ ] 端到端上传流程
- [ ] 文件格式显示
- [ ] 下载功能完整性

## 部署说明

### 1. 数据库迁移
```bash
# 执行数据库迁移脚本
psql -d dialtest_center -f database/migration_add_file_format.sql
```

### 2. 应用部署
```bash
# 编译后端
mvn clean package

# 启动应用
java -jar target/center-1.0.0.jar
```

### 3. 前端部署
```bash
# 安装依赖
npm install

# 构建前端
npm run build

# 启动开发服务器
npm run dev
```

## 注意事项

1. **向后兼容**：现有 ZIP 格式数据完全兼容
2. **文件大小**：两种格式都遵循 100MB 限制
3. **命名规范**：文件名必须包含下划线分隔的名称和版本
4. **MIME 类型**：确保服务器正确配置 MIME 类型支持

## 总结

成功实现了用例集管理对 `.tar.gz` 格式的支持，包括前端界面、后端逻辑、数据库结构和国际化。系统现在可以无缝处理两种压缩格式，为用户提供更灵活的文件上传选择。
