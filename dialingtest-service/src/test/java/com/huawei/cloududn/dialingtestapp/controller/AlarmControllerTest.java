/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.Alarm;
import com.huawei.cloududn.dialingtest.model.AlarmPageResponse;
import com.huawei.cloududn.dialingtest.model.AlarmPageResponseData;
import com.huawei.cloududn.dialingtest.model.AlarmResponse;
import com.huawei.cloududn.dialingtest.model.CreateAlarmRequest;
import com.huawei.cloududn.dialingtest.service.AlarmService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 告警控制器单元测试类
 * 测试告警控制器的所有REST API接口
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RunWith(MockitoJUnitRunner.class)
public class AlarmControllerTest {

    @Mock
    private AlarmService alarmService;

    @InjectMocks
    private AlarmController alarmController;

    private Alarm testAlarm;
    private CreateAlarmRequest testCreateRequest;
    private AlarmPageResponse testPageResponse;

    @Before
    public void setUp() {
        // 初始化测试数据
        testAlarm = new Alarm();
        testAlarm.setId(1);
        testAlarm.setAlarmSummary("Test Alarm");
        testAlarm.setAlarmDescription("Test Description");
        testAlarm.setAlarmLevel("Urgent");
        testAlarm.setStartTime("2025-01-15T10:00:00");
        testAlarm.setEndTime(null);

        testCreateRequest = new CreateAlarmRequest();
        testCreateRequest.setAlarmSummary("Test Alarm");
        testCreateRequest.setAlarmDescription("Test Description");
        testCreateRequest.setAlarmLevel(CreateAlarmRequest.AlarmLevelEnum.fromValue("Urgent"));

        testPageResponse = new AlarmPageResponse();
        testPageResponse.setSuccess(true);
        testPageResponse.setMessage("Query successful");

        AlarmPageResponseData pageData = new AlarmPageResponseData();
        pageData.setContent(Collections.singletonList(testAlarm));
        pageData.setTotalElements(1);
        pageData.setTotalPages(1);
        pageData.setSize(20);
        pageData.setNumber(0);
        testPageResponse.setData(pageData);
    }

    @Test
    public void testGetAlarms_Success_ReturnsOkResponse() {
        // Arrange
        when(alarmService.getAlarms(anyInt(), anyInt(), anyBoolean())).thenReturn(testPageResponse);

        // Act
        ResponseEntity<AlarmPageResponse> response = alarmController.getAlarms(0, 20, false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(alarmService).getAlarms(0, 20, false);
    }

    @Test
    public void testGetAlarms_InvalidParameters_ReturnsBadRequest() {
        // Arrange
        when(alarmService.getAlarms(anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        // Act
        ResponseEntity<AlarmPageResponse> response = alarmController.getAlarms(0, 20, false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testGetAlarms_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(alarmService.getAlarms(anyInt(), anyInt(), anyBoolean()))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<AlarmPageResponse> response = alarmController.getAlarms(0, 20, false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testCreateAlarm_Success_ReturnsCreatedResponse() {
        // Arrange
        AlarmResponse alarmResponse = new AlarmResponse();
        alarmResponse.setSuccess(true);
        alarmResponse.setMessage("Alarm created successfully");
        alarmResponse.setData(testAlarm);
        when(alarmService.createAlarm(any(CreateAlarmRequest.class))).thenReturn(alarmResponse);

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.createAlarm(
                "csrf-token", "username", testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(alarmService).createAlarm(testCreateRequest);
    }

    @Test
    public void testCreateAlarm_InvalidParameters_ReturnsBadRequest() {
        // Arrange
        when(alarmService.createAlarm(any(CreateAlarmRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid parameters"));

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.createAlarm(
                "csrf-token", "username", testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testCreateAlarm_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(alarmService.createAlarm(any(CreateAlarmRequest.class)))
                .thenThrow(new IllegalStateException("Service error"));

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.createAlarm(
                "csrf-token", "username", testCreateRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testEndAlarm_Success_ReturnsOkResponse() {
        // Arrange
        AlarmResponse alarmResponse = new AlarmResponse();
        alarmResponse.setSuccess(true);
        alarmResponse.setMessage("Alarm ended successfully");
        alarmResponse.setData(testAlarm);
        when(alarmService.endAlarm(1)).thenReturn(alarmResponse);

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.endAlarm(
                1, "username");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        verify(alarmService).endAlarm(1);
    }

    @Test
    public void testEndAlarm_InvalidId_ReturnsBadRequest() {
        // Arrange
        when(alarmService.endAlarm(anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid alarm ID"));

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.endAlarm(
                999, "username");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testEndAlarm_AlreadyEnded_ReturnsBadRequest() {
        // Arrange
        when(alarmService.endAlarm(anyInt()))
                .thenThrow(new IllegalStateException("Alarm already ended, cannot end again"));

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.endAlarm(
                1, "username");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    public void testEndAlarm_NotFound_ReturnsNotFound() {
        // Arrange
        when(alarmService.endAlarm(anyInt()))
                .thenThrow(new RuntimeException("Alarm not found"));

        // Act
        ResponseEntity<AlarmResponse> response = alarmController.endAlarm(
                999, "username");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }
}

