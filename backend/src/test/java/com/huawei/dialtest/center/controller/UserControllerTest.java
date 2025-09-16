/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.entity.User;
import com.huawei.dialtest.center.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserController控制器测试
 * 测试用户控制器的REST API接口，包括HTTP请求处理
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private List<User> testUsers;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$encodedpassword");
        testUser.setLastLoginTime(LocalDateTime.now());
        testUser.setCreatedTime(LocalDateTime.now());
        testUser.setUpdatedTime(LocalDateTime.now());

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setPassword("$2a$10$encodedpassword2");
        user2.setCreatedTime(LocalDateTime.now());

        testUsers = Arrays.asList(testUser, user2);
    }

    @Test
    public void testGetAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(testUsers, pageable, 2);
        when(userService.getAllUsers(1, 10, null)).thenReturn(userPage);

        ResponseEntity<?> response = userController.getAllUsers(1, 10, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertTrue(result.containsKey("data"));
        assertTrue(result.containsKey("total"));
        assertTrue(result.containsKey("page"));
        assertTrue(result.containsKey("pageSize"));
        assertTrue(result.containsKey("totalPages"));
        verify(userService).getAllUsers(1, 10, null);
    }

    @Test
    public void testGetAllUsers_Error() {
        when(userService.getAllUsers(1, 10, null)).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getAllUsers(1, 10, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("INTERNAL_ERROR", errorResponse.get("error"));
        verify(userService).getAllUsers(1, 10, null);
    }

    @Test
    public void testGetUserById_Success() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof User);
        User result = (User) response.getBody();
        assertEquals("testuser", result.getUsername());
        verify(userService).getUserById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById(999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserById(999L);
    }

    @Test
    public void testGetUserById_Error() {
        when(userService.getUserById(1L)).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getUserById(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).getUserById(1L);
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        ResponseEntity<?> response = userController.getUserByUsername("testuser");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof User);
        User result = (User) response.getBody();
        assertEquals("testuser", result.getUsername());
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserByUsername("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userService).getUserByUsername("nonexistent");
    }

    @Test
    public void testGetUserByUsername_Error() {
        when(userService.getUserByUsername("testuser")).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.getUserByUsername("testuser");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).getUserByUsername("testuser");
    }

    @Test
    public void testCreateUser_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "password123");

        when(userService.createUser("newuser", "password123")).thenReturn(testUser);

        ResponseEntity<?> response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof User);
        User result = (User) response.getBody();
        assertEquals("testuser", result.getUsername());
        verify(userService).createUser("newuser", "password123");
    }

    @Test
    public void testCreateUser_MissingUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("password", "password123");

        ResponseEntity<?> response = userController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", errorResponse.get("error"));
        verify(userService, never()).createUser(anyString(), anyString());
    }

    @Test
    public void testCreateUser_MissingPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");

        ResponseEntity<?> response = userController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", errorResponse.get("error"));
        verify(userService, never()).createUser(anyString(), anyString());
    }

    @Test
    public void testCreateUser_ValidationError() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "existinguser");
        request.put("password", "password123");

        when(userService.createUser("existinguser", "password123"))
            .thenThrow(new IllegalArgumentException("Username already exists"));

        ResponseEntity<?> response = userController.createUser(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> errorResponse = (Map<String, Object>) response.getBody();
        assertEquals("VALIDATION_ERROR", errorResponse.get("error"));
        verify(userService).createUser("existinguser", "password123");
    }

    @Test
    public void testCreateUser_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "password123");

        when(userService.createUser("newuser", "password123"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.createUser(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).createUser("newuser", "password123");
    }

    @Test
    public void testUpdateUser_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "updateduser");
        request.put("password", "newpassword");

        when(userService.updateUser(1L, "updateduser", "newpassword")).thenReturn(testUser);

        ResponseEntity<?> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof User);
        User result = (User) response.getBody();
        assertEquals("testuser", result.getUsername());
        verify(userService).updateUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testUpdateUser_EmptyUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "");
        request.put("password", "newpassword");

        ResponseEntity<?> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).updateUser(anyLong(), anyString(), anyString());
    }

    @Test
    public void testUpdateUser_ValidationError() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "existinguser");
        request.put("password", "newpassword");

        when(userService.updateUser(1L, "existinguser", "newpassword"))
            .thenThrow(new IllegalArgumentException("Username already exists"));

        ResponseEntity<?> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).updateUser(1L, "existinguser", "newpassword");
    }

    @Test
    public void testUpdateUser_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "updateduser");
        request.put("password", "newpassword");

        when(userService.updateUser(1L, "updateduser", "newpassword"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.updateUser(1L, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).updateUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testDeleteUser_Success() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<?> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> successResponse = (Map<String, Object>) response.getBody();
        assertEquals(true, successResponse.get("success"));
        verify(userService).deleteUser(1L);
    }

    @Test
    public void testDeleteUser_ValidationError() {
        doThrow(new IllegalArgumentException("User not found"))
            .when(userService).deleteUser(999L);

        ResponseEntity<?> response = userController.deleteUser(999L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService).deleteUser(999L);
    }

    @Test
    public void testDeleteUser_Error() {
        doThrow(new RuntimeException("Service error"))
            .when(userService).deleteUser(1L);

        ResponseEntity<?> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).deleteUser(1L);
    }

    @Test
    public void testSearchUsers_Success() {
        when(userService.searchUsersByUsername("test")).thenReturn(testUsers);

        ResponseEntity<?> response = userController.searchUsers("test");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof List);
        @SuppressWarnings("unchecked")
        List<User> result = (List<User>) response.getBody();
        assertEquals(2, result.size());
        verify(userService).searchUsersByUsername("test");
    }

    @Test
    public void testSearchUsers_Error() {
        when(userService.searchUsersByUsername("test"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.searchUsers("test");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).searchUsersByUsername("test");
    }

    @Test
    public void testValidatePassword_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "password123");

        when(userService.validatePassword("testuser", "password123")).thenReturn(true);

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals(true, result.get("valid"));
        verify(userService).validatePassword("testuser", "password123");
    }

    @Test
    public void testValidatePassword_InvalidPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "wrongpassword");

        when(userService.validatePassword("testuser", "wrongpassword")).thenReturn(false);

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getBody();
        assertEquals(false, result.get("valid"));
        verify(userService).validatePassword("testuser", "wrongpassword");
    }

    @Test
    public void testValidatePassword_MissingUsername() {
        Map<String, String> request = new HashMap<>();
        request.put("password", "password123");

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).validatePassword(anyString(), anyString());
    }

    @Test
    public void testValidatePassword_MissingPassword() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).validatePassword(anyString(), anyString());
    }

    @Test
    public void testValidatePassword_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("password", "password123");

        when(userService.validatePassword("testuser", "password123"))
            .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<?> response = userController.validatePassword(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).validatePassword("testuser", "password123");
    }

    @Test
    public void testUpdateLastLoginTime_Success() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        doNothing().when(userService).updateLastLoginTime("testuser");

        ResponseEntity<?> response = userController.updateLastLoginTime(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> successResponse = (Map<String, Object>) response.getBody();
        assertEquals(true, successResponse.get("success"));
        verify(userService).updateLastLoginTime("testuser");
    }

    @Test
    public void testUpdateLastLoginTime_MissingUsername() {
        Map<String, String> request = new HashMap<>();

        ResponseEntity<?> response = userController.updateLastLoginTime(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(userService, never()).updateLastLoginTime(anyString());
    }

    @Test
    public void testUpdateLastLoginTime_Error() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "testuser");

        doThrow(new RuntimeException("Service error"))
            .when(userService).updateLastLoginTime("testuser");

        ResponseEntity<?> response = userController.updateLastLoginTime(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userService).updateLastLoginTime("testuser");
    }
}
