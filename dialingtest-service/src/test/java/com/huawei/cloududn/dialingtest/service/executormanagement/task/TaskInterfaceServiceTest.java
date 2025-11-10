/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement.task;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.dao.taskmanagement.TaskExecutorMappingDao;
import com.huawei.cloududn.dialingtest.entity.TaskExecutorMapping;
import com.huawei.cloududn.dialingtest.service.taskmanagement.orchestration.TaskOrchestratorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import javax.websocket.Session;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TaskInterfaceService单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskInterfaceServiceTest {
    @Mock
    private WssMessageSender wssMessageSender;

    @Mock
    private TaskOrchestratorService taskOrchestratorService;

    @Mock
    private TaskExecutorMappingDao taskExecutorMappingDao;

    @Mock
    private Session webSocketSession;

    @InjectMocks
    private TaskInterfaceService taskInterfaceService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        Mockito.when(webSocketSession.getId()).thenReturn("session1");
    }

    @Test
    public void testHandleTaskStatusUpdate_Success() throws Exception {
        String jsonData = "{\"task_id\":\"123\",\"status\":\"success\",\"result_code\":0,\"message\":\"ok\"}";
        JsonNode data = objectMapper.readTree(jsonData);
        TaskExecutorMapping mapping = new TaskExecutorMapping();
        mapping.setTaskId("123");
        mapping.setExecutorName("executor1");
        Mockito.when(taskExecutorMappingDao.findByTaskId("123")).thenReturn(mapping);
        taskInterfaceService.handleTaskStatusUpdate(data, webSocketSession);
        ArgumentCaptor<Long> taskIdCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> successCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(taskOrchestratorService).sendResultEvent(taskIdCaptor.capture(), 
            successCaptor.capture(), dataCaptor.capture());
        assertEquals(Long.valueOf(123), taskIdCaptor.getValue());
        assertTrue(successCaptor.getValue());
        Map<String, Object> resultData = dataCaptor.getValue();
        assertEquals(0, resultData.get("result_code"));
        assertEquals("ok", resultData.get("message"));
    }

    @Test
    public void testHandleTaskStatusUpdate_Failed() throws Exception {
        String jsonData = "{\"task_id\":\"456\",\"status\":\"failed\",\"result_code\":1,\"message\":\"error\"}";
        JsonNode data = objectMapper.readTree(jsonData);
        taskInterfaceService.handleTaskStatusUpdate(data, webSocketSession);
        ArgumentCaptor<Boolean> successCaptor = ArgumentCaptor.forClass(Boolean.class);
        Mockito.verify(taskOrchestratorService).sendResultEvent(Mockito.anyLong(), 
            successCaptor.capture(), Mockito.anyMap());
        assertFalse(successCaptor.getValue());
    }

    @Test
    public void testHandleTaskStatusUpdate_Timeout() throws Exception {
        String jsonData = "{\"task_id\":\"789\",\"status\":\"timeout\"}";
        JsonNode data = objectMapper.readTree(jsonData);
        taskInterfaceService.handleTaskStatusUpdate(data, webSocketSession);
        ArgumentCaptor<Boolean> successCaptor = ArgumentCaptor.forClass(Boolean.class);
        Mockito.verify(taskOrchestratorService).sendResultEvent(Mockito.anyLong(), 
            successCaptor.capture(), Mockito.anyMap());
        assertFalse(successCaptor.getValue());
    }

    @Test
    public void testHandleTaskStatusUpdate_UnknownStatus() throws Exception {
        String jsonData = "{\"task_id\":\"999\",\"status\":\"unknown\"}";
        JsonNode data = objectMapper.readTree(jsonData);
        taskInterfaceService.handleTaskStatusUpdate(data, webSocketSession);
        ArgumentCaptor<Boolean> successCaptor = ArgumentCaptor.forClass(Boolean.class);
        Mockito.verify(taskOrchestratorService).sendResultEvent(Mockito.anyLong(), 
            successCaptor.capture(), Mockito.anyMap());
        assertFalse(successCaptor.getValue());
    }

    @Test
    public void testHandleTaskStatusUpdate_WithLogPath() throws Exception {
        String jsonData = "{\"task_id\":\"111\",\"status\":\"success\",\"log_path\":\"/var/log/task.log\"}";
        JsonNode data = objectMapper.readTree(jsonData);
        taskInterfaceService.handleTaskStatusUpdate(data, webSocketSession);
        ArgumentCaptor<Map> dataCaptor = ArgumentCaptor.forClass(Map.class);
        Mockito.verify(taskOrchestratorService).sendResultEvent(Mockito.anyLong(), 
            Mockito.anyBoolean(), dataCaptor.capture());
        Map<String, Object> resultData = dataCaptor.getValue();
        assertEquals("/var/log/task.log", resultData.get("log_path"));
    }
}
