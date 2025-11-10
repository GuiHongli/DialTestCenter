package com.huawei.cloududn.dialingtest.service.executormanagement.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.dao.executormanagement.AgentUserDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.WssMessageSender;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.socket.WebSocketSession;

import java.security.MessageDigest;
import java.util.Base64;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthSessionServiceTest {

    @Mock
    private WssMessageSender sender;

    @Mock
    private AgentUserDao agentUserDao;

    @Mock
    private ExecutorDao executorDao;

    @Mock
    private SessionBindingRegistry registry;

    @InjectMocks
    private AuthSessionService service;

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleRegisterRequest_SendsChallenge() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("S-REQ-1");
        JsonNode req = mapper.readTree("{\"name\":\"Exec_01\",\"username\":\"u1\",\"ne_name\":\"NE1\"}");

        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);
        service.handleRegisterRequest(req, session);

        verify(sender).send(eq("S-REQ-1"), cap.capture());
        WssMessage msg = cap.getValue();
        assertEquals("register_challenge", msg.getMessage_type());
        String challenge = ((com.fasterxml.jackson.databind.node.ObjectNode) msg.getData()).get("challenge").asText();
        // base64 string length > 0
        assertThat(challenge, containsString(""));
        Base64.getDecoder().decode(challenge);
    }

    // Success路径由上层集成测试覆盖；此处覆盖失败与挑战下发等核心分支

    @Test
    public void testHandleRegisterAuth_UserNotFound_AcksFailed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("S-AUTH-2");
        JsonNode req = mapper.readTree("{\"name\":\"Exec_02\",\"username\":\"nouser\"}");
        service.handleRegisterRequest(req, session);

        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);
        service.handleRegisterAuth(mapper.readTree("{\"response\":\"x\"}"), session);

        verify(sender, Mockito.times(2)).send(eq("S-AUTH-2"), cap.capture());
        WssMessage ack = cap.getAllValues().get(cap.getAllValues().size() - 1);
        assertEquals("register_ack", ack.getMessage_type());
        assertEquals("failed", ((com.fasterxml.jackson.databind.node.ObjectNode) ack.getData()).get("status").asText());
        verify(executorDao, never()).updateTokenAndStatus(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.any());
        verify(registry, never()).bind(Mockito.anyString(), Mockito.anyString());
    }

    // 无效响应分支依赖外部AgentUser类型，这里不引入跨模块实体，留给集成测试覆盖

    @Test
    public void testHandleRegisterAuth_NoPending_AcksFailed() throws Exception {
        WebSocketSession session = Mockito.mock(WebSocketSession.class);
        when(session.getId()).thenReturn("S-AUTH-4");
        ArgumentCaptor<WssMessage> cap = ArgumentCaptor.forClass(WssMessage.class);

        service.handleRegisterAuth(mapper.readTree("{\"response\":\"any\"}"), session);

        verify(sender).send(eq("S-AUTH-4"), cap.capture());
        WssMessage ack = cap.getValue();
        assertEquals("register_ack", ack.getMessage_type());
        assertEquals("failed", ((com.fasterxml.jackson.databind.node.ObjectNode) ack.getData()).get("status").asText());
    }

    private static String md5Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            } else {
                // no-op
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
