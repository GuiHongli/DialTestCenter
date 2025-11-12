/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state;

/**
 * 任务编排事件枚举。
 *
 * <p>采用统一事件：TASK_SUCCESS / TASK_FAILED / STOP。由Orchestrator将外部结果映射到此事件。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
public enum TaskEvent {
    TASK_SUCCESS,
    TASK_FAILED,
    STOP
}


