/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement;

import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.UeDao;
import com.huawei.cloududn.dialingtest.model.Executor;
import com.huawei.cloududn.dialingtest.model.Ue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * ExecutorSelectionService单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
@RunWith(MockitoJUnitRunner.class)
public class ExecutorSelectionServiceTest {
    @Mock
    private ExecutorDao executorDao;

    @Mock
    private UeDao ueDao;

    @InjectMocks
    private ExecutorSelectionService selectionService;

    private Executor testExecutor;
    private Ue idleUe;
    private Ue busyUe;

    @Before
    public void setUp() {
        testExecutor = new Executor();
        testExecutor.setName("executor1");
        testExecutor.setStatus(1);
        idleUe = new Ue();
        idleUe.setMsisdn("8613800138000");
        idleUe.setExecutorName("executor1");
        idleUe.setInfo("{\"serial\":\"SN001\",\"status\":\"idle\"}");
        busyUe = new Ue();
        busyUe.setMsisdn("8613800138001");
        busyUe.setExecutorName("executor1");
        busyUe.setInfo("{\"serial\":\"SN002\",\"status\":\"busy\"}");
    }

    @Test
    public void testSelectIdleExecutorAndUe_Success() {
        List<Executor> executors = new ArrayList<>();
        executors.add(testExecutor);
        Mockito.when(executorDao.findPage(1, null, 0, 100)).thenReturn(executors);
        List<Ue> ueList = new ArrayList<>();
        ueList.add(idleUe);
        Mockito.when(ueDao.findByExecutorName("executor1")).thenReturn(ueList);
        ExecutorSelectionService.ExecutorUeInfo result = selectionService.selectIdleExecutorAndUe();
        assertNotNull(result);
        assertEquals("executor1", result.getExecutor().getName());
        assertEquals("8613800138000", result.getUe().getMsisdn());
    }

    @Test
    public void testSelectIdleExecutorAndUe_NoOnlineExecutors() {
        Mockito.when(executorDao.findPage(1, null, 0, 100)).thenReturn(new ArrayList<>());
        ExecutorSelectionService.ExecutorUeInfo result = selectionService.selectIdleExecutorAndUe();
        assertNull(result);
    }

    @Test
    public void testSelectIdleExecutorAndUe_NoIdleUe() {
        List<Executor> executors = new ArrayList<>();
        executors.add(testExecutor);
        Mockito.when(executorDao.findPage(1, null, 0, 100)).thenReturn(executors);
        List<Ue> ueList = new ArrayList<>();
        ueList.add(busyUe);
        Mockito.when(ueDao.findByExecutorName("executor1")).thenReturn(ueList);
        ExecutorSelectionService.ExecutorUeInfo result = selectionService.selectIdleExecutorAndUe();
        assertNull(result);
    }

    @Test
    public void testSelectIdleExecutorAndUe_NoUeForExecutor() {
        List<Executor> executors = new ArrayList<>();
        executors.add(testExecutor);
        Mockito.when(executorDao.findPage(1, null, 0, 100)).thenReturn(executors);
        Mockito.when(ueDao.findByExecutorName("executor1")).thenReturn(new ArrayList<>());
        ExecutorSelectionService.ExecutorUeInfo result = selectionService.selectIdleExecutorAndUe();
        assertNull(result);
    }

    @Test
    public void testSelectIdleExecutorAndUe_InvalidUeInfo() {
        List<Executor> executors = new ArrayList<>();
        executors.add(testExecutor);
        Mockito.when(executorDao.findPage(1, null, 0, 100)).thenReturn(executors);
        Ue invalidUe = new Ue();
        invalidUe.setMsisdn("8613800138002");
        invalidUe.setInfo("{invalid json");
        List<Ue> ueList = new ArrayList<>();
        ueList.add(invalidUe);
        Mockito.when(ueDao.findByExecutorName("executor1")).thenReturn(ueList);
        ExecutorSelectionService.ExecutorUeInfo result = selectionService.selectIdleExecutorAndUe();
        assertNull(result);
    }
}

