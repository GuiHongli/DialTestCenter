# 拨测任务管理 - Python集成测试框架

## 项目结构

```
test/taskmanagement/
├── __init__.py                      # 包初始化
├── config.py                        # 全局配置（API端点、数据库、超时等）
├── utils.py                         # 工具函数（APIClient、数据库操作、断言等）
├── base.py                          # 基础测试类（所有测试的父类）
├── test_sc01_start_task.py          # SC-01: 启动任务测试用例
├── test_sc02_query_task.py          # SC-02: 查询任务测试用例（待创建）
├── test_sc03_template_management.py # SC-03: 模板管理测试用例（待创建）
├── test_sc04_callback.py            # SC-04: 异步回调测试用例（待创建）
├── test_sc05_scheduled_task.py      # SC-05: 定时任务测试用例（待创建）
├── test_sc06_data_consistency.py    # SC-06: 数据一致性测试用例（待创建）
└── README.md                        # 本文件
```

## 快速开始

### 1. 环境准备

```bash
# 安装依赖
pip install requests psycopg2-binary

# 设置环境变量（可选）
export TEST_ENV=local
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=dialingtest_test
export DB_USER=postgres
export DB_PASSWORD=postgres
```

### 2. 启动测试

```bash
# 进入项目目录
cd test/taskmanagement

# 运行所有测试
python -m pytest

# 或使用unittest
python -m unittest discover -p 'test_*.py'

# 运行特定测试类
python -m unittest test_sc01_start_task.TestStartTask

# 运行单个测试方法
python -m unittest test_sc01_start_task.TestStartTask.test_tc_01_001_start_validation_task_success

# 生成覆盖率报告
pytest --cov=. --cov-report=html
```

### 3.1 回调打桩脚本（callback_driver.py）

用于模拟外部系统对后端 `CallbackController` 的 SUCCESS/FAILED 回调，上报节奏与结果可配置，支持 HTTPS 自签证书跳过校验。脚本位于 `test/taskmanagement/callback_driver.py`，预置场景位于 `test/taskmanagement/scenarios.json`。

基础信息：
- 服务基础地址（默认）：`https://localhost:8087/dialingtest`
- 回调路径（默认）：`/api/callbacks/notify`
- 任务启动路径（默认）：`/api/tasks/start`

依赖安装：
```bash
pip install -r test/requirements.txt
```

两种使用模式：

1) 前端联动（推荐）
- 前端点击“启动任务”，拿到返回的主任务ID（id/mainTaskId）。
- 用脚本仅做“回调驱动”：
```bash
python test/taskmanagement/callback_driver.py --main-task-id 101 --scenario happy_path --insecure
```

2) 后端独立（脚本自动创建任务 + 回调）
```bash
python test/taskmanagement/callback_driver.py --auto-start --business-type VPN_BLOCK --script-names vpn_app_001.py --target-ues ue_serial_12345 --start-scenario TRAINING --scenario happy_path --insecure
```

自定义计划（不依赖预置场景）：
```bash
python test/taskmanagement/callback_driver.py --main-task-id 101 --plan "SUCCESS:5,FAILED:3,SUCCESS:5" --insecure
```

常用参数：
- `--base-url`：基础地址，默认 `https://localhost:8087/dialingtest`
- `--endpoint`：回调路径，默认 `/api/callbacks/notify`
- `--start-endpoint`：任务启动路径，默认 `/api/tasks/start`
- `--scenario`：预置场景名（来自 `scenarios.json`，用于回调）
- `--start-scenario`：启动任务请求的 `scenario` 值（如 `TRAINING`，默认 `TRAINING`）
- `--plan`：自定义计划，例如 `SUCCESS:5,FAILED:3,SUCCESS:5`（状态:延时秒）
- `--result-data`：为未配置步级 result_data 的步骤提供全局 result_data（JSON字符串）
- `--auto-start`：脚本先创建主任务，再根据场景/计划驱动回调
- `--business-type`、`--script-names`、`--target-ues`、`--failed-apps`：与 `--auto-start` 一起使用
- `--insecure`：本地自签证书场景下跳过 TLS 校验

注意事项：
- 状态机当前配置下，`START_VALIDATION` 收到 SUCCESS 会直接到 `FINAL`。若需走完整链路，可先回调 FAILED 进入训练，再逐步 SUCCESS 推进。
- 不建议脚本自动“挑最近的任务”进行回调，容易在并发场景误回调。推荐使用明确的主任务ID。

### 3. 查看测试日志

测试日志保存在 `logs/` 目录下，文件名格式为 `test_YYYYMMDD_HHMMSS.log`

```bash
# 查看最新的日志
tail -f logs/test_*.log
```

## 核心模块说明

### config.py - 配置管理

定义了所有全局配置：

```python
# API配置
BASE_URL = 'http://localhost:8080'
API_ENDPOINTS = {
    'START_TASK': '/api/tasks/start',
    'GET_TASKS': '/api/tasks',
    ...
}

# 数据库配置
DATABASE_CONFIG = {
    'host': 'localhost',
    'database': 'dialingtest_test',
    ...
}

# 性能基准
PERFORMANCE_BASELINE = {
    'create_task': 500,  # 毫秒
    'query_task': 100,
}
```

### utils.py - 工具类

#### APIClient - API请求客户端

```python
from utils import APIClient

client = APIClient()
response = client.post('/api/tasks/start', {'businessType': 'VPN_BLOCK', ...})
response = client.get('/api/tasks', params={'page': 0, 'size': 20})
```

#### Assertions - 断言助手

```python
from utils import Assertions

assertions = Assertions()
assertions.assert_status_code(response, 202)
assertions.assert_json_field(response, 'id', expected_value=100)
assertions.assert_json_field_not_null(response, 'startTime')
assertions.assert_response_time(response_time, 500)  # 毫秒
```

#### DatabaseHelper - 数据库操作

```python
from utils import DatabaseHelper

db = DatabaseHelper()
task = db.get_task_by_id(100)
latest_task = db.get_latest_task()
db.truncate_table('task')
```

#### TestDataBuilder - 测试数据生成

```python
from utils import TestDataBuilder

builder = TestDataBuilder()

# 构建启动任务请求
start_task_data = builder.build_start_task_request(
    scenario='VALIDATION',
    failed_apps=['app1', 'app2']
)

# 构建创建模板请求
template_data = builder.build_create_template_request(
    name='每周VPN验证',
    cron='0 0 1 ? * 1'
)

# 构建回调请求
callback_data = builder.build_callback_request(
    task_id=100,
    status='SUCCESS'
)
```

### base.py - 基础测试类

所有测试类都应继承 `BaseTestCase`：

```python
from base import BaseTestCase

class TestExample(BaseTestCase):
    """测试示例"""
    
    def setUp(self):
        """测试前准备"""
        super().setUp()
        self.truncate_task_table()
    
    def test_example(self):
        """测试方法"""
        # 启动任务
        response = self.start_task(scenario='VALIDATION')
        self.assertions.assert_status_code(response, 202)
        
        # 查询任务
        task_id = response.json()['id']
        task_response = self.get_task_by_id(task_id)
        self.assertions.assert_status_code(task_response, 200)
        
        # 验证数据库
        task_from_db = self.get_task_from_db(task_id)
        self.assertIsNotNone(task_from_db)
```

## 测试用例编写规范

### 命名规范

- 类名：`Test<ScenarioName>`，如 `TestStartTask`
- 方法名：`test_tc_<number>_<description>`，如 `test_tc_01_001_start_validation_task_success`

### AAA模式

```python
def test_example(self):
    # Arrange - 准备测试数据
    request_data = self.data_builder.build_start_task_request(...)
    
    # Act - 执行操作
    response = self.api.post('/api/tasks/start', request_data)
    
    # Assert - 验证结果
    self.assertions.assert_status_code(response, 202)
    self.assertions.assert_json_field_not_null(response, 'id')
```

### 响应时间测量

```python
from utils import measure_response_time

response, response_time = measure_response_time(
    self.start_task,
    scenario='VALIDATION'
)

# 验证响应时间在基准内
self.assertions.assert_response_time(
    response_time,
    PERFORMANCE_BASELINE['create_task']
)
```

## 已实现的测试用例

### SC-01: 人工启动拨测任务（test_sc01_start_task.py）

- ✓ TC-01-001: 启动验证场景拨测任务
- ✓ TC-01-002: 启动训练场景拨测任务
- ✓ TC-01-003: 参数验证失败
- ✓ TC-01-004: scriptNames为空（额外）
- ✓ TC-01-005: targetUes为空（额外）
- ✓ TC-01-006: scenario大小写敏感性（额外）

## 待创建的测试用例

### SC-02: 任务查询（test_sc02_query_task.py）

- TC-02-001: 分页查询主任务列表
- TC-02-002: 查询具体任务详情
- TC-02-003: 查询不存在的任务
- TC-02-004: 分页参数边界测试

### SC-03: 模板管理（test_sc03_template_management.py）

- TC-03-001: 创建新模板
- TC-03-002: 模板唯一性约束
- TC-03-003: 更新模板
- TC-03-004: 删除模板
- TC-03-005: 查询所有模板

### SC-04: 异步回调（test_sc04_callback.py）

- TC-04-001: 异步任务完成回调（SUCCESS）
- TC-04-002: 异步任务失败回调（FAILED）
- TC-04-003: 重复回调幂等性
- TC-04-004: 无效的主任务ID

### SC-05: 定时任务（test_sc05_scheduled_task.py）

- TC-05-001: 模板定时任务创建

### SC-06: 数据一致性（test_sc06_data_consistency.py）

- TC-06-001: 主任务ID自引用验证
- TC-06-002: Context字段JSON格式验证
- TC-06-003: 时间戳字段自动设置

## 常见问题

### Q: 如何修改API基础URL？

A: 修改 `config.py` 中的 `BASE_URL` 或设置环境变量 `API_BASE_URL`

```python
# config.py 中
BASE_URL = os.getenv('API_BASE_URL', 'http://localhost:8080')
```

### Q: 如何运行特定场景的测试？

A: 使用 `-k` 选项过滤

```bash
# 运行所有SC-01的测试
pytest -k "test_sc01" -v

# 运行所有成功场景的测试
pytest -k "success" -v
```

### Q: 如何调试测试失败？

A: 设置环境变量启用调试日志

```bash
export DEBUG=True
python -m pytest test_sc01_start_task.py::TestStartTask::test_tc_01_001_start_validation_task_success -v -s
```

### Q: 如何跳过某个测试？

A: 使用 `@unittest.skip` 装饰器

```python
@unittest.skip("Waiting for backend implementation")
def test_example(self):
    pass
```

## 性能基准

| 操作 | 目标时间 | 备注 |
|-----|---------|------|
| 创建任务 | < 500ms | 包括参数验证和状态机初始化 |
| 查询单个任务 | < 100ms | 数据库查询+序列化 |
| 查询任务列表 | < 200ms | 分页查询+序列化 |
| 创建模板 | < 300ms | 唯一性检查+DB插入 |
| 异步回调处理 | < 1000ms | 状态机转换+DB更新 |

## CI/CD 集成

### GitHub Actions 示例

```yaml
name: Integration Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
    
    steps:
      - uses: actions/checkout@v2
      - uses: actions/setup-python@v2
        with:
          python-version: 3.9
      
      - name: Install dependencies
        run: pip install -r requirements.txt
      
      - name: Run tests
        run: pytest test/taskmanagement --cov
```

## 参考文档

- [集成测试设计文档](../AR软件实现设计文档/拨测任务管理/拨测任务管理后端组件集成测试设计.md)
- [系统设计文档](../AR软件实现设计文档/拨测任务管理/拨测任务管理后端组件软件实现设计.md)

