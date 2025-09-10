/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * OperationLog实体类测试
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogTest {

    @Test
    public void testDefaultConstructor() {
        OperationLog operationLog = new OperationLog();
        
        assertNotNull("OperationLog should not be null", operationLog);
        assertNull("ID should be null initially", operationLog.getId());
        assertNull("Username should be null initially", operationLog.getUsername());
        assertNull("Operation time should be null initially", operationLog.getOperationTime());
        assertNull("Operation type should be null initially", operationLog.getOperationType());
        assertNull("Target should be null initially", operationLog.getTarget());
        assertNull("Description should be null initially", operationLog.getDescription());
    }

    @Test
    public void testParameterizedConstructor() {
        String username = "testuser";
        String operationType = "CREATE";
        String target = "用户管理";
        String description = "创建新用户";
        
        OperationLog operationLog = new OperationLog(username, operationType, target, description);
        
        assertNotNull("OperationLog should not be null", operationLog);
        assertEquals("Username should match", username, operationLog.getUsername());
        assertEquals("Operation type should match", operationType, operationLog.getOperationType());
        assertEquals("Target should match", target, operationLog.getTarget());
        assertEquals("Description should match", description, operationLog.getDescription());
        assertNotNull("Operation time should be set", operationLog.getOperationTime());
    }

    @Test
    public void testParameterizedConstructorWithoutDescription() {
        String username = "testuser";
        String operationType = "UPDATE";
        String target = "角色管理";
        
        OperationLog operationLog = new OperationLog(username, operationType, target, null);
        
        assertNotNull("OperationLog should not be null", operationLog);
        assertEquals("Username should match", username, operationLog.getUsername());
        assertEquals("Operation type should match", operationType, operationLog.getOperationType());
        assertEquals("Target should match", target, operationLog.getTarget());
        assertNull("Description should be null", operationLog.getDescription());
        assertNotNull("Operation time should be set", operationLog.getOperationTime());
    }

    @Test
    public void testGettersAndSetters() {
        OperationLog operationLog = new OperationLog();
        
        // Test ID
        Long id = 1L;
        operationLog.setId(id);
        assertEquals("ID getter/setter should work", id, operationLog.getId());
        
        // Test username
        String username = "testuser";
        operationLog.setUsername(username);
        assertEquals("Username getter/setter should work", username, operationLog.getUsername());
        
        // Test operation time
        LocalDateTime operationTime = LocalDateTime.now();
        operationLog.setOperationTime(operationTime);
        assertEquals("Operation time getter/setter should work", operationTime, operationLog.getOperationTime());
        
        // Test operation type
        String operationType = "DELETE";
        operationLog.setOperationType(operationType);
        assertEquals("Operation type getter/setter should work", operationType, operationLog.getOperationType());
        
        // Test target
        String target = "测试用例集";
        operationLog.setTarget(target);
        assertEquals("Target getter/setter should work", target, operationLog.getTarget());
        
        // Test description
        String description = "删除测试用例集";
        operationLog.setDescription(description);
        assertEquals("Description getter/setter should work", description, operationLog.getDescription());
    }

    @Test
    public void testEquals() {
        OperationLog operationLog1 = new OperationLog();
        operationLog1.setId(1L);
        
        OperationLog operationLog2 = new OperationLog();
        operationLog2.setId(1L);
        
        OperationLog operationLog3 = new OperationLog();
        operationLog3.setId(2L);
        
        // Test equals with same ID
        assertTrue("OperationLogs with same ID should be equal", operationLog1.equals(operationLog2));
        
        // Test equals with different ID
        assertFalse("OperationLogs with different ID should not be equal", operationLog1.equals(operationLog3));
        
        // Test equals with null
        assertFalse("OperationLog should not be equal to null", operationLog1.equals(null));
        
        // Test equals with different class
        assertFalse("OperationLog should not be equal to different class", operationLog1.equals("string"));
        
        // Test equals with itself
        assertTrue("OperationLog should be equal to itself", operationLog1.equals(operationLog1));
    }

    @Test
    public void testHashCode() {
        OperationLog operationLog1 = new OperationLog();
        operationLog1.setId(1L);
        
        OperationLog operationLog2 = new OperationLog();
        operationLog2.setId(1L);
        
        // Test hashCode consistency
        assertEquals("HashCode should be consistent", operationLog1.hashCode(), operationLog1.hashCode());
        assertEquals("HashCode should be consistent", operationLog2.hashCode(), operationLog2.hashCode());
    }

    @Test
    public void testToString() {
        OperationLog operationLog = new OperationLog();
        operationLog.setId(1L);
        operationLog.setUsername("testuser");
        operationLog.setOperationType("CREATE");
        operationLog.setTarget("用户管理");
        operationLog.setDescription("创建新用户");
        
        String toString = operationLog.toString();
        
        assertNotNull("ToString should not be null", toString);
        assertTrue("ToString should contain class name", toString.contains("OperationLog"));
        assertTrue("ToString should contain ID", toString.contains("id=1"));
        assertTrue("ToString should contain username", toString.contains("username='testuser'"));
        assertTrue("ToString should contain operation type", toString.contains("operationType='CREATE'"));
        assertTrue("ToString should contain target", toString.contains("target='用户管理'"));
        assertTrue("ToString should contain description", toString.contains("description='创建新用户'"));
    }
}
