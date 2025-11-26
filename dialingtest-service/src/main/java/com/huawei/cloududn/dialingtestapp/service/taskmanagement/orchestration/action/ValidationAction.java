/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.impl.DialingTestTaskImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 验证场景拨测动作：进入 START_VALIDATION/START_TRAINING_DIALING/START_WHITELIST 时触发。
 *
 * <p>最小实现：调用 DialingTestTaskImpl 启动异步拨测任务，将 async_job_id 写入上下文。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Component
public class ValidationAction implements TaskAction {
    private static final Logger logger = LoggerFactory.getLogger(ValidationAction.class);
    @Autowired
    private DialingTestTaskImpl dialingTestTask;

    @Override
    public void execute(TaskContext context) {
        if (context == null) {
            logger.warn("Task context is null");
            return;
        } else {
            String jobId = dialingTestTask.start(context);
            context.getData().put("async_job_id", jobId);
            logger.info("ValidationAction started dialing job: {}", jobId);
        }
    }
}


