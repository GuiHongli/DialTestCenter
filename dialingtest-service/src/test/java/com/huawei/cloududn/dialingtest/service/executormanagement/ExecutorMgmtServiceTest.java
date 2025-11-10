package com.huawei.cloududn.dialingtest.service.executormanagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.UeDao;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExecutorMgmtServiceTest {

    @Mock
    private ExecutorDao executorDao;

    @Mock
    private UeDao ueDao;

    @Mock
    private SessionBindingRegistry registry;

    @InjectMocks
    private ExecutorMgmtService service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandleHeartbeatStatus_WithBinding_UpdatesDaoAndUe() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        when(registry.getExecutorName("s1")).thenReturn("Exec_01");
        String data = "{ \"ue_list\": [] }";

        service.handleHeartbeatStatus(new ObjectMapper().readTree(data), session);

        verify(executorDao).updateStatus(Mockito.eq("Exec_01"), Mockito.eq(1), Mockito.any());
    }

    @Test
    public void testHandleHeartbeatStatus_NoBinding_SkipUpdate() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        when(registry.getExecutorName("s1")).thenReturn(null);

        service.handleHeartbeatStatus(new ObjectMapper().readTree("{\"ue_list\":[]}"), session);

        verify(executorDao, never()).updateStatus(Mockito.anyString(), Mockito.anyInt(), Mockito.any());
        // no UE upsert expected
    }

    @Test
    public void testHandleExecutorDisconnect_BindingExists_UpdateAndUnbind() {
        when(registry.getExecutorName("s2")).thenReturn("Exec_02");
        service.handleExecutorDisconnect("s2");
        verify(executorDao).updateStatus(Mockito.eq("Exec_02"), Mockito.eq(0), Mockito.any());
        verify(registry).unbind("s2");
    }

    @Test
    public void testHandleExecutorDisconnect_NoBinding_NoUpdate() {
        when(registry.getExecutorName("s3")).thenReturn(null);
        service.handleExecutorDisconnect("s3");
        verify(executorDao, never()).updateStatus(Mockito.anyString(), Mockito.anyInt(), Mockito.any());
        verify(registry, never()).unbind(Mockito.anyString());
    }

    @Test
    public void testHandleDeregister_MatchingName_UpdatesAndUnbinds() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        when(registry.getExecutorName("s1")).thenReturn("Exec_01");
        String data = "{\"name\":\"Exec_01\"}";

        service.handleDeregister(new ObjectMapper().readTree(data), session);

        verify(executorDao).updateStatus(Mockito.eq("Exec_01"), Mockito.eq(0), Mockito.any());
        verify(registry).unbind("s1");
    }

    @Test
    public void testHandleDeregister_MismatchedName_SkipsUpdate() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s2");
        when(registry.getExecutorName("s2")).thenReturn("Exec_01");
        String data = "{\"name\":\"Exec_02\"}";

        service.handleDeregister(new ObjectMapper().readTree(data), session);

        verify(executorDao, never()).updateStatus(Mockito.anyString(), Mockito.anyInt(), Mockito.any());
        verify(registry, never()).unbind(Mockito.anyString());
    }

    @Test
    public void testHandleDeregister_MissingName_SkipsUpdate() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s3");
        String data = "{}";

        service.handleDeregister(new ObjectMapper().readTree(data), session);

        verify(executorDao, never()).updateStatus(Mockito.anyString(), Mockito.anyInt(), Mockito.any());
        verify(registry, never()).unbind(Mockito.anyString());
    }

    @Test
    public void testHandleExecutorInfoResponse_WithUeDetails_UpsertsCalled() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s1");
        String data = "{\"executor_name\":\"Exec_01\",\"ue_details\":[{\"msisdn\":\"86138\",\"vendor\":\"Huawei\"}]}";

        service.handleExecutorInfoResponse(new ObjectMapper().readTree(data), session);

        verify(ueDao).upsert(Mockito.any());
    }

    @Test
    public void testHandleExecutorInfoResponse_NoUeDetails_NoDaoCall() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s2");
        String data = "{\"executor_name\":\"Exec_02\"}";

        service.handleExecutorInfoResponse(new ObjectMapper().readTree(data), session);

        verify(ueDao, never()).upsert(Mockito.any());
    }

    @Test
    public void testHandleExecutorInfoResponse_MissingExecutorName_SkipsProcessing() throws Exception {
        Session session = Mockito.mock(Session.class);
        when(session.getId()).thenReturn("s3");
        String data = "{\"ue_details\":[]}";

        service.handleExecutorInfoResponse(new ObjectMapper().readTree(data), session);

        verify(ueDao, never()).upsert(Mockito.any());
    }
}
