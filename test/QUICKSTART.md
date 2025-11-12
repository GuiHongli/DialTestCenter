# Python集成测试框架 - 快速开始指南

## 🚀 5分钟快速上手

### Step 1: 安装依赖 (1分钟)

```bash
pip install -r test/requirements.txt
```

### Step 2: 启动数据库 (1分钟)

```bash
docker run --name postgres-test -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15
docker exec postgres-test psql -U postgres -c "CREATE DATABASE dialingtest_test;"
docker exec postgres-test psql -U postgres -d dialingtest_test < dialingtest-service/src/main/resources/sql/task_management.sql
```

### Step 3: 启动后端API (1分钟)

```bash
cd dialingtest-service
mvn spring-boot:run
```

### Step 4: 运行测试 (2分钟)

```bash
cd test/taskmanagement
python -m pytest -v
```

## 📂 项目结构

```
test/taskmanagement/
├── __init__.py                      # 包初始化
├── config.py                        # 全局配置（API、DB、超时等）
├── utils.py                         # 工具类（API、断言、DB操作等）
├── base.py                          # 基础测试类（所有测试继承）
├── test_sc01_start_task.py          # ✓ SC-01: 启动任务 (6个测试)
├── test_sc02_query_task.py          # ⏳ SC-02: 查询任务 (待创建)
├── test_sc03_template_management.py # ⏳ SC-03: 模板管理 (待创建)
├── test_sc04_callback.py            # ⏳ SC-04: 异步回调 (待创建)
├── test_sc05_scheduled_task.py      # ⏳ SC-05: 定时任务 (待创建)
├── test_sc06_data_consistency.py    # ⏳ SC-06: 数据一致性 (待创建)
├── README.md                        # 详细使用指南
├── SUMMARY.md                       # 完成总结
└── INSTALLATION.md                  # 安装配置指南
```

## 🧪 核心API

### 启动任务

```python
from base import BaseTestCase

class TestExample(BaseTestCase):
    def test_start_task(self):
        # 启动验证场景任务
        response = self.start_task(scenario='VALIDATION')
        
        # 验证状态码
        self.assertions.assert_status_code(response, 202)
        
        # 验证响应字段
        self.assertions.assert_json_field(response, 'status', 'RUNNING')
```

### 查询任务

```python
# 查询任务列表（分页）
response = self.get_tasks(page=0, size=20)

# 查询特定任务
response = self.get_task_by_id(100)

# 从数据库验证
task = self.get_task_from_db(100)
```

### 管理模板

```python
# 创建模板
response = self.create_template(
    name='每周VPN验证',
    cron='0 0 1 ? * 1'
)

# 查询模板
response = self.get_template_by_id(1)

# 更新模板
response = self.update_template(1, enabled=False)

# 删除模板
response = self.delete_template(1)
```

### 异步回调

```python
# 模拟微调中心回调
response = self.notify_callback(
    task_id=100,
    status='SUCCESS',
    result_data={'modelName': 'model-v1.2.3'}
)
```

## 📊 已实现的测试用例

### SC-01: 启动任务 (6个测试) ✓

- `test_tc_01_001_start_validation_task_success` - 启动验证场景
- `test_tc_01_002_start_training_task_with_failed_apps` - 启动训练场景
- `test_tc_01_003_start_task_with_null_business_type` - 参数验证
- `test_tc_01_004_start_task_with_empty_script_names` - 空脚本列表
- `test_tc_01_005_start_task_with_empty_target_ues` - 空UE列表
- `test_tc_01_006_start_task_scenario_case_sensitivity` - 大小写处理

## 🔧 常用命令

```bash
# 运行所有测试
pytest -v

# 运行特定场景
pytest -k "test_sc01" -v

# 运行单个测试
pytest test_sc01_start_task.py::TestStartTask::test_tc_01_001_start_validation_task_success -v

# 生成覆盖率报告
pytest --cov=. --cov-report=html

# 查看实时日志
tail -f logs/test_*.log

# 仅显示日志
pytest test_sc01_start_task.py -v -s
```

## 🎯 下一步

### 立即可做（15分钟）
- [x] 框架搭建 ✓
- [x] SC-01测试实现 ✓  
- [ ] 学习README.md中的编写规范
- [ ] 运行现有测试确保通过

### 本周完成（2小时）
- [ ] 实现SC-02: 查询任务 (4个用例)
- [ ] 实现SC-03: 模板管理 (5个用例)
- [ ] 实现SC-04: 异步回调 (4个用例)
- [ ] 实现SC-05: 定时任务 (1个用例)
- [ ] 实现SC-06: 数据一致性 (3个用例)

## 📚 详细文档

- [README.md](taskmanagement/README.md) - 完整使用指南和最佳实践
- [SUMMARY.md](taskmanagement/SUMMARY.md) - 项目完成情况总结
- [INSTALLATION.md](taskmanagement/INSTALLATION.md) - 详细安装配置步骤
- [集成测试设计文档](../AR软件实现设计文档/拨测任务管理/拨测任务管理后端组件集成测试设计.md) - 20个用例的详细设计

## 💡 特点

✅ **模块化设计** - 公共逻辑提取到base.py和utils.py  
✅ **易于扩展** - 新测试只需继承BaseTestCase  
✅ **完整日志** - 所有请求/响应自动记录  
✅ **性能监测** - 内置响应时间测量  
✅ **数据库验证** - 支持端到端数据一致性检查  
✅ **环境灵活** - 支持多环境配置  
✅ **CI/CD友好** - 易于集成自动化流程  

## 🔗 文件导航

| 文件 | 用途 |
|------|------|
| `config.py` | API端点、DB连接、性能基准配置 |
| `utils.py` | APIClient、数据库操作、断言、数据生成 |
| `base.py` | 基础测试类，提供便利方法 |
| `test_sc*.py` | 各场景的具体测试用例 |

---

**开始测试吧！** 🎉  
如有问题，参考 [README.md](taskmanagement/README.md) 中的FAQ部分。
