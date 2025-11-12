/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement.dto;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 启动任务请求单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class StartTaskRequestTest {
    @Test
    public void testSetAndGetBusinessType() {
        StartTaskRequest request = new StartTaskRequest();
        request.setBusinessType("VPN_BLOCK");
        assertEquals("VPN_BLOCK", request.getBusinessType());
    }

    @Test
    public void testSetAndGetScenario() {
        StartTaskRequest request = new StartTaskRequest();
        request.setScenario("TRAINING");
        assertEquals("TRAINING", request.getScenario());
    }

    @Test
    public void testSetAndGetScriptNames() {
        StartTaskRequest request = new StartTaskRequest();
        List<String> scripts = Arrays.asList("script1.py", "script2.py");
        request.setScriptNames(scripts);
        assertEquals(2, request.getScriptNames().size());
        assertTrue(request.getScriptNames().contains("script1.py"));
    }

    @Test
    public void testSetAndGetTargetUes() {
        StartTaskRequest request = new StartTaskRequest();
        List<String> ues = Arrays.asList("ue1", "ue2");
        request.setTargetUes(ues);
        assertEquals(2, request.getTargetUes().size());
        assertTrue(request.getTargetUes().contains("ue1"));
    }

    @Test
    public void testSetAndGetFailedApps() {
        StartTaskRequest request = new StartTaskRequest();
        List<String> apps = Arrays.asList("app1", "app2");
        request.setFailedApps(apps);
        assertEquals(2, request.getFailedApps().size());
        assertTrue(request.getFailedApps().contains("app1"));
    }
}

