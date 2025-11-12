/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.state.TaskState;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态机上下文对象，序列化为任务表的context字段。
 *
 * <p>忽略未知字段，方便兼容测试数据中的扩展键。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TaskContext {
    private TaskState step;
    private final Map<String, Object> data = new HashMap<String, Object>();

    public TaskState getStep() {
        return step;
    }

    public void setStep(TaskState step) {
        this.step = step;
    }

    public Map<String, Object> getData() {
        return data;
    }
}


