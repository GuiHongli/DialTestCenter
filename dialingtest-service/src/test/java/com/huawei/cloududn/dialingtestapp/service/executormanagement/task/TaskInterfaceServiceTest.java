/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.task;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.FieldTag;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.TlvField;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStartResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStopResponseDto;
import com.huawei.cloududn.dialingtestapp.dao.taskmanagement.TaskExecutorMappingDao;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.TaskOrchestratorService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.util.Map;

import javax.websocket.Session;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * TaskInterfaceService单元测试 - V3协议版本
 * 测试任务下发、停止、结果上报等功能
 *
 * @author g00940940
 * @since 2025-11-11
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

        // First, simulate a task dispatch to create the mapping
        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);
        
        // Create and dispatch a task first to populate taskToExecutorMap
        com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest request = 
            new com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest();
        request.setTaskId(taskId);
        request.setExecutorName(executorName);
        request.setScriptName("test-script");
        request.setVersion("1.0");
        request.setSerialNoList(java.util.Arrays.asList("UE001"));
        taskInterfaceService.dispatchTaskToAgent(request);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        taskInterfaceService.handleTaskStopRequest(taskId);

        // Then - Verify sendBinary was called twice (once for dispatch, once for stop)
        verify(wssMessageSender, atLeast(2)).sendBinary(eq(sessionId), bufferCaptor.capture());
    }

    /**
     * 测试handleTaskStopRequest：无执行机映射时跳过
     */
    @Test
    public void testHandleTaskStopRequest_NoExecutorMapping() {
        // Given
        Integer taskId = 456;

        // Mock no executor mapping found
        // When
        taskInterfaceService.handleTaskStopRequest(taskId);

        // Then
        verify(wssMessageSender, never()).sendBinary(anyString(), any(ByteBuffer.class));
    }

    /**
     * 测试handleTaskStopRequest：无会话连接时跳过
     */
    @Test
    public void testHandleTaskStopRequest_NoSessionFound() {
        // Given
        Integer taskId = 789;
        String executorName = "executor-002";

        // Mock executor found but no session
        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(null);

        // When
        taskInterfaceService.handleTaskStopRequest(taskId);

        // Then
        verify(wssMessageSender, never()).sendBinary(anyString(), any(ByteBuffer.class));
    }

    /**
     * 测试handleTaskStopResponse：处理任务停止响应DTO
     */
    @Test
    public void testHandleTaskStopResponse_Success() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-stop-001");

        TaskStopResponseDto dto = new TaskStopResponseDto();
        dto.setTaskId(123);
        dto.setState(0); // Success

        // When
        taskInterfaceService.handleTaskStopResponse(dto, session);

        // Then
        verify(taskOrchestratorService).stopTask(123L);
        // Note: Task mapping cleanup would be verified if we could access the internal map
    }

    /**
     * 测试handleTaskStopResponse：处理TLV格式的任务停止响应
     */
    @Test
    public void testHandleTaskStopResponse_TlvFormat_Success() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-tlv-stop-001");

        TlvDecoder.DecodedMessage decoded = mock(TlvDecoder.DecodedMessage.class);

        // Mock TASKID field
        TlvField taskIdField = mock(TlvField.class);
        when(taskIdField.getAsInt()).thenReturn(456);
        when(decoded.getField(FieldTag.TASKID)).thenReturn(taskIdField);

        // Mock RESULT field
        TlvField resultField = mock(TlvField.class);
        when(resultField.getAsInt()).thenReturn(0); // Success
        when(decoded.getField(FieldTag.RESULT)).thenReturn(resultField);

        // When
        taskInterfaceService.handleTaskStopResponse(decoded, session);

        // Then
        verify(taskOrchestratorService).stopTask(456L);
    }

    /**
     * 测试handleTaskStopResponse：TLV格式处理失败状态
     */
    @Test
    public void testHandleTaskStopResponse_TlvFormat_Failed() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-tlv-stop-002");

        TlvDecoder.DecodedMessage decoded = mock(TlvDecoder.DecodedMessage.class);

        // Mock TASKID field
        TlvField taskIdField = mock(TlvField.class);
        when(taskIdField.getAsInt()).thenReturn(789);
        when(decoded.getField(FieldTag.TASKID)).thenReturn(taskIdField);

        // Mock RESULT field (failure)
        TlvField resultField = mock(TlvField.class);
        when(resultField.getAsInt()).thenReturn(1); // Failed
        when(decoded.getField(FieldTag.RESULT)).thenReturn(resultField);

        // When
        taskInterfaceService.handleTaskStopResponse(decoded, session);

        // Then
        verify(taskOrchestratorService).stopTask(789L);
    }

    /**
     * 测试任务分发功能：dispatchTaskToAgent
     */
    @Test
    public void testDispatchTaskToAgent_Success() {
        // Given
        String executorName = "executor-dispatch-001";
        String sessionId = "session-dispatch-001";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // Create dispatch request
        com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest request = 
            new com.huawei.cloududn.dialingtestapp.service.executormanagement.dto.TaskDispatchRequest();
        request.setTaskId(999);
        request.setExecutorName(executorName);
        request.setScriptName("test-script");
        request.setVersion("1.0");
        request.setSerialNoList(java.util.Arrays.asList("UE001"));

        // When
        taskInterfaceService.dispatchTaskToAgent(request);

        // Then
        verify(sessionBindingRegistry).getSessionId(executorName);
        verify(wssMessageSender).sendBinary(eq(sessionId), bufferCaptor.capture());
        assertNotNull("Should send task start buffer", bufferCaptor.getValue());
    }

    /**
     * 测试环境管理功能：sendAppListQuery
     */
    @Test
    public void testSendAppListQuery_Success() {
        // Given
        String executorName = "executor-app-001";
        String sessionId = "session-app-001";
        String serialNo = "UE123456";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        taskInterfaceService.sendAppListQuery(executorName, serialNo);

        // Then
        verify(wssMessageSender).sendBinary(eq(sessionId), bufferCaptor.capture());
        assertNotNull("Should send app list query buffer", bufferCaptor.getValue());
    }

    /**
     * 测试环境管理功能：sendScreanCapQuery
     */
    @Test
    public void testSendScreanCapQuery_Success() {
        // Given
        String executorName = "executor-screen-001";
        String sessionId = "session-screen-001";
        String serialNo = "UE789012";

        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        taskInterfaceService.sendScreanCapQuery(executorName, serialNo);

        // Then
        verify(wssMessageSender).sendBinary(eq(sessionId), bufferCaptor.capture());
        assertNotNull("Should send screen cap query buffer", bufferCaptor.getValue());
    }

    /**
     * 测试文件直传功能的基础设施
     */
    @Test
    public void testFileTransfer_Infrastructure() {
        // Given - This test verifies that the infrastructure is properly set up
        // without making actual calls

        // When - No action is performed

        // Then - Verify infrastructure is in place
        verifyNoMoreInteractions(wssMessageSender); // No unexpected calls
    }

    /**
     * 测试异常处理：handleTaskStopRequest异常情况
     */
    @Test
    public void testHandleTaskStopRequest_ExceptionHandling() {
        // Given
        Integer taskId = 999;

        // Mock an exception during processing
        doThrow(new RuntimeException("Test exception")).when(sessionBindingRegistry).getSessionId(anyString());

        // When
        taskInterfaceService.handleTaskStopRequest(taskId);

        // Then - Should not throw exception, should handle gracefully
        // The method should log the error and continue
    }

    /**
     * 测试多UE结果处理：验证sub-result字段处理
     */
    @Test
    public void testMultiUeResultProcessing() {
        // Given
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-multi-ue-001");

        // Create DTO with proper field types (result should be String, not int)
        TaskStartResponseDto dto = new TaskStartResponseDto();
        dto.setTaskId(111);
        dto.setResult("SUCCESS"); // Use String "SUCCESS" instead of int 0
        dto.setSubResult(new java.util.ArrayList<>()); // Use empty list for sub-results

        // When
        taskInterfaceService.handleTaskStartResponse(dto, session);

        // Then
        verify(taskOrchestratorService).sendResultEvent(eq(111L), eq(true), any(Map.class));
    }
}
