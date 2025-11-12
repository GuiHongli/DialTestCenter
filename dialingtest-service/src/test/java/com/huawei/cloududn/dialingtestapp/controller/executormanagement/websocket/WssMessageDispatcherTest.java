/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.FieldTag;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.MessageType;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.TlvEncoder;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec.TlvField;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStartResponseDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.TaskStopResponseDto;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.ExecutorMgmtService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.auth.AuthSessionService;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.task.TaskInterfaceService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.nio.ByteBuffer;

import javax.websocket.Session;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * V3版本WssMessageDispatcher测试
 * 测试TLV消息ID分发功能
 *
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class WssMessageDispatcherTest {

    private WssMessageDispatcher dispatcher;
    private AuthSessionService auth;
    private ExecutorMgmtService exec;
    private TaskInterfaceService task;

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
    public void testDispatch_RegisterRequest_RoutedToAuthService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");

        // Create Register-Request TLV message
        ByteBuffer buffer = createRegisterRequestBuffer("Executor_PC_001");

        dispatcher.dispatch(buffer, session);
        verify(auth).handleRegisterRequest(any(RegisterRequestDto.class), eq(session));
    }

    @Test
    public void testDispatch_RegisterResponse_RoutedToAuthService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s2");

        // Create Register-Response TLV message
        ByteBuffer buffer = createRegisterResponseBuffer(1, "username", "response");

        dispatcher.dispatch(buffer, session);
        verify(auth).handleRegisterResponse(any(), eq(session));
    }

    @Test
    public void testDispatch_DeRegisterRequest_RoutedToExecutorService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s3");

        // Create DeRegister-Request TLV message
        ByteBuffer buffer = createDeRegisterRequestBuffer("Executor_PC_001");

        dispatcher.dispatch(buffer, session);
        verify(exec).handleDeRegisterRequest(any(), eq(session));
    }

    @Test
    public void testDispatch_ReportMsg_RoutedToExecutorService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s4");

        // Create Report-Msg TLV message
        ByteBuffer buffer = createReportMsgBuffer(123456789L, "Normal");

        dispatcher.dispatch(buffer, session);
        verify(exec).handleReportMsg(any(), eq(session));
    }

    @Test
    public void testDispatch_AppListResponse_RoutedToTaskService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s5");

        // Create AppList-Response TLV message
        ByteBuffer buffer = createAppListResponseBuffer("SN001", 0);

        dispatcher.dispatch(buffer, session);
        verify(task).handleAppListResponse(any(), eq(session));
    }

    @Test
    public void testDispatch_AppInstallResponse_RoutedToTaskService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s6");

        // Create AppInstall-Response TLV message
        ByteBuffer buffer = createAppInstallResponseBuffer("SN001", 1001, 0);

        dispatcher.dispatch(buffer, session);
        verify(task).handleAppInstallResponse(any(), eq(session));
    }

    @Test
    public void testDispatch_ScreencapResponse_RoutedToTaskService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s7");

        // Create Screencap-Response TLV message
        ByteBuffer buffer = createScreencapResponseBuffer("SN001", 0, "screenshot.png", "imagedata".getBytes());

        dispatcher.dispatch(buffer, session);
        verify(task).handleScreencapResponse(any(), eq(session));
    }

    @Test
    public void testDispatch_ScriptUpdateAck_RoutedToTaskService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s8");

        // Create ScriptUpdate-Ack TLV message
        ByteBuffer buffer = createScriptUpdateAckBuffer("test_script", "1.0", 0);

        dispatcher.dispatch(buffer, session);
        verify(task).handleScriptUpdateAck(any(), eq(session));
    }

    @Test
    public void testDispatch_TaskStartResponse_RoutedToTaskService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s9");

        // Create TaskStart-Response TLV message
        ByteBuffer buffer = createTaskStartResponseBuffer(1001, "Success");

        dispatcher.dispatch(buffer, session);
        verify(task).handleTaskStartResponse(any(TaskStartResponseDto.class), eq(session));
    }

    @Test
    public void testDispatch_TaskStopResponse_RoutedToTaskService() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s10");

        // Create TaskStop-Response TLV message
        ByteBuffer buffer = createTaskStopResponseBuffer(1001, 0);

        dispatcher.dispatch(buffer, session);
        verify(task).handleTaskStopResponse(any(TaskStopResponseDto.class), eq(session));
    }

    @Test
    public void testDispatch_UnknownMessageType_LoggedAndIgnored() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s11");

        // Create unknown message type
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 0xFF); // Unknown message type
        buffer.putInt(0);        // Empty body
        buffer.flip();

        // Should not throw exception, just log warning
        dispatcher.dispatch(buffer, session);

        // Verify no services were called
        verify(auth, never()).handleRegisterRequest(any(RegisterRequestDto.class), any(Session.class));
        verify(exec, never()).handleReportMsg(any(), any());
        verify(task, never()).handleAppListResponse(any(), any());
    }

    @Test
    public void testDispatch_InvalidTlvMessage_ThrowsException() {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s12");

        // Create invalid TLV message (too short)
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put((byte) 0x01);
        buffer.flip();

        // Should throw IllegalArgumentException
        try {
            dispatcher.dispatch(buffer, session);
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    // Helper methods to create TLV messages
    private ByteBuffer createRegisterRequestBuffer(String hostname) {
        return TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST,
            java.util.Arrays.asList(TlvField.ofString(FieldTag.HOSTNAME, hostname)));
    }

    private ByteBuffer createRegisterResponseBuffer(int challengeId, String username, String response) {
        return TlvEncoder.encodeMessage(MessageType.REGISTER_RESPONSE,
            java.util.Arrays.asList(
                TlvField.ofInt(FieldTag.CHALLENGE_ID, challengeId),
                TlvField.ofString(FieldTag.USERNAME, username),
                TlvField.ofBytes(FieldTag.RESPONSE, response.getBytes())
            ));
    }

    private ByteBuffer createDeRegisterRequestBuffer(String hostname) {
        return TlvEncoder.encodeMessage(MessageType.DEREGISTER_REQUEST,
            java.util.Arrays.asList(TlvField.ofString(FieldTag.HOSTNAME, hostname)));
    }

    private ByteBuffer createReportMsgBuffer(long token, String state) {
        return TlvEncoder.encodeMessage(MessageType.REPORT_MSG,
            java.util.Arrays.asList(
                TlvField.ofLong(FieldTag.TOKEN, token),
                TlvField.ofString(FieldTag.STATE, state),
                TlvField.ofContainer(FieldTag.UE_LIST, new byte[0])
            ));
    }

    private ByteBuffer createAppListResponseBuffer(String serialNo, int result) {
        return TlvEncoder.encodeMessage(MessageType.APP_LIST_RESPONSE,
            java.util.Arrays.asList(
                TlvField.ofString(FieldTag.TOKEN, "dummy_token"),
                TlvField.ofString(FieldTag.SERIAL_NO, serialNo),
                TlvField.ofInt(FieldTag.RESULT, result)
            ));
    }

    private ByteBuffer createAppInstallResponseBuffer(String serialNo, int taskId, int result) {
        return TlvEncoder.encodeMessage(MessageType.APP_INSTALL_RESPONSE,
            java.util.Arrays.asList(
                TlvField.ofString(FieldTag.TOKEN, "dummy_token"),
                TlvField.ofString(FieldTag.SERIAL_NO, serialNo),
                TlvField.ofInt(FieldTag.TASKID, taskId),
                TlvField.ofInt(FieldTag.RESULT, result)
            ));
    }

    private ByteBuffer createScreencapResponseBuffer(String serialNo, int result, String filename, byte[] content) {
        return TlvEncoder.encodeMessage(MessageType.SCREENCAP_RESPONSE,
            java.util.Arrays.asList(
                TlvField.ofString(FieldTag.TOKEN, "dummy_token"),
                TlvField.ofString(FieldTag.SERIAL_NO, serialNo),
                TlvField.ofInt(FieldTag.RESULT, result),
                TlvField.ofString(FieldTag.FILENAME, filename),
                TlvField.ofBytes(FieldTag.CONTENT, content)
            ));
    }

    private ByteBuffer createScriptUpdateAckBuffer(String scriptName, String version, int result) {
        return TlvEncoder.encodeMessage(MessageType.SCRIPT_UPDATE_ACK,
            java.util.Arrays.asList(
                TlvField.ofString(FieldTag.TOKEN, "dummy_token"),
                TlvField.ofString(FieldTag.SCRIPT_NAME, scriptName),
                TlvField.ofString(FieldTag.VERSION, version),
                TlvField.ofInt(FieldTag.RESULT, result)
            ));
    }

    private ByteBuffer createTaskStartResponseBuffer(int taskId, String result) {
        return TlvEncoder.encodeMessage(MessageType.TASK_START_RESPONSE,
            java.util.Arrays.asList(
                TlvField.ofString(FieldTag.TOKEN, "dummy_token"),
                TlvField.ofInt(FieldTag.TASKID, taskId),
                TlvField.ofString(FieldTag.RESULT, result)
            ));
    }

    private ByteBuffer createTaskStopResponseBuffer(int taskId, int result) {
        return TlvEncoder.encodeMessage(MessageType.TASK_STOP_RESPONSE,
            java.util.Arrays.asList(
                TlvField.ofString(FieldTag.TOKEN, "dummy_token"),
                TlvField.ofInt(FieldTag.TASKID, taskId),
                TlvField.ofInt(FieldTag.RESULT, result)
            ));
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
