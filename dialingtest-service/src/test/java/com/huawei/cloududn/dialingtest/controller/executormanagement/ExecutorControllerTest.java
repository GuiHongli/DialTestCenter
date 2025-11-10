/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.ExecutorWebsocketEndpoint;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ExecutorController 单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class ExecutorControllerTest {
    @Mock
    private ExecutorDao executorDao;

    @Mock
    private SessionBindingRegistry registry;

    @Mock
    private ExecutorWebsocketEndpoint endpoint;

    @InjectMocks
    private ExecutorController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testListExecutors_Success() {
        List<com.huawei.cloududn.dialingtest.model.Executor> executors = new ArrayList<>();
        com.huawei.cloududn.dialingtest.model.Executor executor = new com.huawei.cloududn.dialingtest.model.Executor();
        executor.setName("executor1");
        executors.add(executor);
        when(executorDao.findPage(any(), any(), eq(0), eq(20))).thenReturn(executors);
        when(executorDao.count(any(), any())).thenReturn(1);

        ResponseEntity<com.huawei.cloududn.dialingtest.model.ExecutorPageResponse> response =
                controller.listExecutors(0, 20, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    public void testListExecutors_InvalidPageParameters() {
        ResponseEntity<com.huawei.cloududn.dialingtest.model.ExecutorPageResponse> response =
                controller.listExecutors(-1, 20, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid page parameters", response.getBody().getMessage());
    }

    @Test
    public void testListExecutors_InvalidSizeParameters() {
        ResponseEntity<com.huawei.cloududn.dialingtest.model.ExecutorPageResponse> response =
                controller.listExecutors(0, 0, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testListExecutors_SizeExceedsLimit() {
        ResponseEntity<com.huawei.cloududn.dialingtest.model.ExecutorPageResponse> response =
                controller.listExecutors(0, 201, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRefreshExecutor_Success() throws IOException {
        com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest request =
                new com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest();
        request.setName("executor1");
        when(registry.getSessionId("executor1")).thenReturn("session1");

        ResponseEntity<com.huawei.cloududn.dialingtest.model.OperationResponse> response =
                controller.refreshExecutor(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Refresh command sent", response.getBody().getMessage());
        verify(endpoint).sendMessage(eq("session1"), any(WssMessage.class));
    }

    @Test
    public void testRefreshExecutor_MissingExecutorName() {
        com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest request =
                new com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest();
        request.setName(null);

        ResponseEntity<com.huawei.cloududn.dialingtest.model.OperationResponse> response =
                controller.refreshExecutor(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Missing executor name", response.getBody().getMessage());
    }

    @Test
    public void testRefreshExecutor_EmptyExecutorName() {
        com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest request =
                new com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest();
        request.setName("  ");

        ResponseEntity<com.huawei.cloududn.dialingtest.model.OperationResponse> response =
                controller.refreshExecutor(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testRefreshExecutor_ExecutorNotOnline() {
        com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest request =
                new com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest();
        request.setName("executor1");
        when(registry.getSessionId("executor1")).thenReturn(null);

        ResponseEntity<com.huawei.cloududn.dialingtest.model.OperationResponse> response =
                controller.refreshExecutor(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Executor not online", response.getBody().getMessage());
    }

    @Test
    public void testRefreshExecutor_SendFailed() throws IOException {
        com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest request =
                new com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest();
        request.setName("executor1");
        when(registry.getSessionId("executor1")).thenReturn("session1");
        doThrow(new IOException("Send failed")).when(endpoint).sendMessage(anyString(), any(WssMessage.class));

        ResponseEntity<com.huawei.cloududn.dialingtest.model.OperationResponse> response =
                controller.refreshExecutor(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Send failed", response.getBody().getMessage());
    }

    @Test
    public void testRefreshExecutor_NullRequest() {
        ResponseEntity<com.huawei.cloududn.dialingtest.model.OperationResponse> response =
                controller.refreshExecutor(null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
