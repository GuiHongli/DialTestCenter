/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.impl.FullReleaseTaskImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 全量发布动作：进入 START_FULL_RELEASE 时触发。
 *
 * <p>最小实现：调用 FullReleaseTaskImpl 启动异步任务，并写入 async_job_id。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Component
public class FullReleaseAction implements TaskAction {
    private static final Logger logger = LoggerFactory.getLogger(FullReleaseAction.class);
    @Autowired
    private FullReleaseTaskImpl fullReleaseTask;

    @Override
    public void execute(TaskContext context) {
        if (context == null) {
            logger.warn("Task context is null");
            return;
        } else {
            String jobId = fullReleaseTask.start(context);
            context.getData().put("async_job_id", jobId);
            logger.info("FullReleaseAction started full release job: {}", jobId);
        }
    }
}



