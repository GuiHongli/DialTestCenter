/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * UserCreateRequest测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class UserCreateRequestTest {

    @Test
    public void testDefaultConstructor() {
        UserCreateRequest request = new UserCreateRequest();
        assertNull(request.getUsername());
        assertNull(request.getPassword());
    }

    @Test
    public void testParameterizedConstructor() {
        String username = "testuser";
        String password = "testpass";
        UserCreateRequest request = new UserCreateRequest(username, password);
        
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    public void testSettersAndGetters() {
        UserCreateRequest request = new UserCreateRequest();
        String username = "testuser";
        String password = "testpass";
        
        request.setUsername(username);
        request.setPassword(password);
        
        assertEquals(username, request.getUsername());
        assertEquals(password, request.getPassword());
    }

    @Test
    public void testSetUsername() {
        UserCreateRequest request = new UserCreateRequest();
        String username = "newuser";
        
        request.setUsername(username);
        
        assertEquals(username, request.getUsername());
    }

    @Test
    public void testSetPassword() {
        UserCreateRequest request = new UserCreateRequest();
        String password = "newpass";
        
        request.setPassword(password);
        
        assertEquals(password, request.getPassword());
    }
}
