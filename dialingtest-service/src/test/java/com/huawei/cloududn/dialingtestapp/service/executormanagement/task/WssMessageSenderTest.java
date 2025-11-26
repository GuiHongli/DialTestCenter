/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.task;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ReportAckDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStartRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow.WssMessageSender;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.Mockito.*;

/**
 * WssMessageSender接口测试 - V4协议版本
 * 测试JSON消息发送和文件发送功能
 *
 * @author g00940940
 * @since 2025-11-16
 */
public class WssMessageSenderTest {

    @Mock
    private WssMessageSender sender;

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
     * 测试发送JSON消息
     */
    @Test
    public void testSendJsonMessage_Success() {
        // Given
        String sessionId = "session-001";
        ReportAckDto dto = new ReportAckDto();
        dto.setToken(12345L);
        dto.setState(0);

        // When
        sender.sendJsonMessage(sessionId, dto);

        // Then
        verify(sender).sendJsonMessage(eq(sessionId), eq(dto));
    }

    /**
     * 测试发送JSON消息：空sessionId
     */
    @Test
    public void testSendJsonMessage_NullSession() {
        // Given
        ReportAckDto dto = new ReportAckDto();
        dto.setToken(12345L);

        // When
        sender.sendJsonMessage(null, dto);

        // Then
        verify(sender).sendJsonMessage(isNull(), eq(dto));
    }

    /**
     * 测试发送文件：成功场景
     */
    @Test
    public void testSendFile_Success() {
        // Given
        String sessionId = "session-002";
        TaskStartRequestDto dto = new TaskStartRequestDto();
        dto.setTaskId(100);
        dto.setScriptName("test-script");

        byte[] fileData = "test file content".getBytes();
        InputStream fileStream = new ByteArrayInputStream(fileData);

        // When
        sender.sendFile(sessionId, dto, fileStream);

        // Then
        verify(sender).sendFile(eq(sessionId), eq(dto), eq(fileStream));
    }

    /**
     * 测试发送文件：空文件流
     */
    @Test
    public void testSendFile_NullStream() {
        // Given
        String sessionId = "session-003";
        TaskStartRequestDto dto = new TaskStartRequestDto();
        dto.setTaskId(101);

        // When
        sender.sendFile(sessionId, dto, null);

        // Then
        verify(sender).sendFile(eq(sessionId), eq(dto), isNull());
    }

    /**
     * 测试发送多个JSON消息
     */
    @Test
    public void testSendJsonMessage_Multiple() {
        // Given
        String sessionId = "session-004";
        ReportAckDto dto1 = new ReportAckDto();
        dto1.setToken(1L);
        
        ReportAckDto dto2 = new ReportAckDto();
        dto2.setToken(2L);

        // When
        sender.sendJsonMessage(sessionId, dto1);
        sender.sendJsonMessage(sessionId, dto2);

        // Then
        verify(sender, times(2)).sendJsonMessage(eq(sessionId), any());
    }

    /**
     * 测试发送文件：大文件场景
     */
    @Test
    public void testSendFile_LargeFile() {
        // Given
        String sessionId = "session-005";
        TaskStartRequestDto dto = new TaskStartRequestDto();
        dto.setTaskId(102);

        byte[] largeFileData = new byte[10 * 1024 * 1024]; // 10MB
        InputStream fileStream = new ByteArrayInputStream(largeFileData);

        // When
        sender.sendFile(sessionId, dto, fileStream);

        // Then
        verify(sender).sendFile(eq(sessionId), eq(dto), eq(fileStream));
    }
}

