/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.action;

import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl.ReplayTaskImpl;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;

/**
 * 回放动作：进入 START_MODEL_REPLAY/START_FULL_REPLAY 时触发。
 *
 * <p>最小实现：调用 ReplayTaskImpl 启动异步任务，并写入 async_job_id。</p>
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Component
public class ReplayAction implements Action<TaskState, TaskEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ReplayAction.class);

    @Autowired
    private ReplayTaskImpl replayTask;

    @Override
    public void execute(StateContext<TaskState, TaskEvent> context) {
        Object ctxObj = context.getExtendedState().getVariables().get("TASK_CONTEXT");
        if (!(ctxObj instanceof TaskContext)) {
            logger.warn("Missing TASK_CONTEXT in extended state");
            return;
        } else {
            TaskContext taskContext = (TaskContext) ctxObj;
            String jobId = replayTask.start(taskContext);
            taskContext.getData().put("async_job_id", jobId);
            logger.info("ReplayAction started replay job: {}", jobId);
        }
    }
}



