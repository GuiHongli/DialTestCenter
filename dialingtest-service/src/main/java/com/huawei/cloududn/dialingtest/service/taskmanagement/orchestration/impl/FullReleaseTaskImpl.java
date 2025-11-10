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
 * 全量发布任务实现（最小可用版本）。
 *
 * <p>此实现仅模拟提交一个异步任务，并返回生成的 async_job_id。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Component
public class FullReleaseTaskImpl {
    private static final Logger logger = LoggerFactory.getLogger(FullReleaseTaskImpl.class);

    /**
     * 启动全量发布异步任务。
     *
     * @param context 任务上下文
     * @return 异步任务ID
     */
    public String start(TaskContext context) {
        String jobId = "job-fullrelease-" + UUID.randomUUID();
        logger.info("Submit full release async job: {}", jobId);
        return jobId;
    }
}



