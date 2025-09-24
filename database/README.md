# DialTestCenter 数据库结构文件说明

## 概述

本目录包含了 DialTestCenter 拨测控制中心的完整数据库结构文件，用于在不同环境中部署和迁移数据库。

## 文件说明

### 1. 核心文件

#### `core_database_schema.sql`
- **用途**: 核心数据库结构，包含主要业务表
- **适用场景**: 新环境部署、核心功能测试
- **包含表**: 
  - `roles` - 角色表
  - `user_roles` - 用户角色关系表
  - `test_case_set` - 测试用例集表
  - `test_case` - 测试用例表
  - `app_type` - 应用类型表
  - `software_package` - 软件包表
  - `operation_logs` - 操作记录表

#### `complete_database_schema.sql`
- **用途**: 完整数据库结构，包含所有表和历史表
- **适用场景**: 完整环境部署、数据迁移
- **包含表**: 核心表 + 历史表 + 扩展表

#### `database_schema.sql`
- **用途**: 从现有数据库导出的完整结构
- **适用场景**: 备份、结构分析
- **说明**: 使用 pg_dump 工具从现有数据库导出

### 2. 历史文件

#### `test_case_set_management.sql`
- **用途**: 测试用例集管理模块初始化脚本
- **状态**: 历史版本，建议使用新的核心文件

#### `user_role_management_supplement.sql`
- **用途**: 用户角色管理补充数据
- **状态**: 历史版本，已整合到核心文件中

#### `alter_environment_config_to_text.sql`
- **用途**: 环境配置字段类型修改
- **状态**: 历史版本，已整合到核心文件中

#### `rename_business_to_business_zh.sql`
- **用途**: 业务字段重命名
- **状态**: 历史版本，已整合到核心文件中

## 使用方法

### 1. 新环境部署

```bash
# 创建数据库
createdb -U postgres dialingtest

# 执行核心结构脚本
psql -U postgres -d dialingtest -f core_database_schema.sql
```

### 2. 完整环境部署

```bash
# 创建数据库
createdb -U postgres dialingtest

# 执行完整结构脚本
psql -U postgres -d dialingtest -f complete_database_schema.sql
```

### 3. 数据迁移

```bash
# 导出现有数据库结构
pg_dump -h localhost -p 5432 -U postgres -d dialingtest --schema-only --no-owner --no-privileges -f database_schema.sql

# 在新环境中导入
psql -U postgres -d dialingtest_new -f database_schema.sql
```

## 数据库配置

### 连接信息
- **主机**: localhost
- **端口**: 5432
- **数据库名**: dialingtest
- **用户名**: postgres
- **密码**: postgres

### 应用配置
在 `application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dialingtest
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
```

## 表结构说明

### 核心表

#### 1. roles (角色表)
- 存储系统角色定义
- 支持中英文名称和描述
- 预定义角色: ADMIN, OPERATOR, BROWSER, EXECUTOR

#### 2. user_roles (用户角色表)
- 用户与角色的关联关系
- 支持一个用户拥有多个角色

#### 3. test_case_set (测试用例集表)
- 存储测试用例集信息
- 包含文件内容、版本、业务类型等
- 支持中英文业务类型

#### 4. test_case (测试用例表)
- 存储具体测试用例信息
- 关联测试用例集
- 包含测试步骤、预期结果、环境配置等

#### 5. app_type (应用类型表)
- 存储应用类型信息
- 按业务大类和应用名称分类

#### 6. software_package (软件包表)
- 存储软件包信息
- 支持 APK 和 IPA 格式
- 包含文件内容、哈希值、平台信息等

#### 7. operation_logs (操作记录表)
- 记录用户操作历史
- 支持中英文描述
- 包含操作类型、目标、数据等

## 索引说明

所有表都创建了相应的索引以提高查询性能：

- **主键索引**: 所有表的主键
- **唯一索引**: 业务唯一约束
- **外键索引**: 关联关系查询
- **业务索引**: 常用查询字段

## 约束说明

### 主键约束
- 所有表都有自增主键

### 唯一约束
- 测试用例集: 名称+版本唯一
- 测试用例: 用例集ID+用例编号唯一
- 应用类型: 业务大类+应用名称唯一
- 软件包: 软件名称唯一、SHA512唯一
- 用户角色: 用户名+角色唯一

### 外键约束
- 测试用例表关联测试用例集表
- 级联删除: 删除用例集时自动删除关联的测试用例

### 检查约束
- 操作记录表: 中英文描述至少有一个不为空

## 初始数据

脚本会自动插入以下初始数据：

1. **角色定义**:
   - ADMIN: 管理员
   - OPERATOR: 操作员
   - BROWSER: 浏览者
   - EXECUTOR: 执行者

2. **默认用户**:
   - admin: 管理员用户

## 注意事项

1. **字符编码**: 确保数据库使用 UTF-8 编码
2. **权限设置**: 确保应用用户有足够的数据库权限
3. **备份策略**: 定期备份数据库结构和数据
4. **版本控制**: 数据库结构变更需要版本管理
5. **测试环境**: 在生产环境部署前先在测试环境验证

## 故障排除

### 常见问题

1. **连接失败**: 检查数据库服务是否启动
2. **权限不足**: 检查用户权限设置
3. **字符编码**: 确保数据库和客户端编码一致
4. **约束冲突**: 检查唯一约束和外键约束

### 日志查看

```bash
# 查看PostgreSQL日志
tail -f /var/log/postgresql/postgresql-*.log

# 查看应用日志
tail -f dialingtest-service/logs/dialingtest-service.log
```

## 联系支持

如有问题，请联系开发团队或查看项目文档。
