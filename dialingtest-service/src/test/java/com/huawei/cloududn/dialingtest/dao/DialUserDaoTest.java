/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.dao;

import com.huawei.cloududn.dialingtest.model.DialUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DialUserDao单元测试
 *
 * @author g00940940
 * @since 2025-09-18
 */
@RunWith(MockitoJUnitRunner.class)
public class DialUserDaoTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DialUserDao dialUserDao;

    private DialUser testDialUser;

    @Before
    public void setUp() {
        testDialUser = new DialUser();
        testDialUser.setId(1);
        testDialUser.setUsername("testuser");
        testDialUser.setPassword("encrypted_password");
        testDialUser.setLastLoginTime(LocalDateTime.now());
    }

    @Test
    public void testInsert_Success_ReturnsUserId() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenReturn(1);
        when(jdbcTemplate.queryForObject(eq("SELECT LAST_INSERT_ID()"), eq(Integer.class))).thenReturn(1);

        // Act
        Integer result = dialUserDao.insert(testDialUser);

        // Assert
        assertEquals(Integer.valueOf(1), result);
        verify(jdbcTemplate).update(anyString(), eq("testuser"), eq("encrypted_password"), any());
        verify(jdbcTemplate).queryForObject(eq("SELECT LAST_INSERT_ID()"), eq(Integer.class));
    }

    @Test
    public void testFindById_UserExists_ReturnsDialUser() {
        // Arrange
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(1))).thenReturn(users);

        // Act
        DialUser result = dialUserDao.findById(1);

        // Assert
        assertNotNull(result);
        assertEquals(testDialUser.getId(), result.getId());
        assertEquals(testDialUser.getUsername(), result.getUsername());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq(1));
    }

    @Test
    public void testFindById_UserNotExists_ReturnsNull() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(999))).thenReturn(new ArrayList<>());

        // Act
        DialUser result = dialUserDao.findById(999);

        // Assert
        assertNull(result);
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq(999));
    }

    @Test
    public void testFindByUsername_UserExists_ReturnsDialUser() {
        // Arrange
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("testuser"))).thenReturn(users);

        // Act
        DialUser result = dialUserDao.findByUsername("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testDialUser.getUsername(), result.getUsername());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("testuser"));
    }

    @Test
    public void testFindByUsername_UserNotExists_ReturnsNull() {
        // Arrange
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("nonexistent"))).thenReturn(new ArrayList<>());

        // Act
        DialUser result = dialUserDao.findByUsername("nonexistent");

        // Assert
        assertNull(result);
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("nonexistent"));
    }

    @Test
    public void testFindByPage_WithUsernameFilter_ReturnsFilteredUsers() {
        // Arrange
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq("%test%"), eq(0), eq(10))).thenReturn(users);

        // Act
        List<DialUser> result = dialUserDao.findByPage("test", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testDialUser.getUsername(), result.get(0).getUsername());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq("%test%"), eq(0), eq(10));
    }

    @Test
    public void testFindByPage_WithoutUsernameFilter_ReturnsAllUsers() {
        // Arrange
        List<DialUser> users = new ArrayList<>();
        users.add(testDialUser);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(0), eq(10))).thenReturn(users);

        // Act
        List<DialUser> result = dialUserDao.findByPage(null, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(jdbcTemplate).query(anyString(), any(RowMapper.class), eq(0), eq(10));
    }

    @Test
    public void testCount_WithUsernameFilter_ReturnsFilteredCount() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("%test%"))).thenReturn(5L);

        // Act
        Long result = dialUserDao.count("test");

        // Assert
        assertEquals(Long.valueOf(5), result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq("%test%"));
    }

    @Test
    public void testCount_WithoutUsernameFilter_ReturnsTotalCount() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class))).thenReturn(10L);

        // Act
        Long result = dialUserDao.count(null);

        // Assert
        assertEquals(Long.valueOf(10), result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class));
    }

    @Test
    public void testUpdate_Success_ReturnsUpdatedRows() {
        // Arrange
        when(jdbcTemplate.update(anyString(), any(), any(), any(), any())).thenReturn(1);

        // Act
        int result = dialUserDao.update(testDialUser);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), eq("testuser"), eq("encrypted_password"), any(), eq(1));
    }

    @Test
    public void testDeleteById_Success_ReturnsDeletedRows() {
        // Arrange
        when(jdbcTemplate.update(anyString(), eq(1))).thenReturn(1);

        // Act
        int result = dialUserDao.deleteById(1);

        // Assert
        assertEquals(1, result);
        verify(jdbcTemplate).update(anyString(), eq(1));
    }

    @Test
    public void testExistsByUsername_WithExcludeId_UserExists_ReturnsTrue() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("testuser"), eq(2))).thenReturn(1L);

        // Act
        boolean result = dialUserDao.existsByUsername("testuser", 2);

        // Assert
        assertTrue(result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq("testuser"), eq(2));
    }

    @Test
    public void testExistsByUsername_WithExcludeId_UserNotExists_ReturnsFalse() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("testuser"), eq(2))).thenReturn(0L);

        // Act
        boolean result = dialUserDao.existsByUsername("testuser", 2);

        // Assert
        assertFalse(result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq("testuser"), eq(2));
    }

    @Test
    public void testExistsByUsername_WithoutExcludeId_UserExists_ReturnsTrue() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("testuser"))).thenReturn(1L);

        // Act
        boolean result = dialUserDao.existsByUsername("testuser", null);

        // Assert
        assertTrue(result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq("testuser"));
    }

    @Test
    public void testExistsByUsername_WithoutExcludeId_UserNotExists_ReturnsFalse() {
        // Arrange
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("testuser"))).thenReturn(0L);

        // Act
        boolean result = dialUserDao.existsByUsername("testuser", null);

        // Assert
        assertFalse(result);
        verify(jdbcTemplate).queryForObject(anyString(), eq(Long.class), eq("testuser"));
    }
}
