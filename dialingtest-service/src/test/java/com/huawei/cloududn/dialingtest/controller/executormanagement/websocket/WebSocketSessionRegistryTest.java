/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

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
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s1");

        registry.addSession(session);

        WebSocketSession fetched = registry.getSession("s1");
        assertNotNull(fetched);
        assertEquals(session, fetched);
    }

    @Test
    public void testRemoveSession_RemovesFromRegistry() {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s2");
        registry.addSession(session);

        registry.removeSession("s2");

        assertNull(registry.getSession("s2"));
    }

    @Test
    public void testSendMessage_CallsSessionSendMessage() throws Exception {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s3");
        registry.addSession(session);

        WssMessage msg = new WssMessage("test_type", "payload");
        registry.sendMessage("s3", msg);

        ArgumentCaptor<TextMessage> captor = ArgumentCaptor.forClass(TextMessage.class);
        verify(session, times(1)).sendMessage(captor.capture());
        assertTrue(captor.getValue().getPayload().contains("test_type"));
    }
}


