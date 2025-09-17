/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;


import com.huawei.dialtest.center.dto.UserCreateRequest;
import com.huawei.dialtest.center.dto.UserUpdateRequest;
import com.huawei.dialtest.center.entity.DialUser;
import com.huawei.dialtest.center.mapper.UserMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private UserService userService;

    private UserCreateRequest userCreateRequest;
    private UserUpdateRequest userUpdateRequest;

    private DialUser testUser;

    @Before
    public void setUp() {
        userCreateRequest = new UserCreateRequest("testuser", "testpass");
        userUpdateRequest = new UserUpdateRequest("newuser", "newpass");
      
        testUser = new DialUser("testuser", "encodedpassword");
        testUser.setId(1L);
    }

    @Test
    public void testCreateUser_Success() {
        when(userMapper.existsByUsername("testuser")).thenReturn(false);
        when(userMapper.insert(any(DialUser.class))).thenReturn(1);

        DialUser result = userService.createUser(userCreateRequest);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userMapper).existsByUsername("testuser");
        verify(userMapper).insert(any(DialUser.class));
        verify(operationLogService).logOperation(eq("testuser"), eq("CREATE"), eq("USER"), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUser_UsernameExists() {
        when(userMapper.existsByUsername("testuser")).thenReturn(true);

        userService.createUser(userCreateRequest);

        verify(userMapper).existsByUsername("testuser");
        verify(userMapper, never()).insert(any(DialUser.class));
    }

    @Test
    public void testUpdateUser_Success() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.existsByUsername("newuser")).thenReturn(false);
        when(userMapper.update(any(DialUser.class))).thenReturn(1);

        DialUser result = userService.updateUser(1L, userUpdateRequest);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userMapper).findById(1L);
        verify(userMapper).existsByUsername("newuser");
        verify(userMapper).update(any(DialUser.class));
        verify(operationLogService).logOperation(eq("newuser"), eq("UPDATE"), eq("USER"), anyString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUser_UserNotFound() {
        when(userMapper.findById(1L)).thenReturn(null);

        userService.updateUser(1L, userUpdateRequest);

        verify(userMapper).findById(1L);
        verify(userMapper, never()).update(any(DialUser.class));
    }

   
}