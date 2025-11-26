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
 * WebSocketSessionRegistry单元测试
 * 测试会话注册表基本功能
 *
 * @author g00940940
 * @since 2025-11-16
 */
public class WebSocketSessionRegistryTest {
    private WebSocketSessionRegistry registry;

    @Before
    public void setUp() {
        registry = new WebSocketSessionRegistry();
    }

    /**
     * 测试添加并获取会话
     */
    @Test
    public void testAddAndGetSession_Success() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-001");

        registry.addSession(session);
        Session retrieved = registry.getSession("session-001");

        assertSame(session, retrieved);

        registry.addSession(null);
        assertNull(registry.getSession("non-existent"));
    }

    /**
     * 测试移除会话
     */
    @Test
    public void testRemoveSession_Success() {
        Session session = mock(Session.class);
        when(session.getId()).thenReturn("session-001");

        registry.addSession(session);
        assertNotNull(registry.getSession("session-001"));

        registry.removeSession("session-001");
        assertNull(registry.getSession("session-001"));

        registry.removeSession(null);
        registry.removeSession("non-existent");
    }

    /**
     * 测试发送Text和Binary消息
     */
    @Test
    public void testSendTextAndBinary_Success() throws IOException {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basic = mock(RemoteEndpoint.Basic.class);
        when(session.getId()).thenReturn("session-001");
        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(basic);

        registry.addSession(session);

        String textMessage = "{\"type\":\"test\"}";
        registry.sendText("session-001", textMessage);
        verify(basic).sendText(textMessage);

        ByteBuffer buffer = ByteBuffer.wrap(new byte[100]);
        registry.sendBinary("session-001", buffer);
        verify(basic).sendBinary(buffer);

        registry.sendText("non-existent", "test");
        registry.sendBinary("non-existent", buffer);
    }
}
