/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.Role;
import com.huawei.dialtest.center.entity.DialUserRole;
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
    public ResponseEntity<BaseApiResponse<PagedResponse<DialUserRole>>> getUserRoles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        try {
            Page<DialUserRole> userRolePage = userRoleService.getAllUserRoles(page, pageSize, search);
            
            PagedResponse<DialUserRole> pagedResponse = new PagedResponse<>(
                userRolePage.getContent(),
                userRolePage.getTotalElements(),
                page,
                pageSize
            );
            
            logger.debug("Successfully retrieved user roles: page={}, pageSize={}, total={}", 
                        page, pageSize, userRolePage.getTotalElements());
            return ResponseEntity.ok(BaseApiResponse.success(pagedResponse));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database access failed while getting user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("DATABASE_ERROR", "Database access failed"));
        } catch (IllegalStateException e) {
            logger.error("Service state error while getting user roles: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("SERVICE_ERROR", "Service state error"));
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
    public ResponseEntity<BaseApiResponse<DialUserRole>> createUserRole(@Valid @RequestBody UserRoleRequest request) {
        logger.info("Creating user role: {} - {}", request.getUsername(), request.getRole());
        try {
            DialUserRole userRole = new DialUserRole();
            userRole.setUsername(request.getUsername());
            userRole.setRole(request.getRole());
            DialUserRole savedUserRole = userRoleService.save(userRole);
            logger.info("User role created successfully: {} - {}", request.getUsername(), request.getRole());
            return ResponseEntity.status(HttpStatus.CREATED).body(BaseApiResponse.success(savedUserRole, "User role created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database access failed while creating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("DATABASE_ERROR", "Database access failed"));
        } catch (IllegalStateException e) {
            logger.error("Service state error while creating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("SERVICE_ERROR", "Service state error"));
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
    public ResponseEntity<BaseApiResponse<DialUserRole>> updateUserRole(
            @PathVariable Long id,
            @Valid @RequestBody UserRoleRequest request) {
        logger.info("Updating user role with ID: {}", id);
        try {
            DialUserRole userRole = userRoleService.findById(id)
                .orElseThrow(() -> new RuntimeException("User role does not exist"));
            userRole.setUsername(request.getUsername());
            userRole.setRole(request.getRole());
            DialUserRole updatedUserRole = userRoleService.save(userRole);
            logger.info("User role updated successfully: {} - {}", request.getUsername(), request.getRole());
            return ResponseEntity.ok(BaseApiResponse.success(updatedUserRole, "User role updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (DataAccessException e) {
            logger.error("Database access failed while updating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("DATABASE_ERROR", "Database access failed"));
        } catch (IllegalStateException e) {
            logger.error("Service state error while updating user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("SERVICE_ERROR", "Service state error"));
        } catch (RuntimeException e) {
            logger.warn("User role not found for update: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseApiResponse.error("NOT_FOUND", "User role not found"));
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
    public ResponseEntity<BaseApiResponse<String>> deleteUserRole(@PathVariable Long id) {
        logger.info("Deleting user role with ID: {}", id);
        try {
            userRoleService.deleteById(id);
            logger.info("User role deleted successfully: {}", id);
            return ResponseEntity.ok(BaseApiResponse.success("User role deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("User role not found for deletion: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(BaseApiResponse.error("NOT_FOUND", "User role not found"));
        } catch (DataAccessException e) {
            logger.error("Database access failed while deleting user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("DATABASE_ERROR", "Database access failed"));
        } catch (IllegalStateException e) {
            logger.error("Service state error while deleting user role: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("SERVICE_ERROR", "Service state error"));
        }
    }

    /**
     * 获取执行机用户数量
     *
     * @return 执行机用户数量
     */
    @GetMapping("/executor-count")
    public ResponseEntity<BaseApiResponse<Long>> getExecutorCount() {
        logger.debug("Getting executor user count");
        try {
            long count = userRoleService.getExecutorUserCount();
            logger.debug("Executor user count: {}", count);
            return ResponseEntity.ok(BaseApiResponse.success(count));
        } catch (DataAccessException e) {
            logger.error("Database access failed while getting executor user count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("DATABASE_ERROR", "Database access failed"));
        } catch (IllegalStateException e) {
            logger.error("Service state error while getting executor user count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("SERVICE_ERROR", "Service state error"));
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
