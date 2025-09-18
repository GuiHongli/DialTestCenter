/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.DialUserDao;
import com.huawei.cloududn.dialingtest.model.CreateDialUserRequest;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponse;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponseData;
import com.huawei.cloududn.dialingtest.model.DialUserResponse;
import com.huawei.cloududn.dialingtest.model.UpdateDialUserRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DialUserService单元测试
 *
 * @author g00940940
 * @since 2025-09-18
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserServiceTest {

    @Mock
    private DialUserDao dialUserDao;

    @InjectMocks
    private DialUserService dialUserService;

    private DialUser testDialUser;
    private CreateDialUserRequest createRequest;
    private UpdateDialUserRequest updateRequest;

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
    }

    @Test
    public void testCreateDialUser_Success_ReturnsSuccessResponse() {
        // Arrange
        when(dialUserDao.existsByUsername("testuser", null)).thenReturn(false);
        when(dialUserDao.insert(any(DialUser.class))).thenReturn(1);

        // Act
        DialUserResponse response = dialUserService.createDialUser(createRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("用户创建成功", response.getMessage());
        assertNotNull(response.getData());
        verify(dialUserDao).existsByUsername("testuser", null);
        verify(dialUserDao).insert(any(DialUser.class));
    }

    @Test
    public void testCreateDialUser_UsernameExists_ReturnsConflictResponse() {
        // Arrange
        when(dialUserDao.existsByUsername("testuser", null)).thenReturn(true);

        // Act
        DialUserResponse response = dialUserService.createDialUser(createRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("用户名已存在", response.getMessage());
        verify(dialUserDao).existsByUsername("testuser", null);
        verify(dialUserDao, never()).insert(any(DialUser.class));
    }

    @Test
    public void testCreateDialUser_DatabaseError_ReturnsErrorResponse() {
        // Arrange
        when(dialUserDao.existsByUsername("testuser", null)).thenReturn(false);
        when(dialUserDao.insert(any(DialUser.class))).thenThrow(new DataAccessException("Database error") {});

        // Act
        DialUserResponse response = dialUserService.createDialUser(createRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("数据库操作失败", response.getMessage());
        verify(dialUserDao).existsByUsername("testuser", null);
        verify(dialUserDao).insert(any(DialUser.class));
    }

    @Test
    public void testGetDialUserById_UserExists_ReturnsSuccessResponse() {
        // Arrange
        when(dialUserDao.findById(1)).thenReturn(testDialUser);

        // Act
        DialUserResponse response = dialUserService.getDialUserById(1);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("查询成功", response.getMessage());
        assertEquals(testDialUser, response.getData());
        verify(dialUserDao).findById(1);
    }

    @Test
    public void testGetDialUserById_UserNotExists_ReturnsNotFoundResponse() {
        // Arrange
        when(dialUserDao.findById(999)).thenReturn(null);

        // Act
        DialUserResponse response = dialUserService.getDialUserById(999);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("用户不存在", response.getMessage());
        verify(dialUserDao).findById(999);
    }

    @Test
    public void testGetDialUserById_DatabaseError_ReturnsErrorResponse() {
        // Arrange
        when(dialUserDao.findById(1)).thenThrow(new DataAccessException("Database error") {});

        // Act
        DialUserResponse response = dialUserService.getDialUserById(1);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("数据库操作失败", response.getMessage());
        verify(dialUserDao).findById(1);
    }

    @Test
    public void testGetDialUsers_Success_ReturnsPageResponse() {
        // Arrange
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        when(dialUserDao.findByPage(null, 0, 10)).thenReturn(users);
        when(dialUserDao.count(null)).thenReturn(1L);

        // Act
        DialUserPageResponse response = dialUserService.getDialUsers(null, 0, 10);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("查询成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getContent().size());
        assertEquals(Integer.valueOf(1), response.getData().getTotalElements());
        verify(dialUserDao).findByPage(null, 0, 10);
        verify(dialUserDao).count(null);
    }

    @Test
    public void testGetDialUsers_WithUsernameFilter_ReturnsFilteredResponse() {
        // Arrange
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        when(dialUserDao.findByPage("test", 0, 10)).thenReturn(users);
        when(dialUserDao.count("test")).thenReturn(1L);

        // Act
        DialUserPageResponse response = dialUserService.getDialUsers("test", 0, 10);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("查询成功", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getContent().size());
        verify(dialUserDao).findByPage("test", 0, 10);
        verify(dialUserDao).count("test");
    }

    @Test
    public void testGetDialUsers_DatabaseError_ReturnsErrorResponse() {
        // Arrange
        when(dialUserDao.findByPage(null, 0, 10)).thenThrow(new DataAccessException("Database error") {});

        // Act
        DialUserPageResponse response = dialUserService.getDialUsers(null, 0, 10);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("数据库操作失败", response.getMessage());
        verify(dialUserDao).findByPage(null, 0, 10);
    }

    @Test
    public void testUpdateDialUser_Success_ReturnsSuccessResponse() {
        // Arrange
        when(dialUserDao.findById(1)).thenReturn(testDialUser);
        when(dialUserDao.existsByUsername("updateduser", 1)).thenReturn(false);
        when(dialUserDao.update(any(DialUser.class))).thenReturn(1);

        // Act
        DialUserResponse response = dialUserService.updateDialUser(1, updateRequest);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("更新成功", response.getMessage());
        assertNotNull(response.getData());
        verify(dialUserDao).findById(1);
        verify(dialUserDao).existsByUsername("updateduser", 1);
        verify(dialUserDao).update(any(DialUser.class));
    }

    @Test
    public void testUpdateDialUser_UserNotExists_ReturnsNotFoundResponse() {
        // Arrange
        when(dialUserDao.findById(999)).thenReturn(null);

        // Act
        DialUserResponse response = dialUserService.updateDialUser(999, updateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("用户不存在", response.getMessage());
        verify(dialUserDao).findById(999);
        verify(dialUserDao, never()).existsByUsername(anyString(), anyInt());
        verify(dialUserDao, never()).update(any(DialUser.class));
    }

    @Test
    public void testUpdateDialUser_UsernameExists_ReturnsConflictResponse() {
        // Arrange
        when(dialUserDao.findById(1)).thenReturn(testDialUser);
        when(dialUserDao.existsByUsername("updateduser", 1)).thenReturn(true);

        // Act
        DialUserResponse response = dialUserService.updateDialUser(1, updateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("用户名已存在", response.getMessage());
        verify(dialUserDao).findById(1);
        verify(dialUserDao).existsByUsername("updateduser", 1);
        verify(dialUserDao, never()).update(any(DialUser.class));
    }

    @Test
    public void testUpdateDialUser_DatabaseError_ReturnsErrorResponse() {
        // Arrange
        when(dialUserDao.findById(1)).thenReturn(testDialUser);
        when(dialUserDao.existsByUsername("updateduser", 1)).thenReturn(false);
        when(dialUserDao.update(any(DialUser.class))).thenThrow(new DataAccessException("Database error") {});

        // Act
        DialUserResponse response = dialUserService.updateDialUser(1, updateRequest);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("数据库操作失败", response.getMessage());
        verify(dialUserDao).findById(1);
        verify(dialUserDao).existsByUsername("updateduser", 1);
        verify(dialUserDao).update(any(DialUser.class));
    }

    @Test
    public void testDeleteDialUser_Success_ReturnsTrue() {
        // Arrange
        when(dialUserDao.deleteById(1)).thenReturn(1);

        // Act
        boolean result = dialUserService.deleteDialUser(1);

        // Assert
        assertTrue(result);
        verify(dialUserDao).deleteById(1);
    }

    @Test
    public void testDeleteDialUser_UserNotExists_ReturnsFalse() {
        // Arrange
        when(dialUserDao.deleteById(999)).thenReturn(0);

        // Act
        boolean result = dialUserService.deleteDialUser(999);

        // Assert
        assertFalse(result);
        verify(dialUserDao).deleteById(999);
    }

    @Test
    public void testDeleteDialUser_DatabaseError_ReturnsFalse() {
        // Arrange
        when(dialUserDao.deleteById(1)).thenThrow(new DataAccessException("Database error") {});

        // Act
        boolean result = dialUserService.deleteDialUser(1);

        // Assert
        assertFalse(result);
        verify(dialUserDao).deleteById(1);
    }

    @Test
    public void testUpdateLastLoginTime_Success_UpdatesTime() {
        // Arrange
        when(dialUserDao.findByUsername("testuser")).thenReturn(testDialUser);
        when(dialUserDao.update(any(DialUser.class))).thenReturn(1);

        // Act
        dialUserService.updateLastLoginTime("testuser");

        // Assert
        verify(dialUserDao).findByUsername("testuser");
        verify(dialUserDao).update(any(DialUser.class));
    }

    @Test
    public void testUpdateLastLoginTime_UserNotExists_DoesNothing() {
        // Arrange
        when(dialUserDao.findByUsername("nonexistent")).thenReturn(null);

        // Act
        dialUserService.updateLastLoginTime("nonexistent");

        // Assert
        verify(dialUserDao).findByUsername("nonexistent");
        verify(dialUserDao, never()).update(any(DialUser.class));
    }

    @Test
    public void testUpdateLastLoginTime_DatabaseError_HandlesGracefully() {
        // Arrange
        when(dialUserDao.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        // Act
        dialUserService.updateLastLoginTime("testuser");

        // Assert
        verify(dialUserDao).findByUsername("testuser");
        verify(dialUserDao, never()).update(any(DialUser.class));
    }
}
