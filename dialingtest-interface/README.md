# DialingTest Interface

## 项目概述

这是一个基于 Swagger 2.0 规范的接口定义项目，使用 `io.swagger.codegen.v3` 3.0.46 版本自动生成模型类、接口类和 Controller。

## 项目结构

```
dialingtest-interface/
├── pom.xml                                    # Maven 配置文件
├── README.md                                  # 项目说明文档
└── src/
    ├── main/
    │   ├── java/                              # 生成的 Java 源码目录
    │   └── resources/
    │       └── yaml/
    │           └── dialuser-api.yaml          # Swagger 2.0 API 定义文件
    └── test/
        ├── java/                              # 生成的测试源码目录
        └── resources/
```

## 技术栈

- **Maven**: 项目构建工具
- **Swagger Codegen V3**: 3.0.46 版本，用于代码生成
- **Spring Boot**: 2.7.18 版本
- **Jackson**: 2.15.2 版本，用于 JSON 处理
- **Java 8**: 目标编译版本

## 生成的文件类型

### 模型类 (Model Classes)
- `DialUser.java` - 拨测用户实体类
- `CreateDialUserRequest.java` - 创建用户请求类
- `UpdateDialUserRequest.java` - 更新用户请求类
- `DialUserResponse.java` - 用户响应类
- `DialUserPageResponse.java` - 分页响应类

### 接口类 (API Interface)
- `DialusersApi.java` - 拨测用户 API 接口定义

### Controller 类
- `DialusersApiController.java` - 拨测用户 API Controller 实现

## 使用方法

### 1. 生成代码

```bash
mvn clean generate-sources
```

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行测试

```bash
mvn test
```

### 4. 打包项目

```bash
mvn clean package
```

## 配置说明

### Swagger Codegen 配置

- **输入文件**: `src/main/resources/yaml/dialuser-api.yaml`
- **生成语言**: Spring Boot
- **输出目录**: `target/generated-sources/swagger`
- **包名配置**:
  - API 包: `com.huawei.cloududn.dialingtest.api`
  - 模型包: `com.huawei.cloududn.dialingtest.model`
  - 调用包: `com.huawei.cloududn.dialingtest`

### 生成选项

- `library`: spring-boot
- `dateLibrary`: java8
- `java8`: true
- `useBeanValidation`: true
- `performBeanValidation`: true
- `useTags`: true
- `interfaceOnly`: false
- `skipDefaultInterface`: false
- `hideGenerationTimestamp`: true

## API 定义

项目基于 Swagger 2.0 规范定义了拨测用户管理 API，包括：

- **GET** `/dialusers` - 分页查询拨测用户
- **POST** `/dialusers` - 新增拨测用户
- **GET** `/dialusers/{id}` - 根据ID查询拨测用户
- **PUT** `/dialusers/{id}` - 修改拨测用户
- **DELETE** `/dialusers/{id}` - 删除拨测用户

## 依赖说明

- `spring-boot-starter-web`: Spring Boot Web 启动器
- `jackson-annotations`: Jackson JSON 注解支持
- `swagger-annotations`: Swagger 注解支持
- `validation-api`: Bean Validation API
- `spring-boot-starter-test`: Spring Boot 测试支持
- `junit`: JUnit 测试框架

## 注意事项

1. 确保 Java 8 或更高版本已安装
2. 确保 Maven 3.6 或更高版本已安装
3. 生成的代码位于 `target/generated-sources/swagger` 目录
4. 生成的源码会自动添加到编译路径中
5. 修改 YAML 文件后需要重新运行 `mvn generate-sources` 来更新生成的代码
