/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.BaseApiResponse;
import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.dto.PasswordValidationRequest;
import com.huawei.dialtest.center.dto.PasswordValidationResult;
import com.huawei.dialtest.center.dto.UpdateLoginTimeRequest;
import com.huawei.dialtest.center.dto.UserCreateRequest;
import com.huawei.dialtest.center.dto.UserUpdateRequest;
import com.huawei.dialtest.center.entity.DialUser;
import com.huawei.dialtest.center.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 用户控制器，提供用户管理的REST API接口
 * 支持用户的创建、更新、删除、查询等操作
 * 提供权限控制和数据验证功能
 *
 * @author g00940940
 * @since 2025-09-09
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param search 搜索关键字（可选）
     * @return 用户分页数据
     */
    @GetMapping
    public ResponseEntity<BaseApiResponse<PagedResponse<DialUser>>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        try {
            logger.info("Received request to get users - page: {}, size: {}, search: {}", page, pageSize, search);
            Page<DialUser> users = userService.getAllUsers(page, pageSize, search);
            
            PagedResponse<DialUser> pagedResponse = new PagedResponse<>(
                users.getContent(),
                users.getTotalElements(),
                page,
                pageSize
            );
            
            logger.info("Successfully retrieved {} users (page {}/{})", users.getContent().size(), page, users.getTotalPages());
            return ResponseEntity.ok(BaseApiResponse.success(pagedResponse));
        } catch (RuntimeException e) {
            logger.error("Failed to get users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to retrieve users"));
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseApiResponse<DialUser>> getUserById(@PathVariable Long id) {
        try {
            logger.info("Received request to get user by ID: {}", id);
            Optional<DialUser> user = userService.getUserById(id);
            if (user.isPresent()) {
                logger.info("Successfully retrieved user: {}", user.get().getUsername());
                return ResponseEntity.ok(BaseApiResponse.success(user.get()));
            } else {
                logger.warn("User not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseApiResponse.error("NOT_FOUND", "User not found"));
            }
        } catch (RuntimeException e) {
            logger.error("Failed to get user by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to retrieve user"));
        }
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<BaseApiResponse<DialUser>> getUserByUsername(@PathVariable String username) {
        try {
            logger.info("Received request to get user by username: {}", username);
            Optional<DialUser> user = userService.getUserByUsername(username);
            if (user.isPresent()) {
                logger.info("Successfully retrieved user: {}", username);
                return ResponseEntity.ok(BaseApiResponse.success(user.get()));
            } else {
                logger.warn("User not found with username: {}", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseApiResponse.error("NOT_FOUND", "User not found"));
            }
        } catch (RuntimeException e) {
            logger.error("Failed to get user by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to retrieve user"));
        }
    }

    /**
     * 创建新用户
     *
     * @param request 用户创建请求
     * @return 创建的用户
     */
    @PostMapping
    public ResponseEntity<BaseApiResponse<DialUser>> createUser(@RequestBody UserCreateRequest request) {
        try {
            logger.info("Received request to create user: {}", request.getUsername());
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.warn("Username is required");
                return ResponseEntity.badRequest()
                    .body(BaseApiResponse.error("VALIDATION_ERROR", "Username is required"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                logger.warn("Password is required");
                return ResponseEntity.badRequest()
                    .body(BaseApiResponse.error("VALIDATION_ERROR", "Password is required"));
            }

            DialUser user = userService.createUser(request);
            logger.info("Successfully created user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(BaseApiResponse.success(user, "User created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Failed to create user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to create user"));
        }
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param request 用户更新请求
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseApiResponse<DialUser>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        try {
            logger.info("Received request to update user with ID: {}", id);
            
            if (request.getUsername() != null && request.getUsername().trim().isEmpty()) {
                logger.warn("Username cannot be empty");
                return ResponseEntity.badRequest()
                    .body(BaseApiResponse.error("VALIDATION_ERROR", "Username cannot be empty"));
            }

            DialUser user = userService.updateUser(id, request);
            logger.info("Successfully updated user: {}", user.getUsername());
            return ResponseEntity.ok(BaseApiResponse.success(user, "User updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Failed to update user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to update user"));
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            logger.info("Received request to delete user with ID: {}", id);
            userService.deleteUser(id);
            logger.info("Successfully deleted user with ID: {}", id);
            return ResponseEntity.ok(BaseApiResponse.success("User deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(BaseApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to delete user"));
        }
    }

    /**
     * 根据用户名搜索用户
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    @GetMapping("/search")
    public ResponseEntity<BaseApiResponse<List<DialUser>>> searchUsers(@RequestParam String username) {
        try {
            logger.info("Received request to search users by username: {}", username);
            List<DialUser> users = userService.searchUsersByUsername(username);
            logger.info("Found {} users matching username: {}", users.size(), username);
            return ResponseEntity.ok(BaseApiResponse.success(users));
        } catch (RuntimeException e) {
            logger.error("Failed to search users by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to search users"));
        }
    }

    /**
     * 验证用户密码
     *
     * @param request 密码验证请求
     * @return 验证结果
     */
    @PostMapping("/validate-password")
    public ResponseEntity<BaseApiResponse<PasswordValidationResult>> validatePassword(@RequestBody PasswordValidationRequest request) {
        try {
            logger.info("Received request to validate password for user: {}", request.getUsername());
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.warn("Username is required");
                return ResponseEntity.badRequest()
                    .body(BaseApiResponse.error("VALIDATION_ERROR", "Username is required"));
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                logger.warn("Password is required");
                return ResponseEntity.badRequest()
                    .body(BaseApiResponse.error("VALIDATION_ERROR", "Password is required"));
            }

            PasswordValidationResult result = userService.validatePassword(request);
            
            logger.info("Password validation result for user {}: {}", request.getUsername(), result.isValid());
            return ResponseEntity.ok(BaseApiResponse.success(result));
        } catch (RuntimeException e) {
            logger.error("Failed to validate password for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to validate password"));
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param request 登录时间更新请求
     * @return 更新结果
     */
    @PostMapping("/update-login-time")
    public ResponseEntity<BaseApiResponse<String>> updateLastLoginTime(@RequestBody UpdateLoginTimeRequest request) {
        try {
            logger.info("Received request to update last login time for user: {}", request.getUsername());
            
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                logger.warn("Username is required");
                return ResponseEntity.badRequest()
                    .body(BaseApiResponse.error("VALIDATION_ERROR", "Username is required"));
            }

            userService.updateLastLoginTime(request);
            logger.info("Successfully updated last login time for user: {}", request.getUsername());
            return ResponseEntity.ok(BaseApiResponse.success("Last login time updated successfully"));
        } catch (RuntimeException e) {
            logger.error("Failed to update last login time for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseApiResponse.error("INTERNAL_ERROR", "Failed to update last login time"));
        }
    }

}
