/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.controller;

import com.huawei.dialtest.center.dto.PagedResponse;
import com.huawei.dialtest.center.entity.DialDialUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class DialUserController {
    private static final Logger logger = LoggerFactory.getLogger(DialUserController.class);

    @Autowired
    private DialUserService userService;

    /**
     * 获取用户列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param search 搜索关键字（可选）
     * @return 用户分页数据
     */
    @GetMapping
    public ResponseEntity<?> getAllDialUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String search) {
        try {
            logger.info("Received request to get users - page: {}, size: {}, search: {}", page, pageSize, search);
            PagedResponse<DialUser> users = userService.getAllDialUsers(page, pageSize, search);
            
            logger.info("Successfully retrieved {} users (page {}/{})", users.getData().size(), page, users.getTotalPages());
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            logger.error("Failed to get users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to retrieve users"));
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDialUserById(@PathVariable Long id) {
        try {
            logger.info("Received request to get user by ID: {}", id);
            Optional<DialUser> user = userService.getDialUserById(id);
            if (user.isPresent()) {
                logger.info("Successfully retrieved user: {}", user.get().getDialUsername());
                return ResponseEntity.ok(user.get());
            } else {
                logger.warn("DialUser not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Failed to get user by ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to retrieve user"));
        }
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getDialUserByDialUsername(@PathVariable String username) {
        try {
            logger.info("Received request to get user by username: {}", username);
            Optional<DialUser> user = userService.getDialUserByDialUsername(username);
            if (user.isPresent()) {
                logger.info("Successfully retrieved user: {}", username);
                return ResponseEntity.ok(user.get());
            } else {
                logger.warn("DialUser not found with username: {}", username);
                return ResponseEntity.notFound().build();
            }
        } catch (RuntimeException e) {
            logger.error("Failed to get user by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to retrieve user"));
        }
    }

    /**
     * 创建新用户
     *
     * @param request 用户创建请求
     * @return 创建的用户
     */
    @PostMapping
    public ResponseEntity<?> createDialUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            logger.info("Received request to create user: {}", username);
            
            if (username == null || username.trim().isEmpty()) {
                logger.warn("DialUsername is required");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", "DialUsername is required"));
            }
            
            if (password == null || password.trim().isEmpty()) {
                logger.warn("Password is required");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", "Password is required"));
            }

            DialUser user = userService.createDialUser(username.trim(), password);
            logger.info("Successfully created user: {}", username);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Failed to create user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to create user"));
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
    public ResponseEntity<?> updateDialUser(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            logger.info("Received request to update user with ID: {}", id);
            
            if (username != null && username.trim().isEmpty()) {
                logger.warn("DialUsername cannot be empty");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", "DialUsername cannot be empty"));
            }

            DialUser user = userService.updateDialUser(id, username, password);
            logger.info("Successfully updated user: {}", user.getDialUsername());
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Failed to update user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to update user"));
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDialUser(@PathVariable Long id) {
        try {
            logger.info("Received request to delete user with ID: {}", id);
            userService.deleteDialUser(id);
            logger.info("Successfully deleted user with ID: {}", id);
            return ResponseEntity.ok(createSuccessResponse("DialUser deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to delete user"));
        }
    }

    /**
     * 根据用户名搜索用户
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDialUsers(@RequestParam String username) {
        try {
            logger.info("Received request to search users by username: {}", username);
            List<DialUser> users = userService.searchDialUsersByDialUsername(username);
            logger.info("Found {} users matching username: {}", users.size(), username);
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            logger.error("Failed to search users by username: {}", username, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to search users"));
        }
    }

    /**
     * 验证用户密码
     *
     * @param request 密码验证请求
     * @return 验证结果
     */
    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            logger.info("Received request to validate password for user: {}", username);
            
            if (username == null || username.trim().isEmpty()) {
                logger.warn("DialUsername is required");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", "DialUsername is required"));
            }
            
            if (password == null || password.trim().isEmpty()) {
                logger.warn("Password is required");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", "Password is required"));
            }

            boolean isValid = userService.validatePassword(username.trim(), password);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "Password is valid" : "Password is invalid");
            
            logger.info("Password validation result for user {}: {}", username, isValid);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Failed to validate password for user: {}", request.get("username"), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to validate password"));
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param request 登录时间更新请求
     * @return 更新结果
     */
    @PostMapping("/update-login-time")
    public ResponseEntity<?> updateLastLoginTime(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            
            logger.info("Received request to update last login time for user: {}", username);
            
            if (username == null || username.trim().isEmpty()) {
                logger.warn("DialUsername is required");
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("VALIDATION_ERROR", "DialUsername is required"));
            }

            userService.updateLastLoginTime(username.trim());
            logger.info("Successfully updated last login time for user: {}", username);
            return ResponseEntity.ok(createSuccessResponse("Last login time updated successfully"));
        } catch (RuntimeException e) {
            logger.error("Failed to update last login time for user: {}", request.get("username"), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "Failed to update last login time"));
        }
    }

    /**
     * 创建错误响应
     *
     * @param code 错误代码
     * @param message 错误消息
     * @return 错误响应
     */
    private Map<String, Object> createErrorResponse(String code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", code);
        response.put("message", message);
        return response;
    }

    /**
     * 创建成功响应
     *
     * @param message 成功消息
     * @return 成功响应
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }
}
