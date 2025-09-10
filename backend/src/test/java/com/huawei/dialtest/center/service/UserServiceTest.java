/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.User;
import com.huawei.dialtest.center.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataAccessException;

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
    private UserRepository userRepository;

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
        when(userRepository.findAllOrderByCreatedTimeDesc()).thenReturn(testUsers);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("testuser2", result.get(1).getUsername());
        verify(userRepository).findAllOrderByCreatedTimeDesc();
    }

    @Test(expected = RuntimeException.class)
    public void testGetAllUsers_Error() {
        when(userRepository.findAllOrderByCreatedTimeDesc()).thenThrow(new DataAccessException("Database error") {});

        userService.getAllUsers();
    }

    @Test
    public void testGetUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(999L);

        assertFalse(result.isPresent());
        verify(userRepository).findById(999L);
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserById_Error() {
        when(userRepository.findById(1L)).thenThrow(new DataAccessException("Database error") {});

        userService.getUserById(1L);
    }

    @Test
    public void testGetUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserByUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserByUsername_Error() {
        when(userRepository.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.getUserByUsername("testuser");
    }

    @Test
    public void testCreateUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser("newuser", "password123");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).save(any(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUser_UsernameExists() {
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        userService.createUser("existinguser", "password123");
    }

    @Test(expected = RuntimeException.class)
    public void testCreateUser_Error() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("Database error") {});

        userService.createUser("newuser", "password123");
    }

    @Test
    public void testUpdateUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("updateduser")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, "updateduser", "newpassword");

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("updateduser");
        verify(userRepository).save(any(User.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUser_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        userService.updateUser(999L, "updateduser", "newpassword");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateUser_UsernameExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        userService.updateUser(1L, "existinguser", "newpassword");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateUser_Error() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenThrow(new DataAccessException("Database error") {});

        userService.updateUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testDeleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteUser_UserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        userService.deleteUser(999L);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteUser_Error() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doThrow(new DataAccessException("Database error") {}).when(userRepository).deleteById(1L);

        userService.deleteUser(1L);
    }

    @Test
    public void testUpdateLastLoginTime_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateLastLoginTime("testuser");

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testUpdateLastLoginTime_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        userService.updateLastLoginTime("nonexistent");

        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateLastLoginTime_Error() {
        when(userRepository.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

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
        
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userWithEncodedPassword));

        boolean result = userService.validatePassword("testuser", "admin123");

        assertTrue(result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testValidatePassword_InvalidPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = userService.validatePassword("testuser", "wrongpassword");

        assertFalse(result);
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testValidatePassword_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        boolean result = userService.validatePassword("nonexistent", "password123");

        assertFalse(result);
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test(expected = RuntimeException.class)
    public void testValidatePassword_Error() {
        when(userRepository.findByUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.validatePassword("testuser", "password123");
    }

    @Test
    public void testSearchUsersByUsername_Success() {
        when(userRepository.findByUsernameContaining("test")).thenReturn(testUsers);

        List<User> result = userService.searchUsersByUsername("test");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findByUsernameContaining("test");
    }

    @Test(expected = RuntimeException.class)
    public void testSearchUsersByUsername_Error() {
        when(userRepository.findByUsernameContaining("test")).thenThrow(new DataAccessException("Database error") {});

        userService.searchUsersByUsername("test");
    }
}
