/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.listener;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

/**
 * 任务状态变更监听器接口，用于在状态转换时执行额外逻辑
 *
 * @author g00940940
 * @since 2025-11-10
 */
public interface TaskStateChangeListener {
    /**
     * 状态变更回调方法
     *
     * @param from 源状态
     * @param to 目标状态
     * @param context 任务上下文
     */
    void onStateChanged(TaskState from, TaskState to, TaskContext context);
}

