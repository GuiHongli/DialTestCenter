/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl.ReplayTaskImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 回放动作：进入 START_MODEL_REPLAY/START_FULL_REPLAY 时触发。
 *
 * <p>最小实现：调用 ReplayTaskImpl 启动异步任务，并写入 async_job_id。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Component
public class ReplayAction implements TaskAction {
    private static final Logger logger = LoggerFactory.getLogger(ReplayAction.class);
    @Autowired
    private ReplayTaskImpl replayTask;

    @Override
    public void execute(TaskContext context) {
        if (context == null) {
            logger.warn("Task context is null");
            return;
        } else {
            String jobId = replayTask.start(context);
            context.getData().put("async_job_id", jobId);
            logger.info("ReplayAction started replay job: {}", jobId);
        }
    }
}



