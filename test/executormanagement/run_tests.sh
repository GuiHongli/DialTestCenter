#!/bin/bash

# 执行机管理集成测试运行脚本
# 
# 用法:
#   ./run_tests.sh              # 运行所有测试
#   ./run_tests.sh sc01         # 运行 SC-01 场景测试
#   ./run_tests.sh api          # 仅运行 API 测试（不需要 WebSocket）
#   ./run_tests.sh ws           # 运行所有 WebSocket 测试

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    print_info "检查依赖..."
    
    if ! command -v python &> /dev/null; then
        print_error "Python 未安装，请先安装 Python 3.7+"
        exit 1
    fi
    
    if ! python -c "import pytest" &> /dev/null; then
        print_warning "pytest 未安装，正在安装依赖..."
        pip install -r ../requirements.txt
    fi
    
    print_success "依赖检查完成"
}

# 检查环境变量
check_env() {
    print_info "检查环境变量..."
    
    # 检查 API URL
    if [ -z "$EXEC_API_BASE_URL" ]; then
        print_warning "EXEC_API_BASE_URL 未设置，使用默认值: http://localhost:8080"
        export EXEC_API_BASE_URL="http://localhost:8080"
    fi
    
    # 检查数据库配置
    if [ -z "$DB_HOST" ]; then
        print_warning "DB_HOST 未设置，使用默认值: localhost"
        export DB_HOST="localhost"
    fi
    
    if [ -z "$DB_NAME" ]; then
        print_warning "DB_NAME 未设置，使用默认值: dialingtest_test"
        export DB_NAME="dialingtest_test"
    fi
    
    # 检查 WebSocket 配置（仅在启用 WS 测试时）
    if [ "$EXEC_WS_ENABLE" = "1" ]; then
        if [ -z "$EXEC_AGENT_NTLM_HASH" ]; then
            print_error "启用 WebSocket 测试但未设置 EXEC_AGENT_NTLM_HASH"
            print_info "请设置环境变量: export EXEC_AGENT_NTLM_HASH=<your_hash>"
            exit 1
        fi
    fi
    
    print_success "环境变量检查完成"
}

# 运行所有测试
run_all_tests() {
    print_info "运行所有测试..."
    python -m pytest -v --tb=short
}

# 运行特定场景测试
run_scenario_tests() {
    local scenario=$1
    print_info "运行 SC-${scenario} 场景测试..."
    python -m pytest test_sc${scenario}*.py -v --tb=short
}

# 运行 API 测试（不需要 WebSocket）
run_api_tests() {
    print_info "运行 API 测试..."
    python -m pytest test_sc03_rest_api.py test_sc08_db_consistency.py -v --tb=short
}

# 运行 WebSocket 测试
run_ws_tests() {
    print_info "运行 WebSocket 测试..."
    
    if [ "$EXEC_WS_ENABLE" != "1" ]; then
        print_warning "WebSocket 测试未启用，设置 EXEC_WS_ENABLE=1"
        export EXEC_WS_ENABLE=1
    fi
    
    python -m pytest \
        test_sc01_register_auth.py \
        test_sc02_heartbeat.py \
        test_sc04_task_channel.py \
        test_sc05_env_mgmt.py \
        test_sc07_offline.py \
        -v --tb=short
}

# 生成测试报告
generate_report() {
    print_info "生成测试报告..."
    
    # HTML 报告
    if command -v pytest-html &> /dev/null; then
        python -m pytest --html=report.html --self-contained-html
        print_success "HTML 报告已生成: report.html"
    else
        print_warning "pytest-html 未安装，跳过 HTML 报告生成"
    fi
    
    # 覆盖率报告
    if command -v pytest-cov &> /dev/null; then
        python -m pytest --cov=. --cov-report=html --cov-report=term
        print_success "覆盖率报告已生成: htmlcov/index.html"
    else
        print_warning "pytest-cov 未安装，跳过覆盖率报告生成"
    fi
}

# 清理测试数据
cleanup_test_data() {
    print_info "清理测试数据..."
    python -c "
from utils import DatabaseHelper
db = DatabaseHelper()
db.cleanup_test_data(prefix='TEST_')
db.cleanup_test_data(prefix='SN_TEST_')
db.cleanup_test_data(prefix='SN_OFFLINE_')
db.close()
print('测试数据清理完成')
"
    print_success "测试数据清理完成"
}

# 显示帮助信息
show_help() {
    echo "执行机管理集成测试运行脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  all             运行所有测试（默认）"
    echo "  sc01, sc02, ... 运行指定场景的测试"
    echo "  api             仅运行 API 测试（不需要 WebSocket）"
    echo "  ws              运行所有 WebSocket 测试"
    echo "  report          生成测试报告"
    echo "  cleanup         清理测试数据"
    echo "  help            显示此帮助信息"
    echo ""
    echo "环境变量:"
    echo "  EXEC_API_BASE_URL        API 基础 URL"
    echo "  EXEC_WS_URL              WebSocket URL"
    echo "  EXEC_WS_ENABLE           WebSocket 测试开关 (0/1)"
    echo "  EXEC_AGENT_NAME          执行机名称"
    echo "  EXEC_AGENT_USERNAME      Agent 用户名"
    echo "  EXEC_AGENT_NTLM_HASH     NTLM Hash"
    echo "  DB_HOST                  数据库主机"
    echo "  DB_PORT                  数据库端口"
    echo "  DB_NAME                  数据库名称"
    echo "  DB_USER                  数据库用户"
    echo "  DB_PASSWORD              数据库密码"
    echo ""
    echo "示例:"
    echo "  $0                       # 运行所有测试"
    echo "  $0 sc01                  # 运行 SC-01 场景测试"
    echo "  $0 api                   # 运行 API 测试"
    echo "  EXEC_WS_ENABLE=1 $0 ws   # 运行 WebSocket 测试"
    echo "  $0 report                # 生成测试报告"
}

# 主函数
main() {
    local command=${1:-all}
    
    # 切换到脚本所在目录
    cd "$(dirname "$0")"
    
    case "$command" in
        all)
            check_dependencies
            check_env
            run_all_tests
            ;;
        sc[0-9][0-9])
            check_dependencies
            check_env
            scenario="${command:2}"
            run_scenario_tests "$scenario"
            ;;
        api)
            check_dependencies
            check_env
            run_api_tests
            ;;
        ws)
            check_dependencies
            check_env
            run_ws_tests
            ;;
        report)
            check_dependencies
            generate_report
            ;;
        cleanup)
            check_env
            cleanup_test_data
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
    
    print_success "完成！"
}

# 运行主函数
main "$@"

