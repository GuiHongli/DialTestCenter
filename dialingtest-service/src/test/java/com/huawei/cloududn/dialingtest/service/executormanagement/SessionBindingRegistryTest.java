/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * SessionBindingRegistry 单元测试
 *
 * @author g00940940
 * @since 2025-11-09
 */
public class SessionBindingRegistryTest {
    private SessionBindingRegistry registry;

    @Before
    public void setUp() {
        registry = new SessionBindingRegistry();
    }

    @Test
    public void testBind_Success() {
        registry.bind("session1", "executor1");
        assertEquals("executor1", registry.getExecutorName("session1"));
        assertEquals("session1", registry.getSessionId("executor1"));
    }

    @Test
    public void testBind_MultipleBindings() {
        registry.bind("session1", "executor1");
        registry.bind("session2", "executor2");
        assertEquals("executor1", registry.getExecutorName("session1"));
        assertEquals("executor2", registry.getExecutorName("session2"));
        assertEquals("session1", registry.getSessionId("executor1"));
        assertEquals("session2", registry.getSessionId("executor2"));
    }

    @Test
    public void testUnbind_RemovesBinding() {
        registry.bind("session1", "executor1");
        registry.unbind("session1");
        assertNull(registry.getExecutorName("session1"));
        assertNull(registry.getSessionId("executor1"));
    }

    @Test
    public void testUnbind_NonExistentSession() {
        registry.unbind("nonexistent");
        assertNull(registry.getExecutorName("nonexistent"));
    }

    @Test
    public void testGetExecutorName_NotFound() {
        assertNull(registry.getExecutorName("nonexistent"));
    }

    @Test
    public void testGetSessionId_NotFound() {
        assertNull(registry.getSessionId("nonexistent"));
    }

    @Test
    public void testBind_Rebind() {
        registry.bind("session1", "executor1");
        registry.bind("session1", "executor2");
        assertEquals("executor2", registry.getExecutorName("session1"));
        assertEquals("session1", registry.getSessionId("executor2"));
    }
}

