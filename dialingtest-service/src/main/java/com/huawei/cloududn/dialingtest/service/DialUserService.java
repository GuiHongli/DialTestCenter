/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.DialUserDao;
import com.huawei.cloududn.dialingtest.model.CreateDialUserRequest;
import com.huawei.cloududn.dialingtest.model.DialUser;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponse;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponseData;
import com.huawei.cloududn.dialingtest.model.DialUserResponse;
import com.huawei.cloududn.dialingtest.model.UpdateDialUserRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拨测用户服务层
 *
 * @author g00940940
 * @since 2025-09-18
 */
@Service
@Transactional
public class DialUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(DialUserService.class);
    
    @Autowired
    private DialUserDao dialUserDao;
    
    /**
     * 创建拨测用户
     *
     * @param request 创建用户请求
     * @return 用户响应
     */
    public DialUserResponse createDialUser(CreateDialUserRequest request) {
        logger.info("Creating dial user with username: {}", request.getUsername());
        
        try {
            // 检查用户名是否已存在
            if (dialUserDao.existsByUsername(request.getUsername(), null)) {
                logger.warn("Username already exists: {}", request.getUsername());
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("用户名已存在");
                return response;
            }
            
            // 创建用户对象
            DialUser dialUser = new DialUser();
            dialUser.setUsername(request.getUsername());
            dialUser.setPassword(encryptPassword(request.getPassword()));
            dialUser.setLastLoginTime(null);
            
            // 插入数据库
            Integer userId = dialUserDao.insert(dialUser);
            dialUser.setId(userId);
            
            logger.info("Dial user created successfully with ID: {}", userId);
            
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(true);
            response.setData(dialUser);
            response.setMessage("用户创建成功");
            return response;
            
        } catch (DataAccessException e) {
            logger.error("Database error while creating dial user: {}", request.getUsername(), e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument while creating dial user: {}", request.getUsername(), e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("请求参数错误");
            return response;
        }
    }
    
    /**
     * 根据ID查询拨测用户
     *
     * @param id 用户ID
     * @return 用户响应
     */
    public DialUserResponse getDialUserById(Integer id) {
        logger.info("Getting dial user by ID: {}", id);
        
        try {
            DialUser dialUser = dialUserDao.findById(id);
            if (dialUser == null) {
                logger.warn("Dial user not found with ID: {}", id);
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("用户不存在");
                return response;
            }
            
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(true);
            response.setData(dialUser);
            response.setMessage("查询成功");
            return response;
            
        } catch (DataAccessException e) {
            logger.error("Database error while getting dial user by ID: {}", id, e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return response;
        }
    }
    
    /**
     * 分页查询拨测用户
     *
     * @param username 用户名过滤条件
     * @param page 页码
     * @param size 每页大小
     * @return 分页响应
     */
    public DialUserPageResponse getDialUsers(String username, Integer page, Integer size) {
        logger.info("Getting dial users with username filter: {}, page: {}, size: {}", username, page, size);
        
        try {
            int offset = page * size;
            List<DialUser> users = dialUserDao.findByPage(username, offset, size);
            Long totalElements = dialUserDao.count(username);
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            DialUserPageResponseData data = new DialUserPageResponseData();
            data.setContent(users);
            data.setTotalElements(totalElements);
            data.setTotalPages(totalPages);
            data.setSize(size);
            data.setNumber(page);
            
            DialUserPageResponse response = new DialUserPageResponse();
            response.setSuccess(true);
            response.setData(data);
            response.setMessage("查询成功");
            return response;
            
        } catch (DataAccessException e) {
            logger.error("Database error while getting dial users", e);
            DialUserPageResponse response = new DialUserPageResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return response;
        }
    }
    
    /**
     * 更新拨测用户
     *
     * @param id 用户ID
     * @param request 更新请求
     * @return 用户响应
     */
    public DialUserResponse updateDialUser(Integer id, UpdateDialUserRequest request) {
        logger.info("Updating dial user with ID: {}", id);
        
        try {
            // 检查用户是否存在
            DialUser existingUser = dialUserDao.findById(id);
            if (existingUser == null) {
                logger.warn("Dial user not found with ID: {}", id);
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("用户不存在");
                return response;
            }
            
            // 检查用户名是否已被其他用户使用
            if (request.getUsername() != null && 
                !request.getUsername().equals(existingUser.getUsername()) &&
                dialUserDao.existsByUsername(request.getUsername(), id)) {
                logger.warn("Username already exists for update: {}", request.getUsername());
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("用户名已存在");
                return response;
            }
            
            // 更新用户信息
            if (request.getUsername() != null) {
                existingUser.setUsername(request.getUsername());
            }
            if (request.getPassword() != null) {
                existingUser.setPassword(encryptPassword(request.getPassword()));
            }
            
            int updatedRows = dialUserDao.update(existingUser);
            if (updatedRows == 0) {
                logger.warn("No rows updated for dial user ID: {}", id);
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("更新失败");
                return response;
            }
            
            logger.info("Dial user updated successfully with ID: {}", id);
            
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(true);
            response.setData(existingUser);
            response.setMessage("更新成功");
            return response;
            
        } catch (DataAccessException e) {
            logger.error("Database error while updating dial user ID: {}", id, e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return response;
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument while updating dial user ID: {}", id, e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("请求参数错误");
            return response;
        }
    }
    
    /**
     * 删除拨测用户
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    public boolean deleteDialUser(Integer id) {
        logger.info("Deleting dial user with ID: {}", id);
        
        try {
            int deletedRows = dialUserDao.deleteById(id);
            if (deletedRows == 0) {
                logger.warn("No dial user found to delete with ID: {}", id);
                return false;
            }
            
            logger.info("Dial user deleted successfully with ID: {}", id);
            return true;
            
        } catch (DataAccessException e) {
            logger.error("Database error while deleting dial user ID: {}", id, e);
            return false;
        }
    }
    
    /**
     * 更新最后登录时间
     *
     * @param username 用户名
     */
    public void updateLastLoginTime(String username) {
        logger.debug("Updating last login time for user: {}", username);
        
        try {
            DialUser user = dialUserDao.findByUsername(username);
            if (user != null) {
                user.setLastLoginTime(LocalDateTime.now());
                dialUserDao.update(user);
                logger.debug("Last login time updated for user: {}", username);
            }
        } catch (DataAccessException e) {
            logger.error("Database error while updating last login time for user: {}", username, e);
        }
    }
    
    /**
     * 密码加密
     *
     * @param password 原始密码
     * @return 加密后的密码
     */
    private String encryptPassword(String password) {
        // 简单的密码加密，实际项目中应使用更安全的加密方式
        return java.util.Base64.getEncoder().encodeToString(password.getBytes());
    }
}
