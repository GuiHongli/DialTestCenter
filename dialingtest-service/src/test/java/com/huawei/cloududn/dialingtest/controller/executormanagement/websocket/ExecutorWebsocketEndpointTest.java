package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorMgmtService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;


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
    public void testAfterConnectionEstablished_AddsSession() {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s1");
        endpoint.afterConnectionEstablished(session);
        verify(registry).addSession(session);
    }

    @Test
    public void testHandleTextMessage_EmptyPayload_NoDispatch() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s1");
        endpoint.handleTextMessage(session, new TextMessage("   "));
        verify(dispatcher, never()).dispatch(Mockito.anyString(), Mockito.any(), Mockito.eq(session));
    }

    @Test
    public void testHandleTextMessage_DispatchesMessage() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s1");
        String json = "{\"message_type\":\"heartbeat_status\",\"data\":{\"ue_list\":[]}}";
        endpoint.handleTextMessage(session, new TextMessage(json));
        verify(dispatcher).dispatch(eq("heartbeat_status"), Mockito.any(), eq(session));
    }

    @Test
    public void testAfterConnectionClosed_RemovesAndNotifiesDisconnect() {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("s1");
        endpoint.afterConnectionClosed(session, CloseStatus.NORMAL);
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
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
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


