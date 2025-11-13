/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration;

import com.huawei.cloududn.dialingtest.model.TaskEntity;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TaskMgmtService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskEvent;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * 任务编排服务单元测试
 *
 * @author g00940940
 * @since 2025-11-10
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskOrchestratorServiceTest {
    @Mock
    private TaskStateMachine taskStateMachine;
    @Mock
    private TaskMgmtService taskMgmtService;
    @InjectMocks
    private TaskOrchestratorService orchestrator;

    @Test
    public void testSendResultEvent_Success() {
        TaskEntity task = new TaskEntity();
        task.setId(1);
        task.setContext("{}");
        Mockito.when(taskMgmtService.findById(1L)).thenReturn(task);
        Mockito.when(taskStateMachine.sendEvent(Mockito.any(TaskState.class), Mockito.any(TaskEvent.class), Mockito.any(TaskContext.class)))
                .thenReturn(TaskState.START_TRAINING_DIALING);
        orchestrator.sendResultEvent(1L, true);
        Mockito.verify(taskStateMachine).sendEvent(Mockito.eq(TaskState.START_VALIDATION), Mockito.eq(TaskEvent.TASK_SUCCESS), Mockito.any(TaskContext.class));
        Mockito.verify(taskMgmtService).updateStatusAndContext(Mockito.eq(1L), Mockito.eq("RUNNING"), Mockito.isNull(), Mockito.anyString());
    }

    @Test
    public void testSendResultEvent_TaskNotFound() {
        Mockito.when(taskMgmtService.findById(999L)).thenReturn(null);
        try {
            orchestrator.sendResultEvent(999L, true);
            fail("Expected ResponseStatusException to be thrown");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            assertTrue(e.getMessage().contains("Task not found"));
        }
    }
}


