/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.DialUserDao;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 拨测用户服务测试类
 *
 * @author Generated
 * @since 2025-09-18
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserServiceTest {

    @Mock
    private DialUserDao dialUserDao;

    @Mock
    private OperationLogUtil operationLogUtil;

    @InjectMocks
    private DialUserService dialUserService;

    @Test
    public void testFindUsersWithPagination_Success_ReturnsUserList() {
        // Arrange
        DialUser user1 = createTestUser(1, "user1", "password1");
        DialUser user2 = createTestUser(2, "user2", "password2");
        List<DialUser> expectedUsers = Arrays.asList(user1, user2);
        
        when(dialUserDao.findUsersWithPagination(0, 10, null)).thenReturn(expectedUsers);

        // Act
        List<DialUser> result = dialUserService.findUsersWithPagination(0, 10, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        verify(dialUserDao).findUsersWithPagination(0, 10, null);
    }

    @Test
    public void testFindUsersWithPagination_WithUsernameFilter_ReturnsFilteredList() {
        // Arrange
        DialUser user = createTestUser(1, "testuser", "password");
        List<DialUser> expectedUsers = Arrays.asList(user);
        
        when(dialUserDao.findUsersWithPagination(20, 5, "test")).thenReturn(expectedUsers);

        // Act
        List<DialUser> result = dialUserService.findUsersWithPagination(4, 5, "test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(dialUserDao).findUsersWithPagination(20, 5, "test");
    }

    @Test
    public void testFindUsersWithPagination_EmptyResult_ReturnsEmptyList() {
        // Arrange
        List<DialUser> expectedUsers = Arrays.asList();
        when(dialUserDao.findUsersWithPagination(0, 10, "nonexistent")).thenReturn(expectedUsers);

        // Act
        List<DialUser> result = dialUserService.findUsersWithPagination(0, 10, "nonexistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(dialUserDao).findUsersWithPagination(0, 10, "nonexistent");
    }

    @Test
    public void testCountUsers_Success_ReturnsCount() {
        // Arrange
        when(dialUserDao.countUsers(null)).thenReturn(5L);

        // Act
        long result = dialUserService.countUsers(null);

        // Assert
        assertEquals(5L, result);
        verify(dialUserDao).countUsers(null);
    }

    @Test
    public void testCountUsers_WithUsernameFilter_ReturnsFilteredCount() {
        // Arrange
        when(dialUserDao.countUsers("test")).thenReturn(2L);

        // Act
        long result = dialUserService.countUsers("test");

        // Assert
        assertEquals(2L, result);
        verify(dialUserDao).countUsers("test");
    }

    @Test
    public void testFindById_Success_ReturnsUser() {
        // Arrange
        DialUser expectedUser = createTestUser(1, "testuser", "password");
        when(dialUserDao.findById(1)).thenReturn(expectedUser);

        // Act
        DialUser result = dialUserService.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser.getId(), result.getId());
        assertEquals(expectedUser.getUsername(), result.getUsername());
        verify(dialUserDao).findById(1);
    }

    @Test
    public void testFindById_UserNotFound_ReturnsNull() {
        // Arrange
        when(dialUserDao.findById(999)).thenReturn(null);

        // Act
        DialUser result = dialUserService.findById(999);

        // Assert
        assertNull(result);
        verify(dialUserDao).findById(999);
    }

    @Test
    public void testFindByUsername_Success_ReturnsUser() {
        // Arrange
        DialUser expectedUser = createTestUser(1, "testuser", "password");
        when(dialUserDao.findByUsername("testuser")).thenReturn(expectedUser);

        // Act
        DialUser result = dialUserService.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(expectedUser.getUsername(), result.getUsername());
        verify(dialUserDao).findByUsername("testuser");
    }

    @Test
    public void testFindByUsername_UserNotFound_ReturnsNull() {
        // Arrange
        when(dialUserDao.findByUsername("nonexistent")).thenReturn(null);

        // Act
        DialUser result = dialUserService.findByUsername("nonexistent");

        // Assert
        assertNull(result);
        verify(dialUserDao).findByUsername("nonexistent");
    }

    @Test
    public void testCreateUser_Success_ReturnsCreatedUser() {
        // Arrange
        when(dialUserDao.findByUsername("newuser")).thenReturn(null);
        when(dialUserDao.create(any(DialUser.class))).thenReturn(1);

        // Act
        DialUser result = dialUserService.createUser("newuser", "password", "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("password", result.getPassword());
        assertNotNull(result.getLastLoginTime());
        verify(dialUserDao).findByUsername("newuser");
        verify(dialUserDao).create(any(DialUser.class));
        verify(operationLogUtil).logUserCreate("testuser", "newuser", "用户名:newuser, 密码:已设置");
    }

    @Test
    public void testCreateUser_UsernameExists_ThrowsException() {
        // Arrange
        DialUser existingUser = createTestUser(1, "existinguser", "password");
        when(dialUserDao.findByUsername("existinguser")).thenReturn(existingUser);

        // Act & Assert
        try {
            dialUserService.createUser("existinguser", "password", "testuser");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户名已存在: existinguser", e.getMessage());
        }
        
        verify(dialUserDao).findByUsername("existinguser");
        verify(dialUserDao, never()).create(any(DialUser.class));
    }

    @Test
    public void testCreateUser_CreateFails_ThrowsException() {
        // Arrange
        when(dialUserDao.findByUsername("newuser")).thenReturn(null);
        when(dialUserDao.create(any(DialUser.class))).thenReturn(0);

        // Act & Assert
        try {
            dialUserService.createUser("newuser", "password", "testuser");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("创建用户失败，数据库操作未生效", e.getMessage());
        }
        
        verify(dialUserDao).findByUsername("newuser");
        verify(dialUserDao).create(any(DialUser.class));
    }

    @Test
    public void testUpdateUser_Success_ReturnsUpdatedUser() {
        // Arrange
        DialUser existingUser = createTestUser(1, "olduser", "oldpassword");
        DialUser updatedUser = createTestUser(1, "newuser", "newpassword");
        
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.findByUsername("newuser")).thenReturn(null);
        when(dialUserDao.update(any(DialUser.class))).thenReturn(1);

        // Act
        DialUser result = dialUserService.updateUser(1, "newuser", "newpassword", "testuser");

        // Assert
        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        assertEquals("newpassword", result.getPassword());
        verify(dialUserDao).findById(1);
        verify(dialUserDao).findByUsername("newuser");
        verify(dialUserDao).update(any(DialUser.class));
        verify(operationLogUtil).logUserUpdate("testuser", "newuser", "用户名:olduser, 密码:已设置", "用户名:newuser, 密码:已更新");
    }

    @Test
    public void testUpdateUser_UserNotFound_ThrowsException() {
        // Arrange
        when(dialUserDao.findById(999)).thenReturn(null);

        // Act & Assert
        try {
            dialUserService.updateUser(999, "newuser", "newpassword", "testuser");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户不存在: 999", e.getMessage());
        }
        
        verify(dialUserDao).findById(999);
        verify(dialUserDao, never()).findByUsername(anyString());
        verify(dialUserDao, never()).update(any(DialUser.class));
    }

    @Test
    public void testUpdateUser_UsernameExists_ThrowsException() {
        // Arrange
        DialUser existingUser = createTestUser(1, "olduser", "oldpassword");
        DialUser conflictingUser = createTestUser(2, "existinguser", "password");
        
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.findByUsername("existinguser")).thenReturn(conflictingUser);

        // Act & Assert
        try {
            dialUserService.updateUser(1, "existinguser", "newpassword", "testuser");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户名已存在: existinguser", e.getMessage());
        }
        
        verify(dialUserDao).findById(1);
        verify(dialUserDao).findByUsername("existinguser");
        verify(dialUserDao, never()).update(any(DialUser.class));
    }

    @Test
    public void testUpdateUser_UpdateFails_ThrowsException() {
        // Arrange
        DialUser existingUser = createTestUser(1, "olduser", "oldpassword");
        
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.findByUsername("newuser")).thenReturn(null);
        when(dialUserDao.update(any(DialUser.class))).thenReturn(0);

        // Act & Assert
        try {
            dialUserService.updateUser(1, "newuser", "newpassword", "testuser");
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("更新用户失败，数据库操作未生效", e.getMessage());
        }
        
        verify(dialUserDao).findById(1);
        verify(dialUserDao).findByUsername("newuser");
        verify(dialUserDao).update(any(DialUser.class));
    }

    @Test
    public void testDeleteUser_Success_DeletesUser() {
        // Arrange
        DialUser existingUser = createTestUser(1, "testuser", "password");
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.deleteById(1)).thenReturn(1);

        // Act
        dialUserService.deleteUser(1);

        // Assert
        verify(dialUserDao).findById(1);
        verify(dialUserDao).deleteById(1);
        verify(operationLogUtil).logUserDelete("system", "testuser", "用户名:testuser, 最后登录:" + existingUser.getLastLoginTime());
    }

    @Test
    public void testDeleteUser_UserNotFound_ThrowsException() {
        // Arrange
        when(dialUserDao.findById(999)).thenReturn(null);

        // Act & Assert
        try {
            dialUserService.deleteUser(999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户不存在: 999", e.getMessage());
        }
        
        verify(dialUserDao).findById(999);
        verify(dialUserDao, never()).deleteById(anyInt());
    }

    @Test
    public void testDeleteUser_DeleteFails_ThrowsException() {
        // Arrange
        DialUser existingUser = createTestUser(1, "testuser", "password");
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.deleteById(1)).thenReturn(0);

        // Act & Assert
        try {
            dialUserService.deleteUser(1);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertEquals("删除用户失败，数据库操作未生效", e.getMessage());
        }
        
        verify(dialUserDao).findById(1);
        verify(dialUserDao).deleteById(1);
    }

    @Test
    public void testDeleteUser_WithOperator_Success_DeletesUser() {
        // Arrange
        DialUser existingUser = createTestUser(1, "testuser", "password");
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.deleteById(1)).thenReturn(1);

        // Act
        dialUserService.deleteUser(1, "admin");

        // Assert
        verify(dialUserDao).findById(1);
        verify(dialUserDao).deleteById(1);
        verify(operationLogUtil).logUserDelete("admin", "testuser", "用户名:testuser, 最后登录:" + existingUser.getLastLoginTime());
    }

    @Test
    public void testUpdateLastLoginTime_Success_UpdatesTime() {
        // Arrange
        DialUser existingUser = createTestUser(1, "testuser", "password");
        when(dialUserDao.findById(1)).thenReturn(existingUser);
        when(dialUserDao.updateLastLoginTime(eq(1), any(LocalDateTime.class))).thenReturn(1);

        // Act
        dialUserService.updateLastLoginTime(1);

        // Assert
        verify(dialUserDao).findById(1);
        verify(dialUserDao).updateLastLoginTime(eq(1), any(LocalDateTime.class));
        verify(operationLogUtil).logUserLogin("testuser");
    }

    @Test
    public void testUpdateLastLoginTime_UserNotFound_ThrowsException() {
        // Arrange
        when(dialUserDao.findById(999)).thenReturn(null);

        // Act & Assert
        try {
            dialUserService.updateLastLoginTime(999);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用户不存在: 999", e.getMessage());
        }
        
        verify(dialUserDao).findById(999);
        verify(dialUserDao, never()).updateLastLoginTime(eq(999), any(LocalDateTime.class));
        verify(operationLogUtil, never()).logUserLogin(anyString());
    }

    private DialUser createTestUser(Integer id, String username, String password) {
        DialUser user = new DialUser();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setLastLoginTime(LocalDateTime.now().toString());
        return user;
    }
}
