/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.CreateDialUserRequest;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponse;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponseData;
import com.huawei.cloududn.dialingtest.model.DialUserResponse;
import com.huawei.cloududn.dialingtest.model.UpdateDialUserRequest;
import com.huawei.cloududn.dialingtest.service.DialUserService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DialUserController单元测试
 *
 * @author g00940940
 * @since 2025-09-18
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserControllerTest {

    @Mock
    private DialUserService dialUserService;

    @InjectMocks
    private DialUserController dialUserController;

    private DialUser testDialUser;
    private CreateDialUserRequest createRequest;
    private UpdateDialUserRequest updateRequest;
    private DialUserResponse successResponse;
    private DialUserPageResponse pageResponse;

    @Before
    public void setUp() {
        testDialUser = new DialUser();
        testDialUser.setId(1);
        testDialUser.setUsername("testuser");
        testDialUser.setPassword("encrypted_password");
        testDialUser.setLastLoginTime(LocalDateTime.now());

        createRequest = new CreateDialUserRequest();
        createRequest.setUsername("testuser");
        createRequest.setPassword("password123");

        updateRequest = new UpdateDialUserRequest();
        updateRequest.setUsername("updateduser");
        updateRequest.setPassword("newpassword123");

        successResponse = new DialUserResponse();
        successResponse.setSuccess(true);
        successResponse.setData(testDialUser);
        successResponse.setMessage("操作成功");

        pageResponse = new DialUserPageResponse();
        pageResponse.setSuccess(true);
        pageResponse.setMessage("查询成功");
        DialUserPageResponseData pageData = new DialUserPageResponseData();
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        pageData.setContent(users);
        pageData.setTotalElements(1);
        pageData.setTotalPages(1);
        pageData.setNumber(0);
        pageData.setSize(10);
        pageResponse.setData(pageData);
    }

    @Test
    public void testGetDialUsers_Success_ReturnsOkResponse() {
        // Arrange
        when(dialUserService.getDialUsers(anyString(), anyInt(), anyInt())).thenReturn(pageResponse);

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 10, "test");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(dialUserService).getDialUsers("test", 0, 10);
    }

    @Test
    public void testGetDialUsers_InvalidPage_ReturnsBadRequest() {
        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(-1, 10, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("页码不能小于0", response.getBody().getMessage());
        verify(dialUserService, never()).getDialUsers(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetDialUsers_InvalidSize_ReturnsBadRequest() {
        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 0, null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("每页大小必须在1-100之间", response.getBody().getMessage());
        verify(dialUserService, never()).getDialUsers(anyString(), anyInt(), anyInt());
    }

    @Test
    public void testGetDialUsers_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(dialUserService.getDialUsers(anyString(), anyInt(), anyInt())).thenThrow(new DataAccessException("Database error") {});

        // Act
        ResponseEntity<DialUserPageResponse> response = dialUserController.getDialUsers(0, 10, null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("数据库操作失败", response.getBody().getMessage());
        verify(dialUserService).getDialUsers(null, 0, 10);
    }

    @Test
    public void testCreateDialUser_Success_ReturnsCreatedResponse() {
        // Arrange
        when(dialUserService.createDialUser(any(CreateDialUserRequest.class))).thenReturn(successResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser(createRequest);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(dialUserService).createDialUser(createRequest);
    }

    @Test
    public void testCreateDialUser_UsernameExists_ReturnsConflictResponse() {
        // Arrange
        DialUserResponse conflictResponse = new DialUserResponse();
        conflictResponse.setSuccess(false);
        conflictResponse.setMessage("用户名已存在");
        when(dialUserService.createDialUser(any(CreateDialUserRequest.class))).thenReturn(conflictResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser(createRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名已存在", response.getBody().getMessage());
        verify(dialUserService).createDialUser(createRequest);
    }

    @Test
    public void testCreateDialUser_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(dialUserService.createDialUser(any(CreateDialUserRequest.class))).thenThrow(new DataAccessException("Database error") {});

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.createDialUser(createRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("数据库操作失败", response.getBody().getMessage());
        verify(dialUserService).createDialUser(createRequest);
    }

    @Test
    public void testGetDialUserById_Success_ReturnsOkResponse() {
        // Arrange
        when(dialUserService.getDialUserById(anyInt())).thenReturn(successResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.getDialUserById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(dialUserService).getDialUserById(1);
    }

    @Test
    public void testGetDialUserById_UserNotFound_ReturnsNotFoundResponse() {
        // Arrange
        DialUserResponse notFoundResponse = new DialUserResponse();
        notFoundResponse.setSuccess(false);
        notFoundResponse.setMessage("用户不存在");
        when(dialUserService.getDialUserById(anyInt())).thenReturn(notFoundResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.getDialUserById(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dialUserService).getDialUserById(999);
    }

    @Test
    public void testGetDialUserById_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(dialUserService.getDialUserById(anyInt())).thenThrow(new DataAccessException("Database error") {});

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.getDialUserById(1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("数据库操作失败", response.getBody().getMessage());
        verify(dialUserService).getDialUserById(1);
    }

    @Test
    public void testUpdateDialUser_Success_ReturnsOkResponse() {
        // Arrange
        when(dialUserService.updateDialUser(anyInt(), any(UpdateDialUserRequest.class))).thenReturn(successResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(1, updateRequest);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(dialUserService).updateDialUser(1, updateRequest);
    }

    @Test
    public void testUpdateDialUser_UserNotFound_ReturnsNotFoundResponse() {
        // Arrange
        DialUserResponse notFoundResponse = new DialUserResponse();
        notFoundResponse.setSuccess(false);
        notFoundResponse.setMessage("用户不存在");
        when(dialUserService.updateDialUser(anyInt(), any(UpdateDialUserRequest.class))).thenReturn(notFoundResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(999, updateRequest);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dialUserService).updateDialUser(999, updateRequest);
    }

    @Test
    public void testUpdateDialUser_UsernameExists_ReturnsConflictResponse() {
        // Arrange
        DialUserResponse conflictResponse = new DialUserResponse();
        conflictResponse.setSuccess(false);
        conflictResponse.setMessage("用户名已存在");
        when(dialUserService.updateDialUser(anyInt(), any(UpdateDialUserRequest.class))).thenReturn(conflictResponse);

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(1, updateRequest);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("用户名已存在", response.getBody().getMessage());
        verify(dialUserService).updateDialUser(1, updateRequest);
    }

    @Test
    public void testUpdateDialUser_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(dialUserService.updateDialUser(anyInt(), any(UpdateDialUserRequest.class))).thenThrow(new DataAccessException("Database error") {});

        // Act
        ResponseEntity<DialUserResponse> response = dialUserController.updateDialUser(1, updateRequest);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("数据库操作失败", response.getBody().getMessage());
        verify(dialUserService).updateDialUser(1, updateRequest);
    }

    @Test
    public void testDeleteDialUser_Success_ReturnsNoContentResponse() {
        // Arrange
        when(dialUserService.deleteDialUser(anyInt())).thenReturn(true);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(1);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(dialUserService).deleteDialUser(1);
    }

    @Test
    public void testDeleteDialUser_UserNotFound_ReturnsNotFoundResponse() {
        // Arrange
        when(dialUserService.deleteDialUser(anyInt())).thenReturn(false);

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(dialUserService).deleteDialUser(999);
    }

    @Test
    public void testDeleteDialUser_ServiceException_ReturnsInternalServerError() {
        // Arrange
        when(dialUserService.deleteDialUser(anyInt())).thenThrow(new DataAccessException("Database error") {});

        // Act
        ResponseEntity<Void> response = dialUserController.deleteDialUser(1);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(dialUserService).deleteDialUser(1);
    }
}
