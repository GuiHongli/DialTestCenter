package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorMgmtService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void testOnMessage_EmptyPayload_NoDispatch() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        endpoint.onMessage("   ", session);
        verify(dispatcher, never()).dispatch(Mockito.anyString(), Mockito.any(), Mockito.eq(session));
    }

    @Test
    public void testOnMessage_DispatchesMessage() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        String json = "{\"message_type\":\"heartbeat_status\",\"data\":{\"ue_list\":[]}}";
        endpoint.onMessage(json, session);
        verify(dispatcher).dispatch(eq("heartbeat_status"), Mockito.any(), eq(session));
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
    public void testSendMessage_DelegatesToRegistry() throws Exception {
        WssMessage msg = new WssMessage("update_executor_info", new ObjectMapper().createObjectNode());
        endpoint.sendMessage("s1", msg);
        verify(registry).sendMessage("s1", msg);
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
