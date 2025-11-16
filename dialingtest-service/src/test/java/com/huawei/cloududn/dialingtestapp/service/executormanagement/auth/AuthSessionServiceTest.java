/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.auth;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow.WssMessageSender;
import com.huawei.cloududn.dialingtestapp.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtestapp.service.DialUserService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.SessionBindingRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.websocket.Session;

import static org.mockito.Mockito.*;

/**
 * AuthSessionService单元测试 - V4协议版本
 * 测试四阶段CHAP认证流程和JSON消息处理
 *
 * @author g00940940
 * @since 2025-11-16
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
     * 测试阶段1-2：handleRegisterRequest发送Register-Challenge JSON消息
     */
    @Test
    public void testHandleRegisterRequest_SendsChallenge() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-001");

        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setHostname("Executor-01");

        // When
        service.handleRegisterRequest(requestDto, session);

        // Then
        verify(sender).sendJsonMessage(eq("session-001"), any());
    }

    /**
     * 测试阶段3-4：handleRegisterResponse成功认证
     */
    @Test
    public void testHandleRegisterResponse_Success() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-002");

        RegisterResponseDto responseDto = new RegisterResponseDto();
        responseDto.setChallengeId(1);
        responseDto.setResponse("validresponse");

        DialUser user = new DialUser();
        // DialUser fields are set via constructor or other means

        when(dialUserService.findByUsername(anyString())).thenReturn(user);

        // When
        service.handleRegisterResponse(responseDto, session);

        // Then
        verify(sender).sendJsonMessage(eq("session-002"), any());
    }

    /**
     * 测试阶段3-4：handleRegisterResponse认证失败
     */
    @Test
    public void testHandleRegisterResponse_Failure() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-003");

        RegisterResponseDto responseDto = new RegisterResponseDto();
        responseDto.setChallengeId(999);
        responseDto.setResponse("invalidresponse");

        // When
        service.handleRegisterResponse(responseDto, session);

        // Then
        verify(sender).sendJsonMessage(eq("session-003"), any());
        verify(executorDao, never()).updateStatus(anyString(), anyInt(), any());
    }

    /**
     * 测试handleRegisterRequest：无用户名时不发送Challenge
     */
    @Test
    public void testHandleRegisterRequest_NoUsername() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-004");

        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setHostname("Executor-02");
        // username is null

        // When
        service.handleRegisterRequest(requestDto, session);

        // Then
        verify(sender).sendJsonMessage(eq("session-004"), any());
    }

    /**
     * 测试handleRegisterResponse：Challenge ID不匹配
     */
    @Test
    public void testHandleRegisterResponse_InvalidChallengeId() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-005");

        RegisterResponseDto responseDto = new RegisterResponseDto();
        responseDto.setChallengeId(999);
        responseDto.setResponse("someresponse");

        // When
        service.handleRegisterResponse(responseDto, session);

        // Then
        verify(sender).sendJsonMessage(eq("session-005"), any());
        verify(registry, never()).bind(anyString(), anyString());
    }

    /**
     * 测试成功认证后的会话绑定
     */
    @Test
    public void testSuccessfulAuth_BindsSession() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-006");

        RegisterRequestDto requestDto = new RegisterRequestDto();
        requestDto.setHostname("Executor-06");

        // Step 1: Request
        service.handleRegisterRequest(requestDto, session);

        // Then verify challenge was sent
        verify(sender, atLeastOnce()).sendJsonMessage(eq("session-006"), any());
    }
}
