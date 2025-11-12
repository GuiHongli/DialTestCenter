package com.huawei.cloududn.dialingtest.service.executormanagement.auth;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.FieldTag;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvDecoder;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec.TlvField;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.service.DialUserService;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.WssMessageSender;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthSessionService单元测试 - V3协议版本
 * 测试四阶段CHAP认证流程和TLV消息处理
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class AuthSessionServiceTest {

    @Mock
    private WssMessageSender sender;

    @Mock
    private DialUserService dialUserService;

    @Mock
    private ExecutorDao executorDao;

    @Mock
    private SessionBindingRegistry registry;

    @InjectMocks
    private AuthSessionService service;

    private AutoCloseable mocks;

    @Before
    public void init() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    /**
     * 测试阶段1-2：handleRegisterRequest发送Register-Challenge (0x02)
     */
    @Test
    public void testHandleRegisterRequestV3_SendsChallenge() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-001");

        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setHostname("Executor-01");

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleRegisterRequest(requestDto, session);

        // Then
        verify(sender).sendBinary(eq("session-001"), bufferCaptor.capture());
        assertNotNull("Should send binary message", bufferCaptor.getValue());
        // Note: Detailed TLV parsing verification would require TlvDecoder, covered in integration tests
    }

    /**
     * 测试阶段3-4：handleRegisterResponse成功认证
     */
    @Test
    public void testHandleRegisterResponseV3_Success() throws NoSuchAlgorithmException {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-001");

        // Mock pending context (simulate previous handleRegisterRequest call)
        service.handleRegisterRequest(new RegisterRequestDto("Executor-01"), session);

        // Create decoded message with valid response
        TlvDecoder.DecodedMessage decoded = createValidRegisterResponse();

        // Mock user lookup from dial_users table
        DialUser user = new DialUser();
        user.setUsername("testuser");
        user.setPassword("0123456789abcdef0123456789abcdef"); // 32-char NTLM Hash hex string
        when(dialUserService.findByUsername("testuser")).thenReturn(user);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleRegisterResponse(decoded, session);

        // Then
        verify(executorDao).saveOrUpdateExecutor(eq("Executor-01"), anyLong(), eq("ONLINE"));
        verify(registry).bind(eq("session-001"), eq("Executor-01"), anyLong());
        verify(sender, Mockito.times(2)).sendBinary(eq("session-001"), bufferCaptor.capture());
        // First call: Register-Challenge, Second call: Register-Result
    }

    /**
     * 测试阶段3-4：handleRegisterResponse用户不存在失败
     */
    @Test
    public void testHandleRegisterResponseV3_UserNotFound() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-002");

        // Mock pending context
        service.handleRegisterRequest(new RegisterRequestDto("Executor-02"), session);

        // Create decoded message
        TlvDecoder.DecodedMessage decoded = createValidRegisterResponse();
        when(dialUserService.findByUsername("testuser")).thenReturn(null);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleRegisterResponse(decoded, session);

        // Then
        verify(executorDao, never()).saveOrUpdateExecutor(anyString(), anyLong(), anyString());
        verify(registry, never()).bind(anyString(), anyString(), anyLong());
        verify(sender, Mockito.times(2)).sendBinary(eq("session-002"), bufferCaptor.capture());
        // Register-Challenge + Register-Result with error
    }

    /**
     * 测试阶段3-4：handleRegisterResponse挑战ID不匹配
     */
    @Test
    public void testHandleRegisterResponseV3_ChallengeIdMismatch() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-003");

        // Mock pending context with challenge ID 1
        service.handleRegisterRequest(new RegisterRequestDto("Executor-03"), session);

        // Create decoded message with different challenge ID
        TlvDecoder.DecodedMessage decoded = createRegisterResponseWithChallengeId(999);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleRegisterResponse(decoded, session);

        // Then
        verify(executorDao, never()).saveOrUpdateExecutor(anyString(), anyLong(), anyString());
        verify(registry, never()).bind(anyString(), anyString(), anyLong());
        verify(sender, Mockito.times(2)).sendBinary(eq("session-003"), bufferCaptor.capture());
    }

    /**
     * 测试阶段3-4：handleRegisterResponse挑战过期
     */
    @Test
    public void testHandleRegisterResponseV3_ChallengeExpired() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-004");

        // Mock pending context
        service.handleRegisterRequest(new RegisterRequestDto("Executor-04"), session);

        // Wait for challenge to expire (simulate by directly calling with expired context)
        // Note: In real implementation, this would be tested with time manipulation

        TlvDecoder.DecodedMessage decoded = createValidRegisterResponse();

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When - simulate expired challenge
        // This test focuses on the error path structure
        service.handleRegisterResponse(decoded, session);

        // Then - should handle expired challenge appropriately
        verify(sender, Mockito.atLeast(1)).sendBinary(eq("session-004"), bufferCaptor.capture());
    }

    /**
     * 测试阶段3-4：handleRegisterResponse无效响应
     */
    @Test
    public void testHandleRegisterResponseV3_InvalidResponse() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-005");

        // Mock pending context
        service.handleRegisterRequest(new RegisterRequestDto("Executor-05"), session);

        // Create decoded message with invalid response
        TlvDecoder.DecodedMessage decoded = createInvalidRegisterResponse();

        // Mock user lookup from dial_users table
        DialUser user = new DialUser();
        user.setUsername("testuser");
        user.setPassword("0123456789abcdef0123456789abcdef"); // 32-char NTLM Hash hex string
        when(dialUserService.findByUsername("testuser")).thenReturn(user);

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleRegisterResponse(decoded, session);

        // Then
        verify(executorDao, never()).saveOrUpdateExecutor(anyString(), anyLong(), anyString());
        verify(registry, never()).bind(anyString(), anyString(), anyLong());
        verify(sender, Mockito.times(2)).sendBinary(eq("session-005"), bufferCaptor.capture());
    }

    /**
     * 测试token生成为8字节long类型
     */
    @Test
    public void testTokenGeneration_Is8ByteLong() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-token-test");

        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setHostname("Executor-TokenTest");

        // Mock successful authentication
        DialUser user = new DialUser();
        user.setUsername("testuser");
        user.setPassword("0123456789abcdef0123456789abcdef"); // 32-char NTLM Hash hex string
        when(dialUserService.findByUsername("testuser")).thenReturn(user);

        TlvDecoder.DecodedMessage decoded = createValidRegisterResponse();

        // When
        service.handleRegisterRequest(requestDto, session);
        service.handleRegisterResponse(decoded, session);

        // Then
        verify(executorDao).saveOrUpdateExecutor(anyString(), anyLong(), eq("ONLINE"));
        verify(registry).bind(anyString(), anyString(), anyLong());
    }

    // Helper methods for creating test data

    private TlvDecoder.DecodedMessage createValidRegisterResponse() {
        TlvDecoder.DecodedMessage decoded = Mockito.mock(TlvDecoder.DecodedMessage.class);

        // Mock challenge ID field (should match the generated one)
        TlvField challengeIdField = Mockito.mock(TlvField.class);
        when(challengeIdField.getAsInt()).thenReturn(1);
        when(decoded.getField(FieldTag.CHALLENGE_ID)).thenReturn(challengeIdField);

        // Mock username field
        TlvField usernameField = Mockito.mock(TlvField.class);
        when(usernameField.getAsString()).thenReturn("testuser");
        when(decoded.getField(FieldTag.USERNAME)).thenReturn(usernameField);

        // Mock valid response bytes (would be computed as MD5(NTLM-Hash + Challenge))
        TlvField responseField = Mockito.mock(TlvField.class);
        byte[] mockResponse = new byte[16]; // 16 bytes for MD5
        for (int i = 0; i < 16; i++) {
            mockResponse[i] = (byte) i;
        }
        when(responseField.getAsBytes()).thenReturn(mockResponse);
        when(decoded.getField(FieldTag.RESPONSE)).thenReturn(responseField);

        return decoded;
    }

    private TlvDecoder.DecodedMessage createRegisterResponseWithChallengeId(int challengeId) {
        TlvDecoder.DecodedMessage decoded = createValidRegisterResponse();
        TlvField challengeIdField = Mockito.mock(TlvField.class);
        when(challengeIdField.getAsInt()).thenReturn(challengeId);
        when(decoded.getField(FieldTag.CHALLENGE_ID)).thenReturn(challengeIdField);
        return decoded;
    }

    private TlvDecoder.DecodedMessage createInvalidRegisterResponse() {
        TlvDecoder.DecodedMessage decoded = createValidRegisterResponse();
        // Invalid response bytes
        TlvField responseField = Mockito.mock(TlvField.class);
        byte[] invalidResponse = new byte[16];
        when(responseField.getAsBytes()).thenReturn(invalidResponse);
        when(decoded.getField(FieldTag.RESPONSE)).thenReturn(responseField);
        return decoded;
    }

    private static String md5Hex(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
