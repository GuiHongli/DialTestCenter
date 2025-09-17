/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * PasswordValidationResult测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class PasswordValidationResultTest {

    @Test
    public void testDefaultConstructor() {
        PasswordValidationResult result = new PasswordValidationResult();
        assertFalse(result.isValid());
        assertNull(result.getMessage());
    }

    @Test
    public void testParameterizedConstructor() {
        boolean valid = true;
        String message = "Password is valid";
        PasswordValidationResult result = new PasswordValidationResult(valid, message);
        
        assertEquals(valid, result.isValid());
        assertEquals(message, result.getMessage());
    }

    @Test
    public void testSettersAndGetters() {
        PasswordValidationResult result = new PasswordValidationResult();
        boolean valid = false;
        String message = "Password is invalid";
        
        result.setValid(valid);
        result.setMessage(message);
        
        assertEquals(valid, result.isValid());
        assertEquals(message, result.getMessage());
    }

    @Test
    public void testSetValid() {
        PasswordValidationResult result = new PasswordValidationResult();
        boolean valid = true;
        
        result.setValid(valid);
        
        assertEquals(valid, result.isValid());
    }

    @Test
    public void testSetMessage() {
        PasswordValidationResult result = new PasswordValidationResult();
        String message = "Test message";
        
        result.setMessage(message);
        
        assertEquals(message, result.getMessage());
    }
}
