package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.service.executormanagement.ExecutorMgmtService;
import com.huawei.cloududn.dialingtest.service.executormanagement.auth.AuthSessionService;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.TaskInterfaceService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.socket.WebSocketSession;

public class WssMessageDispatcherTest {

    private WssMessageDispatcher dispatcher;
    private AuthSessionService auth;
    private ExecutorMgmtService exec;
    private TaskInterfaceService task;

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        dispatcher = new WssMessageDispatcher();
        auth = Mockito.mock(AuthSessionService.class);
        exec = Mockito.mock(ExecutorMgmtService.class);
        task = Mockito.mock(TaskInterfaceService.class);
        set(dispatcher, "authSessionService", auth);
        set(dispatcher, "executorMgmtService", exec);
        set(dispatcher, "taskInterfaceService", task);
    }

    @Test
    public void testDispatch_Deregister_Routed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.getId()).thenReturn("s1");
        dispatcher.dispatch("deregister", mapper.readTree("{\"name\":\"Executor_PC_001\"}"), session);
        Mockito.verify(exec).handleDeregister(Mockito.any(), Mockito.eq(session));
    }

    @Test
    public void testDispatch_ExecutorInfoResponse_Routed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.getId()).thenReturn("s2");
        dispatcher.dispatch("executor_info_response", mapper.readTree("{\"executor_name\":\"Executor_PC_001\"}"), session);
        Mockito.verify(exec).handleExecutorInfoResponse(Mockito.any(), Mockito.eq(session));
    }

    @Test
    public void testDispatch_ScriptUpdateAck_Routed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.getId()).thenReturn("s3");
        dispatcher.dispatch("script_update_ack", mapper.readTree("{\"package_name\":\"test_script\",\"version\":\"1.0\",\"status\":\"success\"}"), session);
        Mockito.verify(task).handleScriptUpdateAck(Mockito.any(), Mockito.eq(session));
    }

    @Test
    public void testDispatch_UeScreencapResponse_Routed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.getId()).thenReturn("s4");
        dispatcher.dispatch("ue_screencap_response", mapper.readTree("{\"ue_serial\":\"SN001\"}"), session);
        Mockito.verify(task).handleUeScreencapResponse(Mockito.any(), Mockito.eq(session));
    }

    @Test
    public void testDispatch_AppInstallResult_Routed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        Mockito.when(session.getId()).thenReturn("s5");
        dispatcher.dispatch("app_install_result", mapper.readTree("{\"ue_serial\":\"SN001\",\"status\":\"success\"}"), session);
        Mockito.verify(task).handleAppInstallResult(Mockito.any(), Mockito.eq(session));
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


