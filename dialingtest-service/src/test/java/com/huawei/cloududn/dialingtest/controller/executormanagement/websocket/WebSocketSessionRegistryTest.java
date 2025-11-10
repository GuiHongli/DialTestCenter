/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WebSocketSessionRegistry.
 *
 * @author g00940940
 * @since 2025-11-06
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
    public void testSendMessage_CallsSessionSendText() throws Exception {
        Session session = mock(Session.class);
        RemoteEndpoint.Basic basicRemote = mock(RemoteEndpoint.Basic.class);
        when(session.getId()).thenReturn("s3");
        when(session.isOpen()).thenReturn(true);
        when(session.getBasicRemote()).thenReturn(basicRemote);
        registry.addSession(session);

        WssMessage msg = new WssMessage("test_type", "payload");
        registry.sendMessage("s3", msg);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(basicRemote, times(1)).sendText(captor.capture());
        assertTrue(captor.getValue().contains("test_type"));
    }
}
