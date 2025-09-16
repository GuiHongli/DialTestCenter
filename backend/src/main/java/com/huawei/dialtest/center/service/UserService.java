/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.User;
import com.huawei.dialtest.center.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户服务类，提供用户管理的业务逻辑
 * 支持用户的增删改查操作，包括密码加密和权限控制
 *
 * @author g00940940
 * @since 2025-09-09
 */
@Service
@Transactional
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 获取用户列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @param search 搜索关键字（可选）
     * @return 用户分页数据
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(int page, int pageSize, String search) {
        try {
            logger.debug("Getting users - page: {}, size: {}, search: {}", page, pageSize, search);
            Pageable pageable = PageRequest.of(page - 1, pageSize);
            
            List<User> content;
            long total;
            
            if (search != null && !search.trim().isEmpty()) {
                // 带搜索条件的分页查询
                content = userMapper.findByUsernameContainingWithPage(search.trim(), page - 1, pageSize);
                total = userMapper.countByUsernameContaining(search.trim());
            } else {
                // 无搜索条件的分页查询
                content = userMapper.findAllByOrderByCreatedTimeDesc(page - 1, pageSize);
                total = userMapper.count();
            }
            
            Page<User> result = new PageImpl<>(content, pageable, total);
            logger.info("Successfully retrieved {} users (page {}/{})", content.size(), page, result.getTotalPages());
            return result;
        } catch (DataAccessException e) {
            logger.error("Failed to get users", e);
            throw new RuntimeException("Failed to retrieve users", e);
        }
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        try {
            logger.info("Getting user by ID: {}", id);
            User user = userMapper.findById(id);
            Optional<User> userOptional = Optional.ofNullable(user);
            if (userOptional.isPresent()) {
                logger.info("Successfully retrieved user: {}", userOptional.get().getUsername());
            } else {
                logger.warn("User not found with ID: {}", id);
            }
            return userOptional;
        } catch (DataAccessException e) {
            logger.error("Failed to get user by ID: {}", id, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        try {
            logger.info("Getting user by username: {}", username);
            User user = userMapper.findByUsername(username);
            Optional<User> userOptional = Optional.ofNullable(user);
            if (userOptional.isPresent()) {
                logger.info("Successfully retrieved user: {}", username);
            } else {
                logger.warn("User not found with username: {}", username);
            }
            return userOptional;
        } catch (DataAccessException e) {
            logger.error("Failed to get user by username: {}", username, e);
            throw new RuntimeException("Failed to retrieve user", e);
        }
    }

    /**
     * 创建新用户
     *
     * @param username 用户名
     * @param password 密码
     * @return 创建的用户
     */
    public User createUser(String username, String password) {
        try {
            logger.info("Creating new user: {}", username);
            
            if (userMapper.existsByUsername(username)) {
                logger.warn("Username already exists: {}", username);
                throw new IllegalArgumentException("Username already exists: " + username);
            }

            String encodedPassword = passwordEncoder.encode(password);
            User user = new User(username, encodedPassword);
            int result = userMapper.insert(user);
            if (result > 0) {
                logger.info("Successfully created user: {}", username);
                return user;
            } else {
                throw new RuntimeException("Failed to create user");
            }
        } catch (DataAccessException e) {
            logger.error("Failed to create user: {}", username, e);
            throw new RuntimeException("Failed to create user", e);
        }
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param username 新用户名
     * @param password 新密码
     * @return 更新后的用户
     */
    public User updateUser(Long id, String username, String password) {
        try {
            logger.info("Updating user with ID: {}", id);
            
            User user = userMapper.findById(id);
            if (user == null) {
                throw new IllegalArgumentException("User not found with ID: " + id);
            }

            if (username != null && !username.equals(user.getUsername())) {
                if (userMapper.existsByUsername(username)) {
                    logger.warn("Username already exists: {}", username);
                    throw new IllegalArgumentException("Username already exists: " + username);
                }
                user.setUsername(username);
            }

            if (password != null && !password.isEmpty()) {
                String encodedPassword = passwordEncoder.encode(password);
                user.setPassword(encodedPassword);
            }

            user.setUpdatedTime(LocalDateTime.now());
            int result = userMapper.update(user);
            if (result > 0) {
                logger.info("Successfully updated user: {}", user.getUsername());
                return user;
            } else {
                throw new RuntimeException("Failed to update user");
            }
        } catch (DataAccessException e) {
            logger.error("Failed to update user with ID: {}", id, e);
            throw new RuntimeException("Failed to update user", e);
        }
    }

    /**
     * 删除用户
     *
     * @param id 用户ID
     */
    public void deleteUser(Long id) {
        try {
            logger.info("Deleting user with ID: {}", id);
            
            User user = userMapper.findById(id);
            if (user == null) {
                logger.warn("User not found with ID: {}", id);
                throw new IllegalArgumentException("User not found with ID: " + id);
            }
            
            int result = userMapper.deleteById(id);
            if (result == 0) {
                throw new RuntimeException("Failed to delete user");
            }
            logger.info("Successfully deleted user with ID: {}", id);
        } catch (DataAccessException e) {
            logger.error("Failed to delete user with ID: {}", id, e);
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    /**
     * 更新用户最后登录时间
     *
     * @param username 用户名
     */
    public void updateLastLoginTime(String username) {
        try {
            logger.info("Updating last login time for user: {}", username);
            
            User user = userMapper.findByUsername(username);
            Optional<User> userOpt = Optional.ofNullable(user);
            if (userOpt.isPresent()) {
                User foundUser = userOpt.get();
                foundUser.setLastLoginTime(LocalDateTime.now());
                userMapper.update(foundUser);
                logger.info("Successfully updated last login time for user: {}", username);
            } else {
                logger.warn("User not found for updating last login time: {}", username);
            }
        } catch (DataAccessException e) {
            logger.error("Failed to update last login time for user: {}", username, e);
            throw new RuntimeException("Failed to update last login time", e);
        }
    }

    /**
     * 验证用户密码
     *
     * @param username 用户名
     * @param password 密码
     * @return 验证结果
     */
    @Transactional(readOnly = true)
    public boolean validatePassword(String username, String password) {
        try {
            logger.info("Validating password for user: {}", username);
            
            User user = userMapper.findByUsername(username);
            Optional<User> userOpt = Optional.ofNullable(user);
            if (userOpt.isPresent()) {
                User foundUser = userOpt.get();
                boolean isValid = passwordEncoder.matches(password, foundUser.getPassword());
                logger.info("Password validation result for user {}: {}", username, isValid);
                return isValid;
            } else {
                logger.warn("User not found for password validation: {}", username);
                return false;
            }
        } catch (DataAccessException e) {
            logger.error("Failed to validate password for user: {}", username, e);
            throw new RuntimeException("Failed to validate password", e);
        }
    }

    /**
     * 根据用户名搜索用户
     *
     * @param username 用户名关键字
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> searchUsersByUsername(String username) {
        try {
            logger.info("Searching users by username: {}", username);
            List<User> users = userMapper.findByUsernameContaining(username);
            logger.info("Found {} users matching username: {}", users.size(), username);
            return users;
        } catch (DataAccessException e) {
            logger.error("Failed to search users by username: {}", username, e);
            throw new RuntimeException("Failed to search users", e);
        }
    }
}
