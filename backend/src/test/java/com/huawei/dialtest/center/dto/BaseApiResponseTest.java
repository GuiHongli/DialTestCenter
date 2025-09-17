/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * BaseApiResponse测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class BaseApiResponseTest {

    @Test
    public void testDefaultConstructor() {
        BaseApiResponse<String> response = new BaseApiResponse<>();
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testParameterizedConstructor() {
        String testData = "test data";
        String testMessage = "test message";
        BaseApiResponse<String> response = new BaseApiResponse<>(true, testData, testMessage);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals(testMessage, response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testSuccessStaticMethod() {
        String testData = "test data";
        BaseApiResponse<String> response = BaseApiResponse.success(testData);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals("Operation successful", response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testSuccessWithMessageStaticMethod() {
        String testData = "test data";
        String testMessage = "custom message";
        BaseApiResponse<String> response = BaseApiResponse.success(testData, testMessage);
        
        assertTrue(response.isSuccess());
        assertEquals(testData, response.getData());
        assertEquals(testMessage, response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testErrorStaticMethod() {
        String testMessage = "error message";
        BaseApiResponse<String> response = BaseApiResponse.error(testMessage);
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(testMessage, response.getMessage());
        assertNull(response.getErrorCode());
    }

    @Test
    public void testErrorWithCodeStaticMethod() {
        String errorCode = "ERROR_CODE";
        String testMessage = "error message";
        BaseApiResponse<String> response = BaseApiResponse.error(errorCode, testMessage);
        
        assertFalse(response.isSuccess());
        assertNull(response.getData());
        assertEquals(testMessage, response.getMessage());
        assertEquals(errorCode, response.getErrorCode());
    }

    @Test
    public void testSettersAndGetters() {
        BaseApiResponse<String> response = new BaseApiResponse<>();
        
        response.setSuccess(true);
        response.setData("test data");
        response.setMessage("test message");
        response.setErrorCode("ERROR_CODE");
        
        assertTrue(response.isSuccess());
        assertEquals("test data", response.getData());
        assertEquals("test message", response.getMessage());
        assertEquals("ERROR_CODE", response.getErrorCode());
    }
}