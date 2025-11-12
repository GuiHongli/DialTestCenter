/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * V3版本WebSocketSessionRegistry测试
 * 测试二进制消息发送功能
 *
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class WebSocketSessionRegistryTest {

    private WebSocketSessionRegistry registry;

    @Before
    public void setUp() {
        registry = new WebSocketSessionRegistry();
    }

    @Test
    public void testAddAndGetSession_Success_ReturnsSame() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("s1");

        registry.addSession(session);

        Session fetched = registry.getSession("s1");
        assertNotNull(fetched);
        assertEquals(session, fetched);
    }

    @Test
    public void testRemoveSession_RemovesFromRegistry() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("s2");
        registry.addSession(session);

        registry.removeSession("s2");

        assertNull(registry.getSession("s2"));
    }

    @Test
    public void testRemoveSession_WithNullId_DoesNotThrow() {
        // Should not throw exception
        registry.removeSession(null);

        // Should not throw exception
        registry.removeSession("");
    }

    @Test
    public void testSendBinary_CallsSessionSendBinary() throws IOException {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getId()).thenReturn("s3");
        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(basicRemote);
        registry.addSession(session);

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(123456789L);
        buffer.flip();

        registry.sendBinary("s3", buffer);

        verify(basicRemote, times(1)).sendBinary(buffer);
    }

    @Test
    public void testSendBinary_SessionNotFound_DoesNotThrow() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(42);
        buffer.flip();

        // Should not throw exception when session not found
        registry.sendBinary("nonexistent", buffer);
    }

    @Test
    public void testSendBinary_SessionNotOpen_DoesNotThrow() throws IOException {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("s4");
        when(session.isOpen()).thenReturn(false); // Session is closed
        registry.addSession(session);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(42);
        buffer.flip();

        // Should not throw exception when session is closed
        registry.sendBinary("s4", buffer);
    }

    @Test
    public void testSendBinary_SendBinaryThrowsIOException_DoesNotPropagate() throws IOException {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getId()).thenReturn("s5");
        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(basicRemote);
        doThrow(new IOException("Network error")).when(basicRemote).sendBinary(any(ByteBuffer.class));
        registry.addSession(session);

        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(42);
        buffer.flip();

        // Should not throw exception even when sendBinary fails
        registry.sendBinary("s5", buffer);
    }

    @Test
    public void testAddSession_WithNullSession_DoesNotThrow() {
        // Should not throw exception when adding null session
        registry.addSession(null);
    }

    @Test
    public void testGetSession_NonExistentSession_ReturnsNull() {
        Session result = registry.getSession("nonexistent");
        assertNull(result);
    }

    @Test
    public void testAddSession_ReplaceExistingSession() {
        Session session1 = mock(Session.class);
        Session session2 = mock(Session.class);
        when(session1.getId()).thenReturn("s6");
        when(session2.getId()).thenReturn("s6");

        registry.addSession(session1);
        registry.addSession(session2); // Should replace session1

        Session fetched = registry.getSession("s6");
        assertEquals(session2, fetched);
    }
}
