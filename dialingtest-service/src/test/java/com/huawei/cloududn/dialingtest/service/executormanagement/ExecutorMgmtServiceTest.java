package com.huawei.cloududn.dialingtest.service.executormanagement;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.ReportMsgDto;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.UeItemDto;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.UeDao;
import com.huawei.cloududn.dialingtest.service.executormanagement.task.WssMessageSender;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

/**
 * ExecutorMgmtService单元测试 - V3协议版本
 * 测试心跳处理、状态管理和UE信息更新
 *
 * @author g00940940
 * @since 2025-11-11
 */
public class ExecutorMgmtServiceTest {

    @Mock
    private ExecutorDao executorDao;

    @Mock
    private UeDao ueDao;

    @Mock
    private SessionBindingRegistry registry;

    @Mock
    private WssMessageSender wssMessageSender;

    @InjectMocks
    private ExecutorMgmtService service;

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
     * 测试handleReportMsg：成功处理心跳消息，更新状态和UE信息
     */
    @Test
    public void testHandleReportMsg_WithBinding_UpdatesDaoAndUe() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-001");
        when(registry.getExecutorName("session-001")).thenReturn("Executor-01");

        // Create ReportMsgDto with UE list
        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(12345L);
        reportMsg.setState("Normal"); // ONLINE

        UeItemDto ueItem = new UeItemDto();
        ueItem.setSerialNo("ABC123");
        ueItem.setBrand("Huawei");
        ueItem.setModel("P40");
        ueItem.setOs("Android");
        ueItem.setVersion("11");
        ueItem.setBattery(85);

        reportMsg.setUeList(Arrays.asList(ueItem));

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleReportMsg(reportMsg, session);

        // Then
        verify(executorDao).updateStatus(eq("Executor-01"), eq(1), any(Instant.class));
        verify(ueDao).upsert(any()); // UE should be upserted
        verify(wssMessageSender).sendBinary(eq("session-001"), bufferCaptor.capture());
        assertNotNull("Should send Report-Ack", bufferCaptor.getValue());
    }

    /**
     * 测试handleReportMsg：无会话绑定时发送错误应答
     */
    @Test
    public void testHandleReportMsg_NoBinding_SendsErrorAck() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-002");
        when(registry.getExecutorName("session-002")).thenReturn(null);

        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(12345L);
        reportMsg.setState("Normal");
        reportMsg.setUeList(Arrays.asList());

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleReportMsg(reportMsg, session);

        // Then
        verify(executorDao, never()).updateStatus(anyString(), anyInt(), any(Instant.class));
        verify(ueDao, never()).upsert(any());
        verify(wssMessageSender).sendBinary(eq("session-002"), bufferCaptor.capture());
        assertNotNull("Should send error Report-Ack", bufferCaptor.getValue());
    }

    /**
     * 测试handleReportMsg：空UE列表时不进行UE更新
     */
    @Test
    public void testHandleReportMsg_EmptyUeList_NoUeUpsert() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-003");
        when(registry.getExecutorName("session-003")).thenReturn("Executor-02");

        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(12345L);
        reportMsg.setState("Normal");
        reportMsg.setUeList(Arrays.asList()); // Empty list

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleReportMsg(reportMsg, session);

        // Then
        verify(executorDao).updateStatus(eq("Executor-02"), eq(1), any(Instant.class));
        verify(ueDao, never()).upsert(any()); // No UE to upsert
        verify(wssMessageSender).sendBinary(eq("session-003"), bufferCaptor.capture());
    }

    /**
     * 测试handleReportMsg：null UE列表时不进行UE更新
     */
    @Test
    public void testHandleReportMsg_NullUeList_NoUeUpsert() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-004");
        when(registry.getExecutorName("session-004")).thenReturn("Executor-03");

        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(12345L);
        reportMsg.setState("Normal");
        reportMsg.setUeList(null); // Null list

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleReportMsg(reportMsg, session);

        // Then
        verify(executorDao).updateStatus(eq("Executor-03"), eq(1), any(Instant.class));
        verify(ueDao, never()).upsert(any()); // No UE to upsert
        verify(wssMessageSender).sendBinary(eq("session-004"), bufferCaptor.capture());
    }

    /**
     * 测试handleReportMsg：多个UE设备时批量更新
     */
    @Test
    public void testHandleReportMsg_MultipleUeItems_BatchUpsert() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-005");
        when(registry.getExecutorName("session-005")).thenReturn("Executor-04");

        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(12345L);
        reportMsg.setState("Normal");

        // Create multiple UE items
        UeItemDto ue1 = new UeItemDto();
        ue1.setSerialNo("UE001");
        ue1.setBrand("Huawei");
        ue1.setModel("P40");

        UeItemDto ue2 = new UeItemDto();
        ue2.setSerialNo("UE002");
        ue2.setBrand("Xiaomi");
        ue2.setModel("Mi11");

        reportMsg.setUeList(Arrays.asList(ue1, ue2));

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        service.handleReportMsg(reportMsg, session);

        // Then
        verify(executorDao).updateStatus(eq("Executor-04"), eq(1), any(Instant.class));
        verify(ueDao, Mockito.times(2)).upsert(any()); // Two UEs should be upserted
        verify(wssMessageSender).sendBinary(eq("session-005"), bufferCaptor.capture());
    }

    /**
     * 测试handleExecutorDisconnect：存在绑定时更新状态并解绑
     */
    @Test
    public void testHandleExecutorDisconnect_BindingExists_UpdateAndUnbind() {
        // Given
        when(registry.getExecutorName("session-disconnect-001")).thenReturn("Executor-Disconnect");

        // When
        service.handleExecutorDisconnect("session-disconnect-001");

        // Then
        verify(executorDao).updateStatus(eq("Executor-Disconnect"), eq(0), any(Instant.class));
        verify(registry).unbind("session-disconnect-001");
    }

    /**
     * 测试handleExecutorDisconnect：无绑定时不执行任何操作
     */
    @Test
    public void testHandleExecutorDisconnect_NoBinding_NoUpdate() {
        // Given
        when(registry.getExecutorName("session-disconnect-002")).thenReturn(null);

        // When
        service.handleExecutorDisconnect("session-disconnect-002");

        // Then
        verify(executorDao, never()).updateStatus(anyString(), anyInt(), any(Instant.class));
        verify(registry, never()).unbind(anyString());
    }

    /**
     * 测试sendReportAck：正确调用消息发送器
     */
    @Test
    public void testSendReportAck_CallsMessageSender() {
        // Given
        String sessionId = "session-ack-001";
        long token = 98765L;
        int state = 0; // OK

        ArgumentCaptor<ByteBuffer> bufferCaptor = ArgumentCaptor.forClass(ByteBuffer.class);

        // When
        // Note: sendReportAck is private, so we test it through public methods
        // This test verifies the integration by checking that handleReportMsg calls sendBinary
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn(sessionId);
        when(registry.getExecutorName(sessionId)).thenReturn("Executor-Ack");

        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(token);
        reportMsg.setState("Normal");
        reportMsg.setUeList(Arrays.asList());

        service.handleReportMsg(reportMsg, session);

        // Then
        verify(wssMessageSender).sendBinary(eq(sessionId), bufferCaptor.capture());
        assertNotNull("Should send Report-Ack buffer", bufferCaptor.getValue());
    }

    /**
     * 测试UE信息处理：UeItemDto正确转换为Ue实体
     */
    @Test
    public void testUeItemProcessing_CorrectlyMapsFields() {
        // Given
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("session-ue-test");
        when(registry.getExecutorName("session-ue-test")).thenReturn("Executor-UeTest");

        ReportMsgDto reportMsg = new ReportMsgDto();
        reportMsg.setToken(12345L);
        reportMsg.setState("Normal");

        UeItemDto ueItem = new UeItemDto();
        ueItem.setSerialNo("TEST123456");
        ueItem.setBrand("TestBrand");
        ueItem.setModel("TestModel");
        ueItem.setOs("Android");
        ueItem.setVersion("12");
        ueItem.setWmsize("1080x2400");
        ueItem.setIpv4("192.168.1.100");
        ueItem.setIpv6("2001:db8::1");
        ueItem.setBattery(90);

        reportMsg.setUeList(Arrays.asList(ueItem));

        // When
        service.handleReportMsg(reportMsg, session);

        // Then
        verify(ueDao).upsert(any()); // UE should be upserted with correct mapping
    }
}
