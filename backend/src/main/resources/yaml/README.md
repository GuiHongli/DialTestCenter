# DialTestCenter API 文档

本目录包含 DialTestCenter 项目的 API 接口文档，采用 OpenAPI 3.0 规范和 YAML 格式。

## 📁 文件结构

```
yaml/
├── README.md              # 本文档
├── user-api.yaml         # 用户管理 API 详细文档 (OpenAPI 3.0)
└── api-overview.yaml     # API 接口汇总文档
```

## 📋 文件说明

### 1. user-api.yaml
- **格式**: OpenAPI 3.0 (Swagger)
- **用途**: 用户管理模块的完整 API 文档
- **内容**: 
  - 接口路径定义
  - 请求/响应模型
  - 参数验证规则
  - 错误响应格式
  - 示例数据

### 2. api-overview.yaml
- **格式**: 自定义 YAML 格式
- **用途**: API 接口的快速参考和汇总
- **内容**:
  - 接口列表
  - 数据模型说明
  - 状态码定义
  - 配置信息

## 🚀 使用方法

### 查看 OpenAPI 文档
1. 启动后端服务
2. 访问 Swagger UI: `http://localhost:8080/swagger-ui.html`
3. 或者使用在线工具如 [Swagger Editor](https://editor.swagger.io/)

### 导入到 Postman
1. 复制 `user-api.yaml` 内容
2. 在 Postman 中导入 OpenAPI 规范
3. 自动生成 API 集合

### 代码生成
可以使用 OpenAPI Generator 生成客户端代码：
```bash
# 生成 Java 客户端
openapi-generator-cli generate -i user-api.yaml -g java -o ./java-client

# 生成 TypeScript 客户端
openapi-generator-cli generate -i user-api.yaml -g typescript-axios -o ./ts-client
```

## 🔧 维护说明

### 更新 API 文档
1. 修改对应的 YAML 文件
2. 确保 OpenAPI 规范语法正确
3. 更新版本号和更新时间
4. 测试生成的文档

### 添加新接口
1. 在 `user-api.yaml` 中添加新的路径定义
2. 在 `api-overview.yaml` 中更新接口列表
3. 定义新的数据模型（如需要）
4. 更新示例数据

## 📖 相关资源

- [OpenAPI 3.0 规范](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
- [OpenAPI Generator](https://openapi-generator.tech/)
- [Postman OpenAPI 导入](https://learning.postman.com/docs/integrations/available-integrations/working-with-openapi/)

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支
3. 更新 API 文档
4. 提交 Pull Request

## 📞 联系方式

如有问题或建议，请联系：
- 邮箱: support@dialtestcenter.com
- 项目地址: https://github.com/dialtestcenter
