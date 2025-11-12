/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.WebSocketSessionRegistry;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * V3版本WssMessageSender测试
 * 测试TLV二进制消息发送功能
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class WssMessageSenderTest {

    @Mock
    private WebSocketSessionRegistry registry;

    @InjectMocks
    private WssMessageSender sender;

    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    @Test
    public void testSendBinary_DelegatesToRegistry() throws IOException {
        // Given
        String sessionId = "s1";
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x01);
        buffer.putInt(4);
        buffer.putInt(12345);
        buffer.flip();

        // When
        sender.sendBinary(sessionId, buffer);

        // Then
        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertEquals(buffer, bufferCaptor.getValue());
    }

    @Test
    public void testSendBinary_ExceptionHandling() throws IOException {
        // Given
        String sessionId = "s1";
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(42);
        buffer.flip();

        doThrow(new IOException("Network error")).when(registry).sendBinary(anyString(), any(ByteBuffer.class));

        // When - Should not throw exception
        sender.sendBinary(sessionId, buffer);

        // Then - Exception should be caught and logged (no re-throwing)
        verify(registry).sendBinary(eq(sessionId), eq(buffer));
    }

    @Test
    public void testSendBinary_EmptyBuffer() throws IOException {
        // Given
        String sessionId = "s2";
        ByteBuffer buffer = ByteBuffer.allocate(0);

        // When
        sender.sendBinary(sessionId, buffer);

        // Then
        verify(registry).sendBinary(eq(sessionId), eq(buffer));
    }

    /**
     * 测试任务消息发送：sendTaskStart
     */
    @Test
    public void testSendTaskStart_Success() throws IOException {
        // Given
        String sessionId = "session-task-001";

        TaskStartRequestDto taskDto = new TaskStartRequestDto();
        taskDto.setTaskId(123);
        taskDto.setScriptName("test-script");
        taskDto.setVersion("1.0");

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendTaskStart(sessionId, taskDto);

        // Then
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertNotNull("Should send non-null buffer", bufferCaptor.getValue());
    }

    /**
     * 测试任务消息发送：sendTaskStop
     */
    @Test
    public void testSendTaskStop_Success() throws IOException {
        // Given
        String sessionId = "session-stop-001";

        TaskStopRequestDto stopDto = new TaskStopRequestDto();
        stopDto.setTaskId(456);

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendTaskStop(sessionId, stopDto);

        // Then
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertNotNull("Should send non-null buffer", bufferCaptor.getValue());
    }

    /**
     * 测试脚本更新消息发送：sendScriptUpdate
     */
    @Test
    public void testSendScriptUpdate_Success() throws IOException {
        // Given
        String sessionId = "session-script-001";

        ScriptUpdateNotifyDto updateDto = new ScriptUpdateNotifyDto();
        updateDto.setScriptName("test-script");
        updateDto.setVersion("2.0");
        updateDto.setScriptFile("script-content".getBytes());
        updateDto.setCrc("1234567890".getBytes());

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendScriptUpdate(sessionId, updateDto);

        // Then
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertNotNull("Should send non-null buffer", bufferCaptor.getValue());
    }

    /**
     * 测试App安装消息发送：sendAppInstallRequest
     */
    @Test
    public void testSendAppInstallRequest_Success() throws IOException {
        // Given
        String sessionId = "session-app-001";

        AppInstallRequestDto installDto = new AppInstallRequestDto();
        installDto.setSerialNo("UE123456");
        installDto.setTaskId(789);
        installDto.setAppName("test-app");
        installDto.setPackageFile("app-package-content".getBytes());
        installDto.setCrc("9876543210".getBytes());

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendAppInstallRequest(sessionId, installDto);

        // Then
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertNotNull("Should send non-null buffer", bufferCaptor.getValue());
    }

    /**
     * 测试App列表查询消息发送：sendAppListQuery
     */
    @Test
    public void testSendAppListQuery_Success() throws IOException {
        // Given
        String sessionId = "session-app-list-001";

        AppListQueryDto queryDto = new AppListQueryDto();
        queryDto.setSerialNo("UE789012");

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendAppListQuery(sessionId, queryDto);

        // Then
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertNotNull("Should send non-null buffer", bufferCaptor.getValue());
    }

    /**
     * 测试截屏查询消息发送：sendScreanCapQuery
     */
    @Test
    public void testSendScreanCapQuery_Success() throws IOException {
        // Given
        String sessionId = "session-screen-001";

        ScreencapQueryDto queryDto = new ScreencapQueryDto();
        queryDto.setSerialNo("UE345678");

        ArgumentCaptor<String> sessionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendScreanCapQuery(sessionId, queryDto);

        // Then
        verify(registry).sendBinary(sessionIdCaptor.capture(), bufferCaptor.capture());
        assertEquals(sessionId, sessionIdCaptor.getValue());
        assertNotNull("Should send non-null buffer", bufferCaptor.getValue());
    }

    /**
     * 测试异常处理：编码异常时不抛出异常
     */
    @Test
    public void testSendTaskStart_ExceptionHandling() throws IOException {
        // Given
        String sessionId = "session-error-001";

        TaskStartRequestDto taskDto = new TaskStartRequestDto();
        taskDto.setTaskId(999);

        // Mock registry to throw exception
        doThrow(new IOException("Connection failed")).when(registry).sendBinary(anyString(), any(ByteBuffer.class));

        // When - Should not throw exception
        sender.sendTaskStart(sessionId, taskDto);

        // Then - Exception should be caught and logged
        verify(registry).sendBinary(anyString(), any(ByteBuffer.class));
    }

    /**
     * 测试文件直传：验证大文件缓冲区处理
     */
    @Test
    public void testFileTransfer_BufferHandling() throws IOException {
        // Given
        String sessionId = "session-file-001";

        // Create a script update with large file content
        ScriptUpdateNotifyDto updateDto = new ScriptUpdateNotifyDto();
        updateDto.setScriptName("large-script");
        updateDto.setVersion("1.0");
        byte[] largeContent = new byte[1024 * 1024]; // 1MB file
        for (int i = 0; i < largeContent.length; i++) {
            largeContent[i] = (byte) (i % 256);
        }
        updateDto.setScriptFile(largeContent);
        updateDto.setCrc("123456789".getBytes());

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        sender.sendScriptUpdate(sessionId, updateDto);

        // Then
        verify(registry).sendBinary(eq(sessionId), bufferCaptor.capture());
        ByteBuffer sentBuffer = bufferCaptor.getValue();
        assertNotNull("Should send buffer for large file", sentBuffer);
        assertTrue("Buffer should contain data", sentBuffer.remaining() > 0);
    }
}


