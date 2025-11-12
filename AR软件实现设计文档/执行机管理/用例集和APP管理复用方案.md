# 用例集和APP管理复用方案

> **文档版本**：V2.1  
> **创建日期**：2025-11-12  
> **主要变更**：
> - ✅ 明确脚本和APP的数据源为test_case_set和software_package表
> - ✅ 在TaskInterfaceService中补充数据查询桥接逻辑
> - ✅ 复用现有WebSocket推送接口，避免过度设计
> - ✅ 符合《执行机管理业务逻辑设计.md》第5.5、5.6节流程

## 1. 概述

### 1.1 方案背景

根据《执行机管理业务逻辑设计.md》第5.5节（UE App安装流程）和第5.6节（拨测脚本更新流程），执行机需要从"文件存储（Storage）"获取脚本和APP文件内容并推送到执行机。

改造前，推送链路不完整：
- 前端管理界面：已实现用例集和APP的上传、下载、管理
- 数据存储：已存储在`test_case_set`和`software_package`表
- 推送接口：`TaskInterfaceService`已有sendScriptUpdate()和sendAppInstall()方法
- **缺失环节**：从数据库读取file_content并构造推送请求的桥接逻辑

### 1.2 方案目标

明确数据源复用方案，补充桥接逻辑：
1. **脚本推送**：从`test_case_set`表读取 → 推送到执行机
2. **APP推送**：从`software_package`表读取 → 推送到执行机

核心原则：
- 复用现有数据表和管理界面（无需新增）
- 在TaskInterfaceService中补充查询逻辑（避免过度设计）
- 符合业务逻辑设计文档的架构（Storage = 数据库表）

## 2. 数据源分析

### 2.1 数据表结构

#### test_case_set 表（脚本数据源）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| id | bigserial | 主键 |
| name | varchar(200) | 用例集名称 |
| version | varchar(50) | 版本号 |
| **file_content** | **bytea** | **存储完整ZIP/TAR.GZ包（脚本源文件）** |
| file_size | bigint | 文件大小 |
| sha256 | varchar(64) | SHA256校验值 |
| business_zh | varchar(50) | 业务类型（中文） |
| business_en | varchar(50) | 业务类型（英文） |

**约束**：UNIQUE(name, version)

#### software_package 表（APP数据源）

| 字段 | 类型 | 说明 |
|:---|:---|:---|
| id | bigserial | 主键 |
| software_name | varchar(255) | 软件名称（唯一） |
| **file_content** | **bytea** | **存储APK/IPA文件** |
| file_size | bigint | 文件大小 |
| file_sha256 | varchar(64) | SHA256校验值 |
| platform | varchar(20) | 平台（Android/iOS） |
| description | text | 描述信息 |
| creator | varchar(100) | 创建者 |

**约束**：UNIQUE(software_name)

### 2.2 与设计文档的对应关系

| 设计文档组件 | 实际实现 | 状态 |
|:---|:---|:---:|
| Admin（前端管理） | TestCaseSetManagement.jsx<br>SoftwarePackageManagement.jsx | ✅ 已实现 |
| ScriptMgmt（管理模块） | TestCaseSetController<br>SoftwarePackageController | ✅ 已实现 |
| **Storage（文件存储）** | **test_case_set.file_content**<br>**software_package.file_content** | ✅ 已实现 |
| TaskInterfaceService | TaskInterfaceService<br>sendScriptUpdate()<br>sendAppInstall() | ⚠️ 需补充桥接 |
| WssMessageSender | WssMessageSender | ✅ 已实现 |
| WebSocket消息 | ScriptUpdate-Notify (0x31)<br>AppInstall-Request (0x23) | ✅ 已实现 |

## 3. 方案设计

### 3.1 脚本推送流程（对应设计文档5.6节）

**触发方式**：
- 方式A：管理员上传新脚本后主动推送
- 方式B：任务启动时检查版本并按需推送

**数据流向**：
```
用例集管理 → test_case_set表 → TaskInterfaceService.pushScriptToExecutor()
                                    ↓
                          查询findByNameAndVersion()
                                    ↓
                          构造ScriptUpdateRequest
                                    ↓
                          调用sendScriptUpdate()
                                    ↓
                    WssMessageSender → ScriptUpdate-Notify (0x31) → 执行机
```

### 3.2 APP推送流程（对应设计文档5.5节）

**触发方式**：
- 任务启动前查询UE已安装APP，发现缺失时推送

**数据流向**：
```
软件包管理 → software_package表 → TaskInterfaceService.pushAppToUe()
                                      ↓
                            查询findBySoftwareName()
                                      ↓
                            构造AppInstallRequest
                                      ↓
                            调用sendAppInstall()
                                      ↓
                    WssMessageSender → AppInstall-Request (0x23) → 执行机
```

## 4. 代码变更

### 4.1 变更清单

| 文件 | 变更类型 | 说明 |
|:---|:---:|:---|
| `TestCaseSetDao.java` | ➕ 新增 | 新增findByNameAndVersion()方法 |
| `SoftwarePackageDao.java` | ➕ 新增 | 新增findBySoftwareName()方法 |
| `TaskInterfaceService.java` | ➕ 新增 | 新增pushScriptToExecutor()方法 |
| `TaskInterfaceService.java` | ➕ 新增 | 新增pushAppToUe()方法 |
| `TaskInterfaceService.java` | 📝 修改 | 注入TestCaseSetDao依赖 |
| `TaskInterfaceService.java` | 📝 修改 | 注入SoftwarePackageDao依赖 |
| `DialingTestTaskImpl.java` | 📝 修改 | 任务启动时调用推送方法（可选） |

### 4.2 DAO层新增方法（P0 - 必须）

#### TestCaseSetDao.java
```java
/**
 * 根据名称和版本查询用例集（含file_content）
 * 用于脚本推送到执行机
 */
TestCaseSet findByNameAndVersion(String name, String version);
```

**SQL**：`SELECT * FROM test_case_set WHERE name = #{name} AND version = #{version}`

#### SoftwarePackageDao.java
```java
/**
 * 根据软件名称查询（含file_content）
 * 用于APP推送到执行机
 */
SoftwarePackage findBySoftwareName(String softwareName);
```

**SQL**：`SELECT * FROM software_package WHERE software_name = #{softwareName}`

### 4.3 TaskInterfaceService新增方法（P1/P2）

#### pushScriptToExecutor() - P1高优先级
```java
/**
 * 向指定执行机推送脚本更新（从数据库读取）
 * 对应设计文档5.6节流程
 *
 * @param executorName 执行机名称
 * @param scriptName 脚本名称
 * @param version 版本号
 */
public void pushScriptToExecutor(String executorName, String scriptName, String version)
```

**实现要点**：
1. 调用`testCaseSetDao.findByNameAndVersion()`查询
2. 读取`file_content`字段
3. 构造`ScriptUpdateRequest`对象
4. 调用现有的`sendScriptUpdate()`方法

#### pushAppToUe() - P2中优先级
```java
/**
 * 向指定执行机和UE推送APP安装（从数据库读取）
 * 对应设计文档5.5节流程
 *
 * @param executorName 执行机名称
 * @param serialNo UE序列号
 * @param appName APP名称
 * @param taskId 任务ID
 */
public void pushAppToUe(String executorName, String serialNo, String appName, Integer taskId)
```

**实现要点**：
1. 调用`softwarePackageDao.findBySoftwareName()`查询
2. 读取`file_content`字段
3. 构造`AppInstallRequest`对象（使用package字段）
4. 调用现有的`sendAppInstall()`方法

### 4.4 依赖注入修改

#### TaskInterfaceService.java
```java
@Autowired
private TestCaseSetDao testCaseSetDao;  // 新增

@Autowired
private SoftwarePackageDao softwarePackageDao;  // 新增
```

### 4.5 任务前置检查（P3 - 可选）

#### DialingTestTaskImpl.java
```java
// 任务启动前调用
if (needScriptUpdate(context)) {
    taskInterfaceService.pushScriptToExecutor(executorName, scriptName, version);
}

// 检查缺失APP并推送
List<String> missingApps = checkMissingApps(executorName, ueSerial, context);
for (String app : missingApps) {
    taskInterfaceService.pushAppToUe(executorName, ueSerial, app, taskId);
}
```