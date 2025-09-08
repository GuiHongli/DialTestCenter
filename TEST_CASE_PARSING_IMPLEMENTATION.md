# 用例集管理 - 压缩包解析和用例信息存储功能实现

## 功能概述

本次实现为用例集管理功能添加了压缩包解析和用例信息存储功能，支持以下核心功能：

1. **压缩包解析**：解析ZIP和TAR.GZ格式的压缩包，提取cases.xlsx文件和scripts目录
2. **Excel文件解析**：解析cases.xlsx文件，提取用例信息
3. **脚本文件匹配**：匹配用例编号与Python脚本文件
4. **不匹配信息记录**：记录缺失脚本的用例信息
5. **前端展示**：在页面上展示测试用例详情和缺失脚本信息

## 实现的功能

### 1. 压缩包解析功能

#### 后端实现
- **ArchiveParseService**：压缩包解析服务
  - 支持ZIP和TAR.GZ格式
  - 提取cases.xlsx文件内容
  - 提取scripts目录下的Python脚本文件名列表
  - 验证压缩包结构完整性

#### 核心方法
```java
// 提取cases.xlsx文件内容
byte[] extractCasesExcel(byte[] archiveData, String fileFormat)

// 提取脚本文件名列表
List<String> extractScriptFileNames(byte[] archiveData, String fileFormat)

// 验证压缩包结构
ArchiveValidationResult validateArchive(byte[] archiveData, String fileFormat)
```

### 2. Excel文件解析功能

#### 后端实现
- **ExcelParseService**：Excel文件解析服务
  - 使用Apache POI库解析Excel文件
  - 提取用例信息：用例名称、用例编号、用例逻辑组网、用例业务大类、用例App、用例测试步骤、用例预期结果
  - 支持数据验证和错误处理

#### 核心方法
```java
// 解析Excel文件并提取用例信息
List<TestCaseInfo> parseCasesExcel(byte[] excelData)
```

#### 用例信息字段
- 用例名称 (caseName)
- 用例编号 (caseNumber)
- 用例逻辑组网 (networkTopology)
- 用例业务大类 (businessCategory)
- 用例App (appName)
- 用例测试步骤 (testSteps)
- 用例预期结果 (expectedResult)

### 3. 脚本文件匹配功能

#### 后端实现
- **ScriptMatchService**：脚本匹配服务
  - 匹配用例编号与Python脚本文件
  - 支持完全匹配、部分匹配、缺失脚本等场景
  - 提供匹配结果统计和详细信息

#### 核心方法
```java
// 匹配用例编号与脚本文件
ScriptMatchResult matchScripts(List<String> caseNumbers, List<String> scriptFileNames)

// 验证单个用例编号是否有对应脚本
boolean hasScriptForCase(String caseNumber, List<String> scriptFileNames)
```

#### 匹配规则
- 用例编号为"TC001"的用例对应脚本文件"TC001.py"
- 脚本文件必须位于scripts目录下
- 支持.py扩展名的Python脚本文件

### 4. 数据库设计

#### 新增表结构
```sql
-- 测试用例表
CREATE TABLE test_case (
    id BIGSERIAL PRIMARY KEY,
    test_case_set_id BIGINT NOT NULL,
    case_name VARCHAR(200) NOT NULL,
    case_number VARCHAR(100) NOT NULL,
    network_topology VARCHAR(500),
    business_category VARCHAR(200),
    app_name VARCHAR(200),
    test_steps TEXT,
    expected_result TEXT,
    script_exists BOOLEAN NOT NULL DEFAULT FALSE,
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 实体类
- **TestCase**：测试用例实体类
  - 与TestCaseSet建立多对一关系
  - 包含所有用例信息字段
  - 支持脚本存在状态标记

### 5. 服务层集成

#### 更新TestCaseSetService
- 集成压缩包解析功能
- 在用例集上传时自动解析和存储用例信息
- 提供用例查询和缺失脚本统计功能

#### 核心流程
1. 上传压缩包文件
2. 验证压缩包结构（包含cases.xlsx和scripts目录）
3. 提取cases.xlsx文件内容
4. 解析Excel文件获取用例信息
5. 提取scripts目录下的脚本文件名
6. 匹配用例编号与脚本文件
7. 创建TestCase实体并保存到数据库
8. 记录脚本存在状态

### 6. API接口

#### 新增接口
```java
// 获取用例集的测试用例列表
GET /api/test-case-sets/{id}/test-cases

// 获取用例集中没有脚本的测试用例列表
GET /api/test-case-sets/{id}/missing-scripts
```

#### 响应格式
```json
// 测试用例列表响应
{
  "data": [
    {
      "id": 1,
      "testCaseSetId": 1,
      "caseName": "测试用例1",
      "caseNumber": "TC001",
      "networkTopology": "网络拓扑1",
      "businessCategory": "业务大类1",
      "appName": "应用1",
      "testSteps": "测试步骤",
      "expectedResult": "预期结果",
      "scriptExists": true,
      "createdTime": "2025-01-27T10:00:00",
      "updatedTime": "2025-01-27T10:00:00"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 10
}

// 缺失脚本响应
{
  "data": [
    {
      "id": 2,
      "caseNumber": "TC002",
      "caseName": "测试用例2",
      "scriptExists": false
    }
  ],
  "count": 1
}
```

### 7. 前端实现

#### 新增组件
- **TestCaseDetails**：测试用例详情组件
  - 显示用例集的所有测试用例
  - 统计信息展示（总用例数、已匹配、缺失脚本、匹配率）
  - 缺失脚本警告提示
  - 支持查看所有用例和缺失脚本两个标签页
  - 提供用例详情查看功能

#### 更新组件
- **TestCaseSetManagement**：用例集管理组件
  - 添加"查看测试用例"按钮
  - 集成TestCaseDetails组件
  - 支持用例详情查看功能

#### 类型定义
```typescript
// 测试用例类型
interface TestCase {
  id: number
  testCaseSetId: number
  caseName: string
  caseNumber: string
  networkTopology?: string
  businessCategory?: string
  appName?: string
  testSteps?: string
  expectedResult?: string
  scriptExists: boolean
  createdTime: string
  updatedTime: string
}
```

### 8. 单元测试

#### 测试覆盖
- **TestCaseTest**：测试用例实体类测试
- **TestCaseServiceTest**：测试用例服务类测试
- **ArchiveParseServiceTest**：压缩包解析服务测试
- **ExcelParseServiceTest**：Excel解析服务测试
- **ScriptMatchServiceTest**：脚本匹配服务测试

#### 测试内容
- 构造函数和getter/setter方法测试
- 业务逻辑方法测试
- 异常情况处理测试
- 边界条件测试
- 数据验证测试

## 使用说明

### 1. 压缩包格式要求
- 支持ZIP和TAR.GZ格式
- 必须包含cases.xlsx文件（用例信息）
- 必须包含scripts目录（Python脚本文件）
- 脚本文件命名规则：用例编号.py（如TC001.py）

### 2. Excel文件格式要求
- 第一行为标题行，包含以下列：
  - 用例名称
  - 用例编号
  - 用例逻辑组网
  - 用例业务大类
  - 用例App
  - 用例测试步骤
  - 用例预期结果
- 从第二行开始为数据行

### 3. 操作流程
1. 在用例集管理页面点击"上传用例集"按钮
2. 选择符合格式要求的压缩包文件
3. 系统自动解析压缩包并存储用例信息
4. 上传成功后，点击"查看测试用例"按钮查看详情
5. 在详情页面可以查看所有用例和缺失脚本信息

### 4. 缺失脚本处理
- 系统会自动检测缺失脚本的用例
- 在用例详情页面会显示缺失脚本警告
- 可以切换到"缺失脚本"标签页查看具体缺失的用例
- 需要手动补充对应的Python脚本文件

## 技术栈

### 后端技术
- Spring Boot 2.7.18
- Spring Data JPA
- PostgreSQL
- Apache POI 5.4.0（Excel处理）
- Apache Commons Compress 1.26.1（压缩包处理）
- JUnit 4 + Mockito（单元测试）

### 前端技术
- React 18
- TypeScript
- Ant Design
- i18next（国际化）

## 文件结构

```
backend/src/main/java/com/huawei/dialtest/center/
├── entity/
│   └── TestCase.java                    # 测试用例实体类
├── repository/
│   └── TestCaseRepository.java          # 测试用例数据访问层
├── service/
│   ├── TestCaseService.java             # 测试用例服务类
│   ├── ArchiveParseService.java         # 压缩包解析服务
│   ├── ExcelParseService.java           # Excel解析服务
│   └── ScriptMatchService.java          # 脚本匹配服务
└── controller/
    └── TestCaseSetController.java       # 用例集控制器（已更新）

backend/src/test/java/com/dialtest/center/
├── entity/
│   └── TestCaseTest.java                # 测试用例实体测试
└── service/
    ├── TestCaseServiceTest.java         # 测试用例服务测试
    ├── ArchiveParseServiceTest.java     # 压缩包解析服务测试
    ├── ExcelParseServiceTest.java       # Excel解析服务测试
    └── ScriptMatchServiceTest.java      # 脚本匹配服务测试

frontend/src/
├── components/
│   ├── TestCaseDetails.tsx              # 测试用例详情组件
│   └── TestCaseSetManagement.tsx        # 用例集管理组件（已更新）
├── services/
│   └── testCaseSetService.ts            # 用例集服务（已更新）
└── types/
    └── testCaseSet.ts                   # 类型定义（已更新）

database/
└── migration_add_test_case.sql          # 数据库迁移脚本
```

## 总结

本次实现完成了用例集管理的核心功能，包括：

1. ✅ 压缩包解析功能（ZIP/TAR.GZ）
2. ✅ Excel文件解析功能（cases.xlsx）
3. ✅ 脚本文件匹配功能
4. ✅ 不匹配信息记录功能
5. ✅ 前端页面展示功能
6. ✅ 完整的单元测试覆盖

系统现在可以自动解析上传的压缩包，提取用例信息，匹配脚本文件，并在前端展示详细的用例信息和缺失脚本警告，大大提升了用例集管理的效率和用户体验。
