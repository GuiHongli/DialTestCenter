# API Documentation YAML Files

本目录包含了各个Controller的Swagger 2格式API文档YAML文件。

## 文件列表

### 1. user-controller.yaml
- **Controller**: UserController
- **功能**: 用户管理API
- **包含接口**:
  - 获取所有用户列表
  - 根据ID获取用户
  - 根据用户名获取用户
  - 创建新用户
  - 更新用户信息
  - 删除用户
  - 搜索用户
  - 验证用户密码
  - 更新最后登录时间

### 2. user-role-controller.yaml
- **Controller**: UserRoleController
- **功能**: 用户角色管理API
- **包含接口**:
  - 获取用户角色列表
  - 创建用户角色
  - 更新用户角色
  - 删除用户角色
  - 获取执行机用户数量

### 3. test-case-set-controller.yaml
- **Controller**: TestCaseSetController
- **功能**: 测试用例集管理API
- **包含接口**:
  - 获取测试用例集列表（支持分页和搜索）
  - 上传测试用例集（支持ZIP和TAR.GZ格式）
  - 获取测试用例集详情
  - 更新测试用例集
  - 删除测试用例集
  - 下载测试用例集文件
  - 获取测试用例列表

### 4. software-package-controller.yaml
- **Controller**: SoftwarePackageController
- **功能**: 软件包管理API
- **包含接口**:
  - 获取软件包列表（支持分页、搜索和平台过滤）
  - 上传软件包（支持APK和IPA格式）
  - 获取软件包详情
  - 更新软件包
  - 删除软件包
  - 下载软件包文件
  - 批量上传软件包

### 5. operation-log-controller.yaml
- **Controller**: OperationLogController
- **功能**: 操作日志管理API
- **包含接口**:
  - 获取操作日志列表（支持分页和过滤）

## 使用方法

### 1. Swagger UI
启动应用后，可以通过以下URL访问Swagger UI：
- `https://localhost:8087/dialingtest/swagger-ui.html`
- `http://localhost:8087/dialingtest/swagger-ui.html`

### 2. API文档JSON格式
- `https://localhost:8087/dialingtest/v2/api-docs`

### 3. 导入到其他工具
这些YAML文件可以直接导入到：
- Swagger Editor
- Postman
- Insomnia
- 其他API测试工具

## 配置说明

- **Host**: localhost:8087
- **Base Path**: /dialingtest
- **协议**: HTTPS/HTTP
- **认证**: 当前未配置认证，可根据需要添加

## 数据结构

每个YAML文件都包含了完整的数据模型定义，包括：
- 请求模型
- 响应模型
- 错误响应模型
- 分页响应模型

## 更新说明

当Controller代码发生变化时，需要同步更新对应的YAML文件以保持文档的准确性。