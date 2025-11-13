/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.impl;

import com.huawei.cloududn.dialingtest.dao.taskmanagement.TaskExecutorMappingDao;
import com.huawei.cloududn.dialingtest.model.Executor;
import com.huawei.cloududn.dialingtest.model.Ue;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorSelectionService;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.TaskInterfaceService;
import com.huawei.cloududn.dialingtest.service.taskmanagement.dto.TaskContext;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.state.TaskState;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

/**
 * 拨测任务实现单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
@RunWith(MockitoJUnitRunner.class)
public class DialingTestTaskImplTest {
    @Mock
    private ExecutorSelectionService executorSelectionService;

    @Mock
    private TaskInterfaceService taskInterfaceService;

    @Mock
    private SessionBindingRegistry sessionBindingRegistry;

    @Mock
    private TaskExecutorMappingDao taskExecutorMappingDao;

    @InjectMocks
    private DialingTestTaskImpl taskImpl;

    private Executor testExecutor;
    private Ue testUe;

    @Before
    public void setUp() {
        testExecutor = new Executor();
        testExecutor.setName("executor1");
        testUe = new Ue();
        testUe.setMsisdn("8613800138000");
        testUe.setInfo("{\"serial\":\"SN001\",\"status\":\"idle\"}");
    }

    @Test
    public void testStart_ReturnsTaskId() {
        ExecutorSelectionService.ExecutorUeInfo executorUeInfo = 
            new ExecutorSelectionService.ExecutorUeInfo(testExecutor, testUe);
        Mockito.when(executorSelectionService.selectIdleExecutorAndUe()).thenReturn(executorUeInfo);
        Mockito.when(sessionBindingRegistry.getSessionId("executor1")).thenReturn("session1");
        TaskContext context = new TaskContext();
        context.setStep(TaskState.START_VALIDATION);
        String taskId = taskImpl.start(context);
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("T_"));
    }

    @Test
    public void testStart_NoAvailableExecutor() {
        Mockito.when(executorSelectionService.selectIdleExecutorAndUe()).thenReturn(null);
        TaskContext context = new TaskContext();
        context.setStep(TaskState.START_VALIDATION);
        String taskId = taskImpl.start(context);
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("T_"));
    }

    @Test
    public void testStart_NoActiveSession() {
        ExecutorSelectionService.ExecutorUeInfo executorUeInfo = 
            new ExecutorSelectionService.ExecutorUeInfo(testExecutor, testUe);
        Mockito.when(executorSelectionService.selectIdleExecutorAndUe()).thenReturn(executorUeInfo);
        Mockito.when(sessionBindingRegistry.getSessionId("executor1")).thenReturn(null);
        TaskContext context = new TaskContext();
        context.setStep(TaskState.START_VALIDATION);
        String taskId = taskImpl.start(context);
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("T_"));
    }

    @Test
    public void testStart_UniqueTaskId() {
        ExecutorSelectionService.ExecutorUeInfo executorUeInfo = 
            new ExecutorSelectionService.ExecutorUeInfo(testExecutor, testUe);
        Mockito.when(executorSelectionService.selectIdleExecutorAndUe()).thenReturn(executorUeInfo);
        Mockito.when(sessionBindingRegistry.getSessionId("executor1")).thenReturn("session1");
        TaskContext context1 = new TaskContext();
        context1.setStep(TaskState.START_VALIDATION);
        TaskContext context2 = new TaskContext();
        context2.setStep(TaskState.START_TRAINING_DIALING);
        String taskId1 = taskImpl.start(context1);
        String taskId2 = taskImpl.start(context2);
        assertNotEquals(taskId1, taskId2);
    }
}

