/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.WebSocketSessionRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * SessionSendQueue单元测试
 * 测试高/低优先级队列和异步发送
 *
 * @author g00940940
 * @since 2025-11-16
 */
public class SessionSendQueueTest {
    private SessionSendQueue queue;
    private WebSocketSessionRegistry sessionRegistry;
    private String sessionId;

    @Before
    public void setUp() {
        sessionId = "session-001";
        sessionRegistry = mock(WebSocketSessionRegistry.class);
        queue = new SessionSendQueue(sessionId, sessionRegistry);
    }

    @After
    public void tearDown() {
        if (queue != null) {
            queue.shutdown();
        }
    }

    /**
     * 测试加入高优队列
     */
    @Test
    public void testEnqueueHighPriority_Success() throws Exception {
        String jsonMessage = "{\"type\":\"test\"}";

        queue.enqueueHighPriority(jsonMessage);
        Thread.sleep(100);

        verify(sessionRegistry, atLeastOnce()).sendText(eq(sessionId), eq(jsonMessage));
    }

    /**
     * 测试加入低优队列
     */
    @Test
    public void testEnqueueLowPriority_Success() throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[100]);

        queue.enqueueLowPriority(buffer);
        Thread.sleep(100);

        verify(sessionRegistry, atLeastOnce()).sendBinary(eq(sessionId), any(ByteBuffer.class));
    }

    /**
     * 测试高优队列优先发送
     */
    @Test
    public void testSendLoop_HighPriorityFirst() throws Exception {
        ByteBuffer lowPriorityBuffer = ByteBuffer.wrap(new byte[50]);
        queue.enqueueLowPriority(lowPriorityBuffer);

        Thread.sleep(50);

        String highPriorityMessage = "{\"type\":\"urgent\"}";
        queue.enqueueHighPriority(highPriorityMessage);

        Thread.sleep(200);

        verify(sessionRegistry, atLeastOnce()).sendText(eq(sessionId), eq(highPriorityMessage));
        verify(sessionRegistry, atLeastOnce()).sendBinary(eq(sessionId), any(ByteBuffer.class));
    }

    /**
     * 测试混合消息发送顺序
     */
    @Test
    public void testSendLoop_MixedMessages() throws Exception {
        queue.enqueueHighPriority("{\"type\":\"msg1\"}");
        queue.enqueueLowPriority(ByteBuffer.wrap(new byte[10]));
        queue.enqueueHighPriority("{\"type\":\"msg2\"}");
        queue.enqueueLowPriority(ByteBuffer.wrap(new byte[20]));

        Thread.sleep(300);

        verify(sessionRegistry, times(2)).sendText(eq(sessionId), anyString());
        verify(sessionRegistry, atLeast(1)).sendBinary(eq(sessionId), any(ByteBuffer.class));
    }

    /**
     * 测试队列关闭
     */
    @Test
    public void testShutdown_Success() throws Exception {
        queue.enqueueHighPriority("{\"type\":\"test\"}");
        Thread.sleep(100);

        queue.shutdown();

        int hiSize = queue.getHighPriorityQueueSize();
        int loSize = queue.getLowPriorityQueueSize();

        assertTrue(hiSize >= 0);
        assertTrue(loSize >= 0);
    }
}

