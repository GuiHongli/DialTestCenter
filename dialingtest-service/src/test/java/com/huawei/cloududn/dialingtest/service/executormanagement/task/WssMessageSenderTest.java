/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.WebSocketSessionRegistry;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvEncoder;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.MessageType;

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
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class WssMessageSenderTest {

    @Mock
    private WebSocketSessionRegistry registry;

    @InjectMocks
    private WssMessageSender sender;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
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
}


