/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.User;
import com.huawei.dialtest.center.mapper.UserMapper;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserService服务类测试
 * 测试用户服务的业务逻辑，包括增删改查操作
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

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
        when(userMapper.findAllByOrderByCreatedTimeDesc(0, 10)).thenReturn(testUsers);
        when(userMapper.count()).thenReturn(2L);

        Page<User> result = userService.getAllUsers(1, 10, null);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        assertEquals("testuser2", result.getContent().get(1).getUsername());
        verify(userMapper).findAllByOrderByCreatedTimeDesc(0, 10);
        verify(userMapper).count();
    }

    @Test(expected = RuntimeException.class)
    public void testGetAllUsers_Error() {
        when(userMapper.findAllByOrderByCreatedTimeDesc(0, 10)).thenThrow(new DataAccessException("Database error") {});

        userService.getAllUsers(1, 10, null);
    }

    @Test
    public void testGetAllUsers_WithSearch() {
        List<User> searchResults = Arrays.asList(testUser);
        when(userMapper.findByUsernameContainingWithPage("test", 0, 10)).thenReturn(searchResults);
        when(userMapper.countByUsernameContaining("test")).thenReturn(1L);

        Page<User> result = userService.getAllUsers(1, 10, "test");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
        verify(userMapper).findByUsernameContainingWithPage("test", 0, 10);
        verify(userMapper).countByUsernameContaining("test");
    }

    @Test
    public void testGetUserById_Success() {
        when(userMapper.findById(1L)).thenReturn(testUser);

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userMapper).findById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        Optional<User> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
        verify(userMapper).findById(999L);
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserById_Error() {
        when(userMapper.findById(1L)).thenThrow(new DataAccessException("Database error") {});

        userService.getUserById(1L);
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        Optional<User> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userMapper).findByUsername("testuser");
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        Optional<User> result = userService.getUserByUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(userMapper).findByUsername("nonexistent");
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserByUsername_Error() {
        when(userMapper.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.getUserByUsername("testuser");
    }

    @Test
    public void testCreateUser_Success() {
        when(userMapper.existsByUsername("newuser")).thenReturn(false);
        when(userMapper.insert(any(User.class))).thenReturn(1);

        User result = userService.createUser("newuser", "password123");

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userMapper).existsByUsername("newuser");
        verify(userMapper).insert(any(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUser_UsernameExists() {
        when(userMapper.existsByUsername("existinguser")).thenReturn(true);

        userService.createUser("existinguser", "password123");
    }

    @Test(expected = RuntimeException.class)
    public void testCreateUser_Error() {
        when(userMapper.existsByUsername("newuser")).thenReturn(false);
        when(userMapper.insert(any(User.class))).thenThrow(new DataAccessException("Database error") {});

        userService.createUser("newuser", "password123");
    }

    @Test
    public void testUpdateUser_Success() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.existsByUsername("updateduser")).thenReturn(false);
        when(userMapper.update(any(User.class))).thenReturn(1);

        User result = userService.updateUser(1L, "updateduser", "newpassword");

        assertNotNull(result);
        verify(userMapper).findById(1L);
        verify(userMapper).existsByUsername("updateduser");
        verify(userMapper).update(any(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUser_UserNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        userService.updateUser(999L, "updateduser", "newpassword");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUser_UsernameExists() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.existsByUsername("existinguser")).thenReturn(true);

        userService.updateUser(1L, "existinguser", "newpassword");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateUser_Error() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.update(any(User.class))).thenThrow(new DataAccessException("Database error") {});

        userService.updateUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testDeleteUser_Success() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        userService.deleteUser(1L);

        verify(userMapper).findById(1L);
        verify(userMapper).deleteById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteUser_UserNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        userService.deleteUser(999L);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteUser_Error() {
        when(userMapper.findById(1L)).thenReturn(testUser);
        doThrow(new DataAccessException("Database error") {}).when(userMapper).deleteById(1L);

        userService.deleteUser(1L);
    }

    @Test
    public void testUpdateLastLoginTime_Success() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);
        when(userMapper.update(any(User.class))).thenReturn(1);

        userService.updateLastLoginTime("testuser");

        verify(userMapper).findByUsername("testuser");
        verify(userMapper).update(any(User.class));
    }

    @Test
    public void testUpdateLastLoginTime_UserNotFound() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        userService.updateLastLoginTime("nonexistent");

        verify(userMapper).findByUsername("nonexistent");
        verify(userMapper, never()).update(any(User.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateLastLoginTime_Error() {
        when(userMapper.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.updateLastLoginTime("testuser");
    }

    @Test
    public void testValidatePassword_Success() {
        // Create a user with properly encoded password
        User userWithEncodedPassword = new User();
        userWithEncodedPassword.setId(1L);
        userWithEncodedPassword.setUsername("testuser");
        // Use a known BCrypt hash for "admin123"
        userWithEncodedPassword.setPassword("$2a$10$kl97OCzpiKVHp16ttRJa3OcNFVTnmznxsQDgpLg0Tm5bRW7DIsXEm");
        
        when(userMapper.findByUsername("testuser")).thenReturn(userWithEncodedPassword);

        boolean result = userService.validatePassword("testuser", "admin123");

        assertTrue(result);
        verify(userMapper).findByUsername("testuser");
    }

    @Test
    public void testValidatePassword_InvalidPassword() {
        when(userMapper.findByUsername("testuser")).thenReturn(testUser);

        boolean result = userService.validatePassword("testuser", "wrongpassword");

        assertFalse(result);
        verify(userMapper).findByUsername("testuser");
    }

    @Test
    public void testValidatePassword_UserNotFound() {
        when(userMapper.findByUsername("nonexistent")).thenReturn(null);

        boolean result = userService.validatePassword("nonexistent", "password123");

        assertFalse(result);
        verify(userMapper).findByUsername("nonexistent");
    }

    @Test(expected = RuntimeException.class)
    public void testValidatePassword_Error() {
        when(userMapper.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.validatePassword("testuser", "password123");
    }

    @Test
    public void testSearchUsersByUsername_Success() {
        when(userMapper.findByUsernameContaining("test")).thenReturn(testUsers);

        List<User> result = userService.searchUsersByUsername("test");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userMapper).findByUsernameContaining("test");
    }

    @Test(expected = RuntimeException.class)
    public void testSearchUsersByUsername_Error() {
        when(userMapper.findByUsernameContaining("test")).thenThrow(new DataAccessException("Database error") {});

        userService.searchUsersByUsername("test");
    }
}
