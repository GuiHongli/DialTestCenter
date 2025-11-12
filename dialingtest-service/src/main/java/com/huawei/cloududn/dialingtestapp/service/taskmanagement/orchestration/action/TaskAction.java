/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto.TaskContext;

/**
 * 任务动作接口，用于状态机状态转换时执行具体业务逻辑
 *
 * @author g00940940
 * @since 2025-11-10
 */
public interface TaskAction {
    /**
     * 执行任务动作
     *
     * @param context 任务上下文
     */
    void execute(TaskContext context);
}

