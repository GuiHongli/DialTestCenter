# Python集成测试框架 - 安装和配置指南

## 📦 安装步骤

### 1. 安装Python依赖

```bash
# 进入项目根目录
cd D:\code\DialTestCenter

# 安装测试框架依赖
pip install -r test/requirements.txt

# 或单个安装
pip install requests psycopg2-binary pytest pytest-cov
```

### 2. 配置数据库

```bash
# 启动PostgreSQL（Docker方式）
docker run --name postgres-test \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  -d postgres:15

# 创建测试数据库
docker exec postgres-test psql -U postgres -c "CREATE DATABASE dialingtest_test;"

# 初始化表结构
docker exec postgres-test psql -U postgres -d dialingtest_test < dialingtest-service/src/main/resources/sql/task_management.sql
```

### 3. 启动后端应用

```bash
# 在另一个terminal中
cd dialingtest-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.datasource.url=jdbc:postgresql://localhost:5432/dialingtest_test"
```

## 🧪 运行测试

```bash
# 进入测试目录
cd test/taskmanagement

# 运行所有测试
python -m pytest -v

# 运行特定场景
pytest -k "test_sc01" -v

# 生成HTML覆盖率报告
pytest --cov=. --cov-report=html

# 查看报告：htmlcov/index.html
```

## ⚙️ 环境配置

### 方式1：环境变量

```bash
export API_BASE_URL=http://localhost:8080
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=dialingtest_test
export DB_USER=postgres
export DB_PASSWORD=postgres
export TEST_ENV=local
export DEBUG=True
```

### 方式2：修改config.py

编辑 `test/taskmanagement/config.py` 中的配置值。

## 📊 测试结果

运行成功后，在 `logs/` 目录中会生成日志文件

```bash
# 查看最新日志
tail logs/test_*.log

# 实时监控日志
tail -f logs/test_*.log
```

## 🐛 故障排除

| 问题 | 解决方案 |
|------|--------|
| `psycopg2.OperationalError` | 检查数据库是否启动，用户名密码是否正确 |
| `ConnectionRefusedError` | 检查后端API是否启动（http://localhost:8080） |
| `ModuleNotFoundError: requests` | 运行 `pip install -r test/requirements.txt` |
| `AssertionError: Expected status 202` | 检查请求数据格式，查看日志详情 |

## 📚 文件清单

```
test/
├── taskmanagement/
│   ├── __init__.py              # 包初始化
│   ├── config.py                # 配置管理
│   ├── utils.py                 # 工具函数
│   ├── base.py                  # 基础测试类
│   ├── test_sc01_start_task.py  # SC-01测试
│   ├── README.md                # 使用指南
│   ├── SUMMARY.md               # 完成总结
│   └── INSTALLATION.md          # 本文件
├── requirements.txt             # Python依赖
└── logs/                        # 日志输出
```
