/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.dto.UserCreateRequest;
import com.huawei.dialtest.center.dto.UserUpdateRequest;
import com.huawei.dialtest.center.entity.DialUser;
import com.huawei.dialtest.center.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserController控制器测试
 * 测试用户控制器的REST API接口，包括HTTP请求处理
 *
 * @author g00940940
 * @since 2025-09-16
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private DialUser testUser;
    private List<DialUser> testUsers;

    @Before
    public void setUp() {
        testUser = new DialUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedpassword");
        testUser.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        DialUser user2 = new DialUser();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setPassword("$2a$10$encodedpassword2");

        testUsers = Arrays.asList(testUser, user2);
    }

    @Test
    public void testGetAllUsers_Success() {
        PagedResponse<DialUser> pagedResponse = new PagedResponse<>(testUsers, 2L, 1, 10);
        when(userService.getAllUsers(1, 10, null)).thenReturn(pagedResponse);

        ResponseEntity<BaseApiResponse<PagedResponse<DialUser>>> response = userController.getAllUsers(1, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertEquals(testUsers, response.getBody().getData().getData());
        assertEquals(2L, response.getBody().getData().getTotal());
        assertEquals(1, response.getBody().getData().getPage());
        assertEquals(10, response.getBody().getData().getPageSize());
        verify(userService).getAllUsers(1, 10, null);
    }

    @Test
    public void testGetAllUsers_Error() {
        when(userService.getAllUsers(1, 10, null)).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<BaseApiResponse<PagedResponse<DialUser>>> response = userController.getAllUsers(1, 10, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("INTERNAL_ERROR", response.getBody().getErrorCode());
        assertEquals("Failed to retrieve users", response.getBody().getMessage());
        verify(userService).getAllUsers(1, 10, null);
    }

   
    @Test
    public void testCreateUser_Success() {
        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(testUser);

        UserCreateRequest request = new UserCreateRequest("testuser", "password123");

        ResponseEntity<BaseApiResponse<DialUser>> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUser, response.getBody().getData());
        assertEquals("User created successfully", response.getBody().getMessage());
        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    public void testCreateUser_ValidationError() {
        UserCreateRequest request = new UserCreateRequest("", "password123");

        ResponseEntity<BaseApiResponse<DialUser>> response = userController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        assertEquals("Username is required", response.getBody().getMessage());
    }

    @Test
    public void testUpdateUser_Success() {
        when(userService.updateUser(anyLong(), any(UserUpdateRequest.class))).thenReturn(testUser);

        UserUpdateRequest request = new UserUpdateRequest("updateduser", "newpassword");

        ResponseEntity<BaseApiResponse<DialUser>> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(testUser, response.getBody().getData());
        assertEquals("User updated successfully", response.getBody().getMessage());
        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    public void testDeleteUser_Success() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<BaseApiResponse<String>> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("User deleted successfully", response.getBody().getData());
        verify(userService).deleteUser(1L);
    }


}