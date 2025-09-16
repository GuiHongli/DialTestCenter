/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import org.junit.Test;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * ErrorResponse测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class ErrorResponseTest {

    @Test
    public void testDefaultConstructor() {
        ErrorResponse response = new ErrorResponse();
        
        assertFalse(response.isSuccess());
        assertNull(response.getError());
        assertNull(response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testParameterizedConstructor() {
        String errorCode = "ERROR_CODE";
        String errorMessage = "Error message";
        ErrorResponse response = new ErrorResponse(errorCode, errorMessage);
        
        assertFalse(response.isSuccess());
        assertEquals(errorCode, response.getError());
        assertEquals(errorMessage, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testOfStaticMethod() {
        String errorCode = "ERROR_CODE";
        String errorMessage = "Error message";
        ErrorResponse response = ErrorResponse.of(errorCode, errorMessage);
        
        assertFalse(response.isSuccess());
        assertEquals(errorCode, response.getError());
        assertEquals(errorMessage, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    public void testSettersAndGetters() {
        ErrorResponse response = new ErrorResponse();
        
        response.setSuccess(false);
        response.setError("ERROR_CODE");
        response.setMessage("Error message");
        
        LocalDateTime timestamp = LocalDateTime.now();
        response.setTimestamp(timestamp);
        
        assertFalse(response.isSuccess());
        assertEquals("ERROR_CODE", response.getError());
        assertEquals("Error message", response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
    }
}
