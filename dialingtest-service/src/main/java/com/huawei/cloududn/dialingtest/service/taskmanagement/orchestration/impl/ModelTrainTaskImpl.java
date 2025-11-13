/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 模型训练任务实现（最小可用版本）。
 *
 * <p>此实现模拟向“微调中心”提交训练任务，并返回 async_job_id。</p>
 */
@Component
public class ModelTrainTaskImpl {
    private static final Logger logger = LoggerFactory.getLogger(ModelTrainTaskImpl.class);

    /**
     * 启动模型训练异步任务。
     *
     * @param context 任务上下文
     * @return 异步任务ID
     */
    public String start(TaskContext context) {
        String jobId = "job-train-" + UUID.randomUUID();
        logger.info("Submit model training async job: {}", jobId);
        return jobId;
    }
}


