/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.UserRole;
import com.huawei.dialtest.center.service.UserRoleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

/**
 * 用户角色控制器，提供用户角色管理的REST API接口
 * 支持用户角色的创建、更新、删除、查询等操作
 * 提供权限检查和执行机用户统计功能
 *
 * @author g00940940
 * @since 2025-09-06
 */
@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {
    private static final Logger logger = LoggerFactory.getLogger(UserRoleController.class);

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 获取用户角色列表（分页）
     *
     * @param page 页码（从1开始，默认1）
     * @param pageSize 每页大小（默认10）
     * @param search 搜索关键词（可选）
     * @return 分页的用户角色列表
     */
    @GetMapping
    public ResponseEntity<?> getUserRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        try {
            Page<UserRole> userRolePage = userRoleService.getAllUserRoles(page, pageSize, search);
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", userRolePage.getContent());
            response.put("total", userRolePage.getTotalElements());
            response.put("page", page);
            response.put("pageSize", pageSize);
            response.put("totalPages", userRolePage.getTotalPages());
            
            logger.debug("Successfully retrieved user roles: page={}, pageSize={}, total={}", 
                        page, pageSize, userRolePage.getTotalElements());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database access failed while getting user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalStateException e) {
            logger.error("Service state error while getting user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 创建用户角色
     *
     * @param request 用户角色请求，不能为空
     * @return 创建的用户角色
     * @throws IllegalArgumentException 当请求参数无效时抛出
     */
    @PostMapping
    public ResponseEntity<UserRole> createUserRole(@Valid @RequestBody UserRoleRequest request) {
        logger.info("Creating user role: {} - {}", request.getUsername(), request.getRole());
        try {
            UserRole userRole = new UserRole();
            userRole.setUsername(request.getUsername());
            userRole.setRole(request.getRole());
            UserRole savedUserRole = userRoleService.save(userRole);
            logger.info("User role created successfully: {} - {}", request.getUsername(), request.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUserRole);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database access failed while creating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalStateException e) {
            logger.error("Service state error while creating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 更新用户角色
     *
     * @param id 用户角色ID，不能为空
     * @param request 用户角色请求，不能为空
     * @return 更新后的用户角色
     * @throws RuntimeException 当用户角色不存在时抛出
     * @throws IllegalArgumentException 当请求参数无效时抛出
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserRole> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request) {
        logger.info("Updating user role with ID: {}", id);
        try {
            UserRole userRole = userRoleService.findById(id)
                .orElseThrow(() -> new RuntimeException("User role does not exist"));
            userRole.setUsername(request.getUsername());
            userRole.setRole(request.getRole());
            UserRole updatedUserRole = userRoleService.save(userRole);
            logger.info("User role updated successfully: {} - {}", request.getUsername(), request.getRole());
            return ResponseEntity.ok(updatedUserRole);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database access failed while updating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalStateException e) {
            logger.error("Service state error while updating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            logger.warn("User role not found for update: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 删除用户角色
     *
     * @param id 用户角色ID，不能为空
     * @return 删除结果
     * @throws IllegalArgumentException 当用户角色不存在时抛出
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable Long id) {
        logger.info("Deleting user role with ID: {}", id);
        try {
            userRoleService.deleteById(id);
            logger.info("User role deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.warn("User role not found for deletion: {}", id);
            return ResponseEntity.notFound().build();
        } catch (DataAccessException e) {
            logger.error("Database access failed while deleting user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalStateException e) {
            logger.error("Service state error while deleting user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 获取执行机用户数量
     *
     * @return 执行机用户数量
     */
    @GetMapping("/executor-count")
    public ResponseEntity<Long> getExecutorCount() {
        logger.debug("Getting executor user count");
        try {
            long count = userRoleService.getExecutorUserCount();
            logger.debug("Executor user count: {}", count);
            return ResponseEntity.ok(count);
        } catch (DataAccessException e) {
            logger.error("Database access failed while getting executor user count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalStateException e) {
            logger.error("Service state error while getting executor user count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 用户角色请求对象，用于接收用户角色创建和更新的请求数据
     * 包含用户名和角色信息
     */
    public static class UserRoleRequest {
        private String username;
        private Role role;

        /**
         * 获取用户名
         *
         * @return 用户名
         */
        public String getUsername() {
            return username;
        }

        /**
         * 设置用户名
         *
         * @param username 用户名，不能为空
         */
        public void setUsername(String username) {
            this.username = username;
        }

        /**
         * 获取角色
         *
         * @return 角色枚举
         */
        public Role getRole() {
            return role;
        }

        /**
         * 设置角色
         *
         * @param role 角色枚举，不能为空
         */
        public void setRole(Role role) {
            this.role = role;
        }
    }
}
