/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.CreateDialUserRequest;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponse;
import com.huawei.cloududn.dialingtest.model.DialUserResponse;
import com.huawei.cloududn.dialingtest.model.UpdateDialUserRequest;
import com.huawei.cloududn.dialingtest.service.DialUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 拨测用户控制器
 *
 * @author g00940940
 * @since 2025-09-18
 */
@RestController
@RequestMapping("/dialingtest/api")
@Api(tags = "拨测用户管理", description = "拨测用户的增删改查操作")
public class DialUserController {
    
    private static final Logger logger = LoggerFactory.getLogger(DialUserController.class);
    
    @Autowired
    private DialUserService dialUserService;
    
    /**
     * 分页查询拨测用户
     *
     * @param page 页码，从0开始
     * @param size 每页大小
     * @param username 用户名过滤条件
     * @return 分页响应
     */
    @GetMapping("/dialusers")
    @ApiOperation(value = "分页查询拨测用户", notes = "支持用户名过滤的分页查询")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 400, message = "请求参数错误"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public ResponseEntity<DialUserPageResponse> getDialUsers(
            @ApiParam(value = "页码，从0开始", defaultValue = "0") @RequestParam(defaultValue = "0") Integer page,
            @ApiParam(value = "每页大小", defaultValue = "10") @RequestParam(defaultValue = "10") Integer size,
            @ApiParam(value = "用户名过滤条件") @RequestParam(required = false) String username) {
        
        logger.info("Getting dial users - page: {}, size: {}, username: {}", page, size, username);
        
        try {
            if (page < 0) {
                logger.warn("Invalid page parameter: {}", page);
                DialUserPageResponse response = new DialUserPageResponse();
                response.setSuccess(false);
                response.setMessage("页码不能小于0");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (size <= 0 || size > 100) {
                logger.warn("Invalid size parameter: {}", size);
                DialUserPageResponse response = new DialUserPageResponse();
                response.setSuccess(false);
                response.setMessage("每页大小必须在1-100之间");
                return ResponseEntity.badRequest().body(response);
            }
            
            DialUserPageResponse response = dialUserService.getDialUsers(username, page, size);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in getDialUsers", e);
            DialUserPageResponse response = new DialUserPageResponse();
            response.setSuccess(false);
            response.setMessage("请求参数错误");
            return ResponseEntity.badRequest().body(response);
        } catch (DataAccessException e) {
            logger.error("Database error in getDialUsers", e);
            DialUserPageResponse response = new DialUserPageResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 新增拨测用户
     *
     * @param request 创建用户请求
     * @return 用户响应
     */
    @PostMapping("/dialusers")
    @ApiOperation(value = "新增拨测用户", notes = "创建新的拨测用户")
    @ApiResponses({
        @ApiResponse(code = 201, message = "创建成功"),
        @ApiResponse(code = 400, message = "请求参数错误"),
        @ApiResponse(code = 409, message = "用户名已存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public ResponseEntity<DialUserResponse> createDialUser(@Valid @RequestBody CreateDialUserRequest request) {
        logger.info("Creating dial user with username: {}", request.getUsername());
        
        try {
            DialUserResponse response = dialUserService.createDialUser(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else if ("用户名已存在".equals(response.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in createDialUser", e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("请求参数错误");
            return ResponseEntity.badRequest().body(response);
        } catch (DataAccessException e) {
            logger.error("Database error in createDialUser", e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 根据ID查询拨测用户
     *
     * @param id 用户ID
     * @return 用户响应
     */
    @GetMapping("/dialusers/{id}")
    @ApiOperation(value = "根据ID查询拨测用户", notes = "根据用户ID查询用户信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "查询成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public ResponseEntity<DialUserResponse> getDialUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer id) {
        
        logger.info("Getting dial user by ID: {}", id);
        
        try {
            DialUserResponse response = dialUserService.getDialUserById(id);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else if ("用户不存在".equals(response.getMessage())) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in getDialUserById", e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("请求参数错误");
            return ResponseEntity.badRequest().body(response);
        } catch (DataAccessException e) {
            logger.error("Database error in getDialUserById", e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 修改拨测用户
     *
     * @param id 用户ID
     * @param request 更新请求
     * @return 用户响应
     */
    @PutMapping("/dialusers/{id}")
    @ApiOperation(value = "修改拨测用户", notes = "更新用户信息")
    @ApiResponses({
        @ApiResponse(code = 200, message = "修改成功"),
        @ApiResponse(code = 400, message = "请求参数错误"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 409, message = "用户名已存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public ResponseEntity<DialUserResponse> updateDialUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer id,
            @Valid @RequestBody UpdateDialUserRequest request) {
        
        logger.info("Updating dial user with ID: {}", id);
        
        try {
            DialUserResponse response = dialUserService.updateDialUser(id, request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else if ("用户不存在".equals(response.getMessage())) {
                return ResponseEntity.notFound().build();
            } else if ("用户名已存在".equals(response.getMessage())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in updateDialUser", e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("请求参数错误");
            return ResponseEntity.badRequest().body(response);
        } catch (DataAccessException e) {
            logger.error("Database error in updateDialUser", e);
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("数据库操作失败");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除拨测用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/dialusers/{id}")
    @ApiOperation(value = "删除拨测用户", notes = "根据用户ID删除用户")
    @ApiResponses({
        @ApiResponse(code = 204, message = "删除成功"),
        @ApiResponse(code = 404, message = "用户不存在"),
        @ApiResponse(code = 500, message = "服务器内部错误")
    })
    public ResponseEntity<Void> deleteDialUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer id) {
        
        logger.info("Deleting dial user with ID: {}", id);
        
        try {
            boolean deleted = dialUserService.deleteDialUser(id);
            
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument in deleteDialUser", e);
            return ResponseEntity.badRequest().build();
        } catch (DataAccessException e) {
            logger.error("Database error in deleteDialUser", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
