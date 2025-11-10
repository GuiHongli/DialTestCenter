package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.WebSocketSessionRegistry;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
    public void testSendQueryUeScreencap_BuildsCorrectMessage() throws Exception {
        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);
        sender.sendQueryUeScreencap("s1", "SN001");
        verify(registry).sendMessage(eq("s1"), cap.capture());
        WssMessage msg = cap.getValue();
        assertEquals("query_ue_screencap", msg.getMessage_type());
        assertTrue(msg.getData() instanceof Map);
        assertEquals("SN001", ((Map<?, ?>) msg.getData()).get("ue_serial"));
    }

    @Test
    public void testSendScriptUpdateNotify_BuildsCorrectMessage() throws Exception {
        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);
        sender.sendScriptUpdateNotify("s2", "openlive_scripts", "1.1", "s3://bucket/scripts.zip", "md5");
        verify(registry).sendMessage(eq("s2"), cap.capture());
        WssMessage msg = cap.getValue();
        assertEquals("script_update_notify", msg.getMessage_type());
        Map<?, ?> data = (Map<?, ?>) msg.getData();
        assertEquals("openlive_scripts", data.get("package_name"));
        assertEquals("1.1", data.get("version"));
        assertEquals("s3://bucket/scripts.zip", data.get("package_url"));
        assertEquals("md5", data.get("checksum"));
    }

    @Test
    public void testSendAppInstall_PassesPayload() throws Exception {
        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);
        Map<String, Object> payload = new HashMap<>();
        payload.put("ue_serial", "SN001");
        payload.put("install_type", "url");
        sender.sendAppInstall("s3", payload);
        verify(registry).sendMessage(eq("s3"), cap.capture());
        WssMessage msg = cap.getValue();
        assertEquals("app_install", msg.getMessage_type());
        assertSame(payload, msg.getData());
    }

    @Test
    public void testSendTaskCancel_BuildsCorrectMessage() throws Exception {
        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);
        sender.sendTaskCancel("s4", "T_1");
        verify(registry).sendMessage(eq("s4"), cap.capture());
        WssMessage msg = cap.getValue();
        assertEquals("task_cancel", msg.getMessage_type());
        assertEquals("T_1", ((Map<?, ?>) msg.getData()).get("task_id"));
    }
}


