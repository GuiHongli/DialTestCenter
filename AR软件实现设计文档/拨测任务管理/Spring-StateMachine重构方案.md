# Spring State Machine 重构方案

## 一、重构背景

项目当前使用Spring State Machine管理任务编排工作流，但该库不符合项目依赖要求，需要移除并替换为自研实现。

**核心要求**：保证重构前后业务逻辑完全一致。

---

## 二、当前使用分析

### 2.1 使用场景
任务编排工作流管理（MLOps闭环流程）

### 2.2 状态流转
9个状态节点：
```
START_VALIDATION → START_TRAINING_DIALING → START_MODEL_TRAIN → START_MODEL_REPLAY 
→ START_GRAY_VALIDATION → START_FULL_REPLAY → START_WHITELIST → START_FULL_RELEASE → FINAL
```

### 2.3 事件类型
- `TASK_SUCCESS`：任务成功
- `TASK_FAILED`：任务失败

### 2.4 涉及组件
```
orchestration/
├── config/
│   ├── StateMachineConfig.java         (需删除)
│   └── StateMachineListener.java       (需替换)
├── action/
│   ├── ValidationAction.java           (需修改)
│   ├── TrainModelAction.java           (需修改)
│   ├── ReplayAction.java               (需修改)
│   ├── GrayValidationAction.java       (需修改)
│   └── FullReleaseAction.java          (需修改)
├── state/
│   ├── TaskState.java                  (保持不变)
│   └── TaskEvent.java                  (保持不变)
└── TaskOrchestratorService.java        (需修改)
```

---

## 三、重构方案

### 3.1 核心思路
用 **状态转换表 + 监听器模式** 替代Spring State Machine框架。

### 3.2 新增组件

#### (1) TaskAction 接口
```java
// orchestration/action/TaskAction.java
public interface TaskAction {
    void execute(TaskContext context);
}
```

#### (2) TaskStateChangeListener 接口
```java
// orchestration/listener/TaskStateChangeListener.java
public interface TaskStateChangeListener {
    void onStateChanged(TaskState from, TaskState to, TaskContext context);
}
```

#### (3) TaskStateMachine 类
```java
// orchestration/TaskStateMachine.java
@Component
public class TaskStateMachine {
    // 状态转换表：(当前状态, 事件) -> 下一状态
    private Map<StateEventKey, TaskState> transitionTable;
    
    // 状态Action映射
    private Map<TaskState, TaskAction> stateActions;
    
    // 监听器列表
    private List<TaskStateChangeListener> listeners;
    
    @PostConstruct
    public void init() {
        initTransitionTable();  // 初始化18条转换规则
        initActionTable();      // 初始化状态-Action映射
    }
    
    public TaskState sendEvent(TaskState current, TaskEvent event, TaskContext ctx) {
        // 1. 查表获取下一状态
        // 2. 通知监听器
        // 3. 执行新状态的Action
        // 4. 返回新状态
    }
}
```

#### (4) TaskStatePersistenceListener 类
```java
// orchestration/listener/TaskStatePersistenceListener.java
@Component
public class TaskStatePersistenceListener implements TaskStateChangeListener {
    @Override
    public void onStateChanged(TaskState from, TaskState to, TaskContext context) {
        // 更新context.step = to
        // 持久化到数据库
    }
}
```

### 3.3 修改组件

#### (1) 所有Action类（5个）
```java
// 原代码
implements Action<TaskState, TaskEvent>
execute(StateContext<TaskState, TaskEvent> context)

// 新代码
implements TaskAction
execute(TaskContext context)
```

#### (2) TaskOrchestratorService
```java
// 原代码
StateMachine<TaskState, TaskEvent> machine = stateMachineConfig.build(initial);
machine.getExtendedState().getVariables().put("TASK_CONTEXT", ctx);
machine.start();
machine.sendEvent(event);

// 新代码
ctx.getData().put("taskId", mainTaskId);
TaskState newState = taskStateMachine.sendEvent(currentState, event, ctx);
```

### 3.4 删除组件
- `config/StateMachineConfig.java`
- `config/StateMachineListener.java`
- `pom.xml` 中的 `spring-statemachine-core` 依赖

---

## 四、状态转换规则迁移

从 `StateMachineConfig` 的 66-90 行提取 18 条转换规则：

| 当前状态 | 事件 | 下一状态 |
|---------|------|---------|
| START_VALIDATION | TASK_SUCCESS | FINAL |
| START_VALIDATION | TASK_FAILED | START_TRAINING_DIALING |
| START_TRAINING_DIALING | TASK_SUCCESS | START_MODEL_TRAIN |
| START_TRAINING_DIALING | TASK_FAILED | START_TRAINING_DIALING |
| START_MODEL_TRAIN | TASK_SUCCESS | START_MODEL_REPLAY |
| START_MODEL_TRAIN | TASK_FAILED | FINAL |
| START_MODEL_REPLAY | TASK_SUCCESS | START_GRAY_VALIDATION |
| START_MODEL_REPLAY | TASK_FAILED | FINAL |
| START_GRAY_VALIDATION | TASK_SUCCESS | START_FULL_REPLAY |
| START_GRAY_VALIDATION | TASK_FAILED | FINAL |
| START_FULL_REPLAY | TASK_SUCCESS | START_WHITELIST |
| START_FULL_REPLAY | TASK_FAILED | FINAL |
| START_WHITELIST | TASK_SUCCESS | START_FULL_RELEASE |
| START_WHITELIST | TASK_FAILED | FINAL |
| START_FULL_RELEASE | TASK_SUCCESS | FINAL |
| START_FULL_RELEASE | TASK_FAILED | FINAL |

---

## 五、实施步骤

### Step 1: 创建新接口和类（不影响现有代码）
1. 创建 `TaskAction.java`
2. 创建 `TaskStateChangeListener.java`
3. 创建 `TaskStateMachine.java`
4. 创建 `TaskStatePersistenceListener.java`

### Step 2: 修改Action类
5个Action类实现新接口 `TaskAction`，保持原有逻辑不变

### Step 3: 重构Orchestrator
修改 `TaskOrchestratorService.sendResultEvent()` 方法，使用新的状态机

### Step 4: 验证测试
运行单元测试和集成测试，确保所有场景正常

### Step 5: 清理旧代码
- 删除 `StateMachineConfig.java`
- 删除 `StateMachineListener.java`  
- 删除 pom.xml 中的依赖

---

## 六、业务一致性保证

### 6.1 核心逻辑不变
- ✅ 18条状态转换规则完全一致
- ✅ Action执行时机和顺序不变
- ✅ 状态持久化机制不变
- ✅ 幂等性检查逻辑保留
- ✅ TaskContext传递方式不变

### 6.2 测试验证点
1. **转换规则测试**：验证18条规则的输入输出
2. **工作流测试**：完整流程 START_VALIDATION → FINAL
3. **幂等性测试**：重复事件不会重复执行
4. **并发测试**：多任务互不干扰
5. **异常测试**：Action失败的处理逻辑

---

## 七、关键实现细节

### 7.1 状态转换表实现
```java
private void initTransitionTable() {
    transitionTable.put(key(START_VALIDATION, TASK_SUCCESS), FINAL);
    transitionTable.put(key(START_VALIDATION, TASK_FAILED), START_TRAINING_DIALING);
    // ... 其他16条规则
}

private StateEventKey key(TaskState state, TaskEvent event) {
    return new StateEventKey(state, event);
}
```

### 7.2 监听器通知机制
```java
private void notifyListeners(TaskState from, TaskState to, TaskContext ctx) {
    for (TaskStateChangeListener listener : listeners) {
        listener.onStateChanged(from, to, ctx);
    }
}
```

### 7.3 Action执行时机
进入新状态后立即执行对应的Action（与原逻辑一致）

---

## 八、风险评估

| 风险项 | 影响 | 缓解措施 |
|--------|------|----------|
| 转换规则遗漏 | 高 | 对照原代码逐条验证 |
| 并发安全问题 | 中 | 每个任务独立实例，无共享状态 |
| 测试覆盖不足 | 中 | 补充单元测试和集成测试 |

---

## 九、工作量估算

| 任务 | 工时 |
|------|------|
| 创建新组件（4个） | 3小时 |
| 修改Action类（5个） | 1小时 |
| 重构Orchestrator | 2小时 |
| 单元测试 | 2小时 |
| 集成测试 | 2小时 |
| **总计** | **1个工作日** |

---

## 十、参考文档

- 原设计文档：`拨测任务管理后端组件软件实现设计.md`
- 核心流程图：`5.3核心流程图.png`
- 原StateMachineConfig：`StateMachineConfig.java` (66-90行)

---

**版本历史**
- v1.0 (2025-11-10): 初始版本

