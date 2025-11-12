/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import com.huawei.cloududn.dialingtestapp.service.executormanagement.ExecutorMgmtService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * V3版本ExecutorWebsocketEndpoint测试
 * 测试TLV二进制消息接收和发送功能
 *
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class ExecutorWebsocketEndpointTest {

    private ExecutorWebsocketEndpoint endpoint;
    private WebSocketSessionRegistry registry;
    private WssMessageDispatcher dispatcher;
    private ExecutorMgmtService execService;

    @Before
    public void setUp() {
        endpoint = new ExecutorWebsocketEndpoint();
        registry = Mockito.mock(WebSocketSessionRegistry.class);
        dispatcher = Mockito.mock(WssMessageDispatcher.class);
        execService = Mockito.mock(ExecutorMgmtService.class);
        set(endpoint, "sessionRegistry", registry);
        set(endpoint, "dispatcher", dispatcher);
        set(endpoint, "executorMgmtService", execService);
    }

    @Test
    public void testOnOpen_AddsSession() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        endpoint.onOpen(session);
        verify(registry).addSession(session);
    }

    @Test
    public void testOnMessage_EmptyBuffer_NoDispatch() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        ByteBuffer emptyBuffer = ByteBuffer.allocate(0);
        endpoint.onMessage(emptyBuffer, session);
        verify(dispatcher, never()).dispatch(any(ByteBuffer.class), eq(session));
    }

    @Test
    public void testOnMessage_NullBuffer_NoDispatch() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        endpoint.onMessage(null, session);
        verify(dispatcher, never()).dispatch(any(ByteBuffer.class), eq(session));
    }

    @Test
    public void testOnMessage_ValidBuffer_DispatchesToDispatcher() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");

        // Create a valid TLV buffer (Register-Request message)
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) 0x01); // MessageType.REGISTER_REQUEST
        buffer.putInt(4);        // Body length
        buffer.putInt(42);       // Sample data
        buffer.flip();

        endpoint.onMessage(buffer, session);
        verify(dispatcher).dispatch(eq(buffer), eq(session));
    }

    @Test
    public void testOnClose_RemovesAndNotifiesDisconnect() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        CloseReason reason = Mockito.mock(CloseReason.class);
        when(reason.getReasonPhrase()).thenReturn("Normal closure");
        endpoint.onClose(session, reason);
        verify(registry).removeSession("s1");
        verify(execService).handleExecutorDisconnect("s1");
    }

    @Test
    public void testSendBinary_DelegatesToRegistry() throws IOException {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");

        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putLong(123456789L);
        buffer.flip();

        // Mock registry.getSession to return the session
        when(registry.getSession("s1")).thenReturn(session);

        endpoint.sendBinary("s1", buffer);
        verify(registry).sendBinary("s1", buffer);
    }

    @Test
    public void testGetSession_DelegatesToRegistry() {
        Session session = Mockito.mock(Session.class);
        when(registry.getSession("s2")).thenReturn(session);
        assertSame(session, endpoint.getSession("s2"));
    }

    private static void set(Object target, String field, Object value) {
        try {
            java.lang.reflect.Field f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
