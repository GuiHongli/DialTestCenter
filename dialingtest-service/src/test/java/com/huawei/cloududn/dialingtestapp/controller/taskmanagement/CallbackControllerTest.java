/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.taskmanagement;

import com.huawei.cloududn.dialingtestapp.service.taskmanagement.orchestration.TaskOrchestratorService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;
import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CallbackControllerTest {
    @Mock
    private TaskOrchestratorService orchestratorService;

    @InjectMocks
    private CallbackController controller;

    @Test
    public void testNotifyResult_Success() {
        Map<String, Object> body = new HashMap<>();
        body.put("mainTaskId", 1);
        body.put("status", "SUCCESS");
        ResponseEntity<Object> resp = controller.notifyCallback(body);
        Mockito.verify(orchestratorService).sendResultEvent(1L, true);
        assertEquals(200, resp.getStatusCodeValue());
    }
}


