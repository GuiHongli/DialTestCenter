/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * ApiResponse测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class ApiResponseTest {

    @Test
    public void testDefaultConstructor() {
        ApiResponse<String> response = new ApiResponse<>();
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testParameterizedConstructor() {
        String testData = "test data";
        String testMessage = "test message";
        ApiResponse<String> response = new ApiResponse<>(true, testData, testMessage);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals(testMessage, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testSuccessStaticMethod() {
        String testData = "test data";
        ApiResponse<String> response = ApiResponse.success(testData);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals("Operation successful", response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testSuccessWithMessageStaticMethod() {
        String testData = "test data";
        String testMessage = "custom message";
        ApiResponse<String> response = ApiResponse.success(testData, testMessage);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals(testMessage, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testErrorStaticMethod() {
        String testMessage = "error message";
        ApiResponse<String> response = ApiResponse.error(testMessage);
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(testMessage, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testErrorWithCodeStaticMethod() {
        String errorCode = "ERROR_CODE";
        String testMessage = "error message";
        ApiResponse<String> response = ApiResponse.error(errorCode, testMessage);
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(testMessage, response.getMessage());
        assertEquals(errorCode, response.getErrorCode());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testSettersAndGetters() {
        ApiResponse<String> response = new ApiResponse<>();
        
        response.setSuccess(true);
        response.setData("test data");
        response.setMessage("test message");
        response.setErrorCode("ERROR_CODE");
        
        LocalDateTime timestamp = LocalDateTime.now();
        response.setTimestamp(timestamp);
        
        assertTrue(response.isSuccess());
        assertEquals("test data", response.getData());
        assertEquals("test message", response.getMessage());
        assertEquals("ERROR_CODE", response.getErrorCode());
        assertEquals(timestamp, response.getTimestamp());
    }
}
