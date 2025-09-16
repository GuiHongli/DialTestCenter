/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.DialDialUser;
import com.huawei.dialtest.center.mapper.DialUserMapper;
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
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DialUserService服务类测试
 * 测试用户服务的业务逻辑，包括增删改查操作
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserServiceTest {
    @Mock
    private DialUserMapper userMapper;

    @InjectMocks
    private DialUserService userService;

    private DialUser testDialUser;
    private List<DialUser> testDialUsers;

    @Before
    public void setUp() {
        testDialUser = new DialUser();
        testDialUser.setId(1L);
        testDialUser.setDialUsername("testuser");
        testDialUser.setPassword("$2a$10$encodedpassword");
        testDialUser.setLastLoginTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        DialUser user2 = new DialUser();
        user2.setId(2L);
        user2.setDialUsername("testuser2");
        user2.setPassword("$2a$10$encodedpassword2");

        testDialUsers = Arrays.asList(testDialUser, user2);
    }

    @Test
    public void testGetAllDialUsers_Success() {
        when(userMapper.findAllByOrderByCreatedTimeDesc(0, 10)).thenReturn(testDialUsers);
        when(userMapper.count()).thenReturn(2L);

        Page<DialUser> result = userService.getAllDialUsers(1, 10, null);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getDialUsername());
        assertEquals("testuser2", result.getContent().get(1).getDialUsername());
        verify(userMapper).findAllByOrderByCreatedTimeDesc(0, 10);
        verify(userMapper).count();
    }

    @Test(expected = RuntimeException.class)
    public void testGetAllDialUsers_Error() {
        when(userMapper.findAllByOrderByCreatedTimeDesc(0, 10)).thenThrow(new DataAccessException("Database error") {});

        userService.getAllDialUsers(1, 10, null);
    }

    @Test
    public void testGetAllDialUsers_WithSearch() {
        List<DialUser> searchResults = Arrays.asList(testDialUser);
        when(userMapper.findByDialUsernameContainingWithPage("test", 0, 10)).thenReturn(searchResults);
        when(userMapper.countByDialUsernameContaining("test")).thenReturn(1L);

        Page<DialUser> result = userService.getAllDialUsers(1, 10, "test");

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getDialUsername());
        verify(userMapper).findByDialUsernameContainingWithPage("test", 0, 10);
        verify(userMapper).countByDialUsernameContaining("test");
    }

    @Test
    public void testGetDialUserById_Success() {
        when(userMapper.findById(1L)).thenReturn(testDialUser);

        Optional<DialUser> result = userService.getDialUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getDialUsername());
        verify(userMapper).findById(1L);
    }

    @Test
    public void testGetDialUserById_NotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        Optional<DialUser> result = userService.getDialUserById(999L);

        assertFalse(result.isPresent());
        verify(userMapper).findById(999L);
    }

    @Test(expected = RuntimeException.class)
    public void testGetDialUserById_Error() {
        when(userMapper.findById(1L)).thenThrow(new DataAccessException("Database error") {});

        userService.getDialUserById(1L);
    }

    @Test
    public void testGetDialUserByDialUsername_Success() {
        when(userMapper.findByDialUsername("testuser")).thenReturn(testDialUser);

        Optional<DialUser> result = userService.getDialUserByDialUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getDialUsername());
        verify(userMapper).findByDialUsername("testuser");
    }

    @Test
    public void testGetDialUserByDialUsername_NotFound() {
        when(userMapper.findByDialUsername("nonexistent")).thenReturn(null);

        Optional<DialUser> result = userService.getDialUserByDialUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(userMapper).findByDialUsername("nonexistent");
    }

    @Test(expected = RuntimeException.class)
    public void testGetDialUserByDialUsername_Error() {
        when(userMapper.findByDialUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.getDialUserByDialUsername("testuser");
    }

    @Test
    public void testCreateDialUser_Success() {
        when(userMapper.existsByDialUsername("newuser")).thenReturn(false);
        when(userMapper.insert(any(DialUser.class))).thenReturn(1);

        DialUser result = userService.createDialUser("newuser", "password123");

        assertNotNull(result);
        assertEquals("newuser", result.getDialUsername());
        verify(userMapper).existsByDialUsername("newuser");
        verify(userMapper).insert(any(DialUser.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateDialUser_DialUsernameExists() {
        when(userMapper.existsByDialUsername("existinguser")).thenReturn(true);

        userService.createDialUser("existinguser", "password123");
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDialUser_Error() {
        when(userMapper.existsByDialUsername("newuser")).thenReturn(false);
        when(userMapper.insert(any(DialUser.class))).thenThrow(new DataAccessException("Database error") {});

        userService.createDialUser("newuser", "password123");
    }

    @Test
    public void testUpdateDialUser_Success() {
        when(userMapper.findById(1L)).thenReturn(testDialUser);
        when(userMapper.existsByDialUsername("updateduser")).thenReturn(false);
        when(userMapper.update(any(DialUser.class))).thenReturn(1);

        DialUser result = userService.updateDialUser(1L, "updateduser", "newpassword");

        assertNotNull(result);
        verify(userMapper).findById(1L);
        verify(userMapper).existsByDialUsername("updateduser");
        verify(userMapper).update(any(DialUser.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDialUser_DialUserNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        userService.updateDialUser(999L, "updateduser", "newpassword");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateDialUser_DialUsernameExists() {
        when(userMapper.findById(1L)).thenReturn(testDialUser);
        when(userMapper.existsByDialUsername("existinguser")).thenReturn(true);

        userService.updateDialUser(1L, "existinguser", "newpassword");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateDialUser_Error() {
        when(userMapper.findById(1L)).thenReturn(testDialUser);
        when(userMapper.update(any(DialUser.class))).thenThrow(new DataAccessException("Database error") {});

        userService.updateDialUser(1L, "updateduser", "newpassword");
    }

    @Test
    public void testDeleteDialUser_Success() {
        when(userMapper.findById(1L)).thenReturn(testDialUser);
        when(userMapper.deleteById(1L)).thenReturn(1);

        userService.deleteDialUser(1L);

        verify(userMapper).findById(1L);
        verify(userMapper).deleteById(1L);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteDialUser_DialUserNotFound() {
        when(userMapper.findById(999L)).thenReturn(null);

        userService.deleteDialUser(999L);
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteDialUser_Error() {
        when(userMapper.findById(1L)).thenReturn(testDialUser);
        doThrow(new DataAccessException("Database error") {}).when(userMapper).deleteById(1L);

        userService.deleteDialUser(1L);
    }

    @Test
    public void testUpdateLastLoginTime_Success() {
        when(userMapper.findByDialUsername("testuser")).thenReturn(testDialUser);
        when(userMapper.update(any(DialUser.class))).thenReturn(1);

        userService.updateLastLoginTime("testuser");

        verify(userMapper).findByDialUsername("testuser");
        verify(userMapper).update(any(DialUser.class));
    }

    @Test
    public void testUpdateLastLoginTime_DialUserNotFound() {
        when(userMapper.findByDialUsername("nonexistent")).thenReturn(null);

        userService.updateLastLoginTime("nonexistent");

        verify(userMapper).findByDialUsername("nonexistent");
        verify(userMapper, never()).update(any(DialUser.class));
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateLastLoginTime_Error() {
        when(userMapper.findByDialUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.updateLastLoginTime("testuser");
    }

    @Test
    public void testValidatePassword_Success() {
        // Create a user with properly encoded password
        DialUser userWithEncodedPassword = new DialUser();
        userWithEncodedPassword.setId(1L);
        userWithEncodedPassword.setDialUsername("testuser");
        // Use a known BCrypt hash for "admin123"
        userWithEncodedPassword.setPassword("$2a$10$kl97OCzpiKVHp16ttRJa3OcNFVTnmznxsQDgpLg0Tm5bRW7DIsXEm");
        
        when(userMapper.findByDialUsername("testuser")).thenReturn(userWithEncodedPassword);

        boolean result = userService.validatePassword("testuser", "admin123");

        assertTrue(result);
        verify(userMapper).findByDialUsername("testuser");
    }

    @Test
    public void testValidatePassword_InvalidPassword() {
        when(userMapper.findByDialUsername("testuser")).thenReturn(testDialUser);

        boolean result = userService.validatePassword("testuser", "wrongpassword");

        assertFalse(result);
        verify(userMapper).findByDialUsername("testuser");
    }

    @Test
    public void testValidatePassword_DialUserNotFound() {
        when(userMapper.findByDialUsername("nonexistent")).thenReturn(null);

        boolean result = userService.validatePassword("nonexistent", "password123");

        assertFalse(result);
        verify(userMapper).findByDialUsername("nonexistent");
    }

    @Test(expected = RuntimeException.class)
    public void testValidatePassword_Error() {
        when(userMapper.findByDialUsername("testuser")).thenThrow(new DataAccessException("Database error") {});

        userService.validatePassword("testuser", "password123");
    }

    @Test
    public void testSearchDialUsersByDialUsername_Success() {
        when(userMapper.findByDialUsernameContaining("test")).thenReturn(testDialUsers);

        List<DialUser> result = userService.searchDialUsersByDialUsername("test");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userMapper).findByDialUsernameContaining("test");
    }

    @Test(expected = RuntimeException.class)
    public void testSearchDialUsersByDialUsername_Error() {
        when(userMapper.findByDialUsernameContaining("test")).thenThrow(new DataAccessException("Database error") {});

        userService.searchDialUsersByDialUsername("test");
    }
}
