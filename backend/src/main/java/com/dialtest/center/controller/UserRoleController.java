package com.dialtest.center.controller;

import java.util.List;

import javax.persistence.EntityNotFoundException;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dialtest.center.entity.Role;
import com.dialtest.center.entity.UserRole;
import com.dialtest.center.service.UserRoleService;

/**
 * 用户角色管理控制器
 */
@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);
    
    @Autowired
    private UserRoleService userRoleService;
    
    /**
     * 获取用户角色列表
     * @param username 用户名（可选，不传则返回所有）
     * @return 用户角色列表
     */
    @GetMapping
    public ResponseEntity<List<UserRole>> getUserRoles(
            @RequestParam(required = false) String username) {
        
        try {
            List<UserRole> userRoles;
            if (username != null && !username.trim().isEmpty()) {
                userRoles = userRoleService.getUserRoles(username.trim());
            } else {
                userRoles = userRoleService.getAllUserRoles();
            }
            
            logger.debug("获取用户角色列表成功: {}", userRoles.size());
            return ResponseEntity.ok(userRoles);
            
        } catch (Exception e) {
            logger.error("获取用户角色列表失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 创建用户角色
     * @param request 用户角色请求
     * @return 创建的用户角色
     */
    @PostMapping
    public ResponseEntity<UserRole> createUserRole(@Valid @RequestBody UserRoleRequest request) {
        
        try {
            UserRole userRole = new UserRole();
            userRole.setUsername(request.getUsername());
            userRole.setRole(request.getRole());
            
            UserRole savedUserRole = userRoleService.save(userRole);
            
            logger.info("创建用户角色成功: {} - {}", request.getUsername(), request.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUserRole);
            
        } catch (Exception e) {
            logger.error("创建用户角色失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 更新用户角色
     * @param id 用户角色ID
     * @param request 用户角色请求
     * @return 更新后的用户角色
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserRole> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request) {
        
        try {
            UserRole userRole = userRoleService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("用户角色不存在"));
            
            userRole.setUsername(request.getUsername());
            userRole.setRole(request.getRole());
            
            UserRole updatedUserRole = userRoleService.save(userRole);
            
            logger.info("更新用户角色成功: {} - {}", request.getUsername(), request.getRole());
            return ResponseEntity.ok(updatedUserRole);
            
        } catch (EntityNotFoundException e) {
            logger.warn("更新用户角色失败，用户角色不存在: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("更新用户角色失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除用户角色
     * @param id 用户角色ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable Long id) {
        
        try {
            userRoleService.deleteById(id);
            
            logger.info("删除用户角色成功: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            logger.error("删除用户角色失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 获取执行机用户数量
     * @return 执行机用户数量
     */
    @GetMapping("/executor-count")
    public ResponseEntity<Long> getExecutorCount() {
        try {
            long count = userRoleService.getExecutorUserCount();
            logger.debug("获取执行机用户数量: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            logger.error("获取执行机用户数量失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 用户角色请求对象
     */
    public static class UserRoleRequest {
        private String username;
        private Role role;
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public Role getRole() {
            return role;
        }
        
        public void setRole(Role role) {
            this.role = role;
        }
    }
}