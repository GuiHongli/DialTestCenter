package com.dialtest.center.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import com.dialtest.center.entity.User;

/**
 * 用户控制器测试类
 * 
 * @author DialTestCenter
 * @version 1.0.0
 */
public class UserControllerTest {
    
    private UserController userController;
    
    private User testUser;
    
    @Before
    public void setUp() {
        userController = new UserController();
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
    }
    
    @Test
    public void testGetAllUsers() {
        ResponseEntity<List<User>> response = userController.getAllUsers();
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }
    
    @Test
    public void testGetUserById() {
        ResponseEntity<User> response = userController.getUserById(1L);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNull(response.getBody());
    }
    
    @Test
    public void testCreateUser() {
        ResponseEntity<User> response = userController.createUser(testUser);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(testUser.getUsername(), response.getBody().getUsername());
        assertEquals(testUser.getEmail(), response.getBody().getEmail());
    }
    
    @Test
    public void testUpdateUser() {
        ResponseEntity<User> response = userController.updateUser(1L, testUser);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals(Long.valueOf(1L), response.getBody().getId());
    }
    
    @Test
    public void testDeleteUser() {
        ResponseEntity<Void> response = userController.deleteUser(1L);
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
    }
    
    @Test
    public void testHealth() {
        ResponseEntity<String> response = userController.health();
        
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("User service is running!", response.getBody());
    }
}
