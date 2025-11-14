/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.AlarmDao;
import com.huawei.cloududn.dialingtest.model.Alarm;
import com.huawei.cloududn.dialingtest.model.AlarmPageResponse;
import com.huawei.cloududn.dialingtest.model.AlarmResponse;
import com.huawei.cloududn.dialingtest.model.CreateAlarmRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 告警服务测试类
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RunWith(MockitoJUnitRunner.class)
public class AlarmServiceTest {

    @Mock
    private AlarmDao alarmDao;

    @InjectMocks
    private AlarmService alarmService;

    private Alarm testAlarm;
    private CreateAlarmRequest testRequest;

    @Before
    public void setUp() {
        // 设置测试告警
        testAlarm = new Alarm();
        testAlarm.setId(1);
        testAlarm.setAlarmSummary("Test Alarm");
        testAlarm.setAlarmDescription("Test Description");
        testAlarm.setAlarmLevel("Urgent");
        testAlarm.setStartTime("2025-01-15T10:00:00");
        testAlarm.setEndTime(null);

        // 设置测试请求
        testRequest = new CreateAlarmRequest();
        testRequest.setAlarmSummary("Test Alarm");
        testRequest.setAlarmDescription("Test Description");
        testRequest.setAlarmLevel("Urgent");
    }

    @Test
    public void testCreateAlarm_Success_Urgent() {
        // Arrange
        when(alarmDao.save(any(Alarm.class))).thenAnswer(invocation -> {
            Alarm alarm = invocation.getArgument(0);
            alarm.setId(1);
            return 1;
        });
        when(alarmDao.findById(1L)).thenReturn(testAlarm);

        // Act
        AlarmResponse response = alarmService.createAlarm(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Alarm created successfully", response.getMessage());
        assertNotNull(response.getData());
        verify(alarmDao).save(any(Alarm.class));
        verify(alarmDao).findById(1L);
    }

    @Test
    public void testCreateAlarm_Success_Important() {
        // Arrange
        testRequest.setAlarmLevel("Important");
        when(alarmDao.save(any(Alarm.class))).thenAnswer(invocation -> {
            Alarm alarm = invocation.getArgument(0);
            alarm.setId(1);
            return 1;
        });
        when(alarmDao.findById(1L)).thenReturn(testAlarm);

        // Act
        AlarmResponse response = alarmService.createAlarm(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(alarmDao).save(any(Alarm.class));
    }

    @Test
    public void testCreateAlarm_Success_Minor() {
        // Arrange
        testRequest.setAlarmLevel("Minor");
        when(alarmDao.save(any(Alarm.class))).thenAnswer(invocation -> {
            Alarm alarm = invocation.getArgument(0);
            alarm.setId(1);
            return 1;
        });
        when(alarmDao.findById(1L)).thenReturn(testAlarm);

        // Act
        AlarmResponse response = alarmService.createAlarm(testRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(alarmDao).save(any(Alarm.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlarm_MissingSummary() {
        // Arrange
        testRequest.setAlarmSummary(null);

        // Act
        alarmService.createAlarm(testRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlarm_MissingLevel() {
        // Arrange
        testRequest.setAlarmLevel(null);

        // Act
        alarmService.createAlarm(testRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlarm_EmptyLevel() {
        // Arrange
        // 设置空字符串，会在Service层的验证中抛出异常
        testRequest.setAlarmLevel("");

        // Act
        alarmService.createAlarm(testRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlarm_SummaryTooLong() {
        // Arrange
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 201; i++) {
            sb.append("a");
        }
        String longSummary = sb.toString();
        testRequest.setAlarmSummary(longSummary);

        // Act
        alarmService.createAlarm(testRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateAlarm_InvalidLevel() {
        // Arrange
        // 设置无效的告警级别，会在Service层的验证中抛出异常
        testRequest.setAlarmLevel("Invalid");

        // Act
        alarmService.createAlarm(testRequest);
    }

    @Test(expected = IllegalStateException.class)
    public void testCreateAlarm_DatabaseError() {
        // Arrange
        when(alarmDao.save(any(Alarm.class))).thenReturn(0);

        // Act
        alarmService.createAlarm(testRequest);
    }

    @Test
    public void testEndAlarm_Success() {
        // Arrange
        when(alarmDao.findById(1L)).thenReturn(testAlarm);
        when(alarmDao.updateEndTime(1L)).thenReturn(1);
        Alarm endedAlarm = new Alarm();
        endedAlarm.setId(1);
        endedAlarm.setEndTime("2025-01-15T11:00:00");
        when(alarmDao.findById(1L)).thenReturn(endedAlarm);

        // Act
        AlarmResponse response = alarmService.endAlarm(1);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Alarm ended successfully", response.getMessage());
        verify(alarmDao).findById(1L);
        verify(alarmDao).updateEndTime(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndAlarm_InvalidId() {
        // Act
        alarmService.endAlarm(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndAlarm_NegativeId() {
        // Act
        alarmService.endAlarm(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEndAlarm_NotFound() {
        // Arrange
        when(alarmDao.findById(999L)).thenReturn(null);

        // Act
        alarmService.endAlarm(999);
    }

    @Test(expected = IllegalStateException.class)
    public void testEndAlarm_AlreadyEnded() {
        // Arrange
        Alarm endedAlarm = new Alarm();
        endedAlarm.setId(1);
        endedAlarm.setEndTime("2025-01-15T11:00:00");
        when(alarmDao.findById(1L)).thenReturn(endedAlarm);

        // Act
        alarmService.endAlarm(1);
    }

    @Test(expected = IllegalStateException.class)
    public void testEndAlarm_DatabaseError() {
        // Arrange
        when(alarmDao.findById(1L)).thenReturn(testAlarm);
        when(alarmDao.updateEndTime(1L)).thenReturn(0);

        // Act
        alarmService.endAlarm(1);
    }

    @Test
    public void testGetAlarms_Success() {
        // Arrange
        List<Alarm> alarms = Arrays.asList(testAlarm);
        when(alarmDao.findAlarmsWithPagination(0, 20, false)).thenReturn(alarms);
        when(alarmDao.countAlarms(false)).thenReturn(1L);

        // Act
        AlarmPageResponse response = alarmService.getAlarms(0, 20, false);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("Query successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(Integer.valueOf(1), Integer.valueOf(response.getData().getTotalElements()));
        verify(alarmDao).findAlarmsWithPagination(0, 20, false);
        verify(alarmDao).countAlarms(false);
    }

    @Test
    public void testGetAlarms_EmptyResult() {
        // Arrange
        when(alarmDao.findAlarmsWithPagination(0, 20, true)).thenReturn(Arrays.asList());
        when(alarmDao.countAlarms(true)).thenReturn(0L);

        // Act
        AlarmPageResponse response = alarmService.getAlarms(0, 20, true);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals(Integer.valueOf(0), Integer.valueOf(response.getData().getTotalElements()));
    }

    @Test
    public void testGetAlarms_InvalidPageSize() {
        // Arrange
        List<Alarm> alarms = Arrays.asList(testAlarm);
        when(alarmDao.findAlarmsWithPagination(0, 20, false)).thenReturn(alarms);
        when(alarmDao.countAlarms(false)).thenReturn(1L);

        // Act
        AlarmPageResponse response = alarmService.getAlarms(0, 0, false);

        // Assert
        assertNotNull(response);
        assertEquals(Integer.valueOf(20), Integer.valueOf(response.getData().getSize()));
    }

    @Test
    public void testGetAlarms_InvalidPageNumber() {
        // Arrange
        List<Alarm> alarms = Arrays.asList(testAlarm);
        when(alarmDao.findAlarmsWithPagination(0, 20, false)).thenReturn(alarms);
        when(alarmDao.countAlarms(false)).thenReturn(1L);

        // Act
        AlarmPageResponse response = alarmService.getAlarms(-1, 20, false);

        // Assert
        assertNotNull(response);
        assertEquals(Integer.valueOf(0), Integer.valueOf(response.getData().getNumber()));
    }

    @Test
    public void testGetAlarms_SortOrder() {
        // Arrange
        Alarm alarm1 = new Alarm();
        alarm1.setId(1);
        alarm1.setStartTime("2025-01-15T10:00:00");
        Alarm alarm2 = new Alarm();
        alarm2.setId(2);
        alarm2.setStartTime("2025-01-15T11:00:00");
        List<Alarm> alarms = Arrays.asList(alarm2, alarm1);
        when(alarmDao.findAlarmsWithPagination(0, 20, false)).thenReturn(alarms);
        when(alarmDao.countAlarms(false)).thenReturn(2L);

        // Act
        AlarmPageResponse response = alarmService.getAlarms(0, 20, false);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getData().getContent().size());
    }

    @Test
    public void testGetAlarms_OnlyCurrent() {
        // Arrange
        List<Alarm> alarms = Arrays.asList(testAlarm);
        when(alarmDao.findAlarmsWithPagination(0, 20, true)).thenReturn(alarms);
        when(alarmDao.countAlarms(true)).thenReturn(1L);

        // Act
        AlarmPageResponse response = alarmService.getAlarms(0, 20, true);

        // Assert
        assertNotNull(response);
        verify(alarmDao).findAlarmsWithPagination(0, 20, true);
        verify(alarmDao).countAlarms(true);
    }
}

