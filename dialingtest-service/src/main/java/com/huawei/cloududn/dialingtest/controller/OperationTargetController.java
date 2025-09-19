/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.OperationTargetsApi;
import com.huawei.cloududn.dialingtest.dao.OperationTargetDao;
import com.huawei.cloududn.dialingtest.model.OperationTargetListResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作目标控制器，提供操作目标管理的REST API接口
 * 支持操作目标的查询等操作
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RestController
@RequestMapping("/api")
public class OperationTargetController implements OperationTargetsApi {
    private static final Logger logger = LoggerFactory.getLogger(OperationTargetController.class);
    
    @Autowired
    private OperationTargetDao operationTargetDao;
    
    /**
     * 查询操作目标列表
     *
     * @return 操作目标列表响应
     */
    @Override
    public ResponseEntity<OperationTargetListResponse> operationTargetsGet() {
        try {
            logger.info("Querying operation targets list");
            
            OperationTargetListResponse response = operationTargetDao.getAllOperationTargets();
            
            logger.info("Successfully queried {} operation targets", response.getData().size());
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
                
        } catch (Exception e) {
            logger.error("Error querying operation targets", e);
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
    private OperationTargetListResponse createErrorResponse(String message) {
        OperationTargetListResponse response = new OperationTargetListResponse();
        response.setSuccess(false);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}
