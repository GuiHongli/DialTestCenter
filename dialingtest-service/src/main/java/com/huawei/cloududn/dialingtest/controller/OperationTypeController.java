/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.OperationTypesApi;
import com.huawei.cloududn.dialingtest.dao.OperationTypeDao;
import com.huawei.cloududn.dialingtest.model.OperationTypeListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作类型控制器，提供操作类型管理的REST API接口
 * 支持操作类型的查询等操作
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/api")
public class OperationTypeController implements OperationTypesApi {
    private static final Logger logger = LoggerFactory.getLogger(OperationTypeController.class);
    
    @Autowired
    private OperationTypeDao operationTypeDao;
    
    /**
     * 查询操作类型列表
     *
     * @return 操作类型列表响应
     */
    @Override
    public ResponseEntity<OperationTypeListResponse> operationTypesGet() {
        try {
            logger.info("Querying operation types list");
            
            OperationTypeListResponse response = operationTypeDao.getAllOperationTypes();
            
            logger.info("Successfully queried {} operation types", response.getData().size());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
                
        } catch (Exception e) {
            logger.error("Error querying operation types", e);
            return ResponseEntity.status(500)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createErrorResponse("Internal server error"));
        }
    }
    
    /**
     * 创建错误响应
     *
     * @param message 错误消息
     * @return 错误响应
     */
    private OperationTypeListResponse createErrorResponse(String message) {
        OperationTypeListResponse response = new OperationTypeListResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
