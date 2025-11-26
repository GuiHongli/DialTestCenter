/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.task;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStartResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStopResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow.WssMessageSender;
import com.huawei.cloududn.dialingtestapp.dao.taskmanagement.TaskExecutorMappingDao;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.TaskOrchestratorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.websocket.Session;

import static org.mockito.Mockito.*;

/**
 * TaskInterfaceService单元测试 - V4协议版本
 * 测试任务下发、停止、结果上报等功能
 *
 * @author g00940940
 * @since 2025-11-16
 */
public class TaskInterfaceServiceTest {

    @Mock
    private WssMessageSender wssMessageSender;

    @Mock
    private TaskOrchestratorService taskOrchestratorService;

    @Mock
    private TaskExecutorMappingDao taskExecutorMappingDao;

    @Mock
    private SessionBindingRegistry sessionBindingRegistry;

    @InjectMocks
    private TaskInterfaceService taskInterfaceService;

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    /**
     * 测试handleTaskStopRequest：发送任务停止请求
     */
    @Test
    public void testHandleTaskStopRequest_Success() {
        // Given
        Integer taskId = 123;
        String executorName = "executor-001";
        String sessionId = "session-001";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest request = 
            new com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest();
        request.setTaskId(taskId);
        request.setExecutorName(executorName);
        request.setScriptName("test-script");
        request.setVersion("1.0");
        request.setSerialNoList(java.util.Arrays.asList("UE001"));
        taskInterfaceService.dispatchTaskToAgent(request);

        // When
        taskInterfaceService.handleTaskStopRequest(taskId);

        // Then
        verify(wssMessageSender, atLeast(2)).sendJsonMessage(eq(sessionId), any());
    }

    /**
     * 测试handleTaskStopRequest：任务不存在
     */
    @Test
    public void testHandleTaskStopRequest_TaskNotFound() {
        // Given
        Integer taskId = 999;

        // When
        taskInterfaceService.handleTaskStopRequest(taskId);

        // Then
        verify(wssMessageSender, never()).sendJsonMessage(anyString(), any());
    }

    /**
     * 测试handleTaskStartResponse：任务启动成功
     */
    @Test
    public void testHandleTaskStartResponse_Success() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-002");

        TaskStartResponseDto responseDto = new TaskStartResponseDto();
        responseDto.setTaskId(100);
        responseDto.setResult("SUCCESS");
        responseDto.setSubResult(new java.util.ArrayList<>());

        // When
        taskInterfaceService.handleTaskStartResponse(responseDto, session);

        // Then
        verify(taskOrchestratorService).sendResultEvent(eq(100L), eq(true), any());
    }

    /**
     * 测试handleTaskStartResponse：任务启动失败
     */
    @Test
    public void testHandleTaskStartResponse_Failure() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-003");

        TaskStartResponseDto responseDto = new TaskStartResponseDto();
        responseDto.setTaskId(101);
        responseDto.setResult("FAILED");
        responseDto.setSubResult(new java.util.ArrayList<>());

        // When
        taskInterfaceService.handleTaskStartResponse(responseDto, session);

        // Then
        verify(taskOrchestratorService).sendResultEvent(eq(101L), eq(false), any());
    }

    /**
     * 测试handleTaskStopResponse：任务停止成功
     */
    @Test
    public void testHandleTaskStopResponse_Success() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-004");

        TaskStopResponseDto responseDto = new TaskStopResponseDto();
        responseDto.setTaskId(102);
        responseDto.setState(0);

        // When
        taskInterfaceService.handleTaskStopResponse(responseDto, session);

        // Then
        verify(taskOrchestratorService).stopTask(eq(102L));
    }

    /**
     * 测试handleTaskStopResponse：任务停止失败
     */
    @Test
    public void testHandleTaskStopResponse_Failure() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-005");

        TaskStopResponseDto responseDto = new TaskStopResponseDto();
        responseDto.setTaskId(103);
        responseDto.setState(1);

        // When
        taskInterfaceService.handleTaskStopResponse(responseDto, session);

        // Then
        verify(taskOrchestratorService).stopTask(eq(103L));
    }

    /**
     * 测试dispatchTaskToAgent：成功下发任务
     */
    @Test
    public void testDispatchTaskToAgent_Success() {
        // Given
        String executorName = "executor-002";
        String sessionId = "session-006";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest request = 
            new com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest();
        request.setTaskId(104);
        request.setExecutorName(executorName);
        request.setScriptName("test-script");
        request.setVersion("1.0");
        request.setSerialNoList(java.util.Arrays.asList("UE001", "UE002"));

        // When
        taskInterfaceService.dispatchTaskToAgent(request);

        // Then
        verify(wssMessageSender).sendJsonMessage(eq(sessionId), any());
    }

    /**
     * 测试dispatchTaskToAgent：会话不存在
     */
    @Test(expected = RuntimeException.class)
    public void testDispatchTaskToAgent_SessionNotFound() {
        // Given
        String executorName = "executor-003";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(null);

        com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest request = 
            new com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest();
        request.setTaskId(105);
        request.setExecutorName(executorName);
        request.setScriptName("test-script");
        request.setVersion("1.0");

        // When & Then - 期望抛出 RuntimeException（因为执行机未连接）
        taskInterfaceService.dispatchTaskToAgent(request);
    }

    /**
     * 测试dispatchTaskToAgent：空UE列表
     */
    @Test
    public void testDispatchTaskToAgent_EmptyUeList() {
        // Given
        String executorName = "executor-004";
        String sessionId = "session-007";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest request = 
            new com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest();
        request.setTaskId(106);
        request.setExecutorName(executorName);
        request.setScriptName("test-script");
        request.setVersion("1.0");
        request.setSerialNoList(java.util.Collections.emptyList());

        // When
        taskInterfaceService.dispatchTaskToAgent(request);

        // Then
        verify(wssMessageSender).sendJsonMessage(eq(sessionId), any());
    }
}
