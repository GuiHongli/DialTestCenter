/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.WssMessage;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.model.Executor;
import com.huawei.cloududn.dialingtest.service.executormanagement.SessionBindingRegistry;
import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.ExecutorWebsocketEndpoint;
import com.huawei.cloududn.dialingtest.api.ExecutorsApi;
import com.huawei.cloududn.dialingtest.model.ExecutorPageResponse;
import com.huawei.cloududn.dialingtest.model.ExecutorPageData;
import com.huawei.cloududn.dialingtest.model.OperationResponse;
import com.huawei.cloududn.dialingtest.model.RefreshExecutorRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Executor northbound REST API.
 *
 * <p>Provides executor list query and refresh command.</p>
 *
 * @author g00940940
 * @since 2025-11-04
 */
@RestController
@RequestMapping("/api")
public class ExecutorController implements ExecutorsApi {

    private static final Logger logger = LoggerFactory.getLogger(ExecutorController.class);

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private SessionBindingRegistry registry;

    @Autowired
    private ExecutorWebsocketEndpoint endpoint;

    @Override
    public ResponseEntity<ExecutorPageResponse> listExecutors(Integer page, Integer size, Integer status, String keyword) {
        logger.info("Listing executors, page={}, size={}, status={}, keyword={}", page, size, status, keyword);
        int p = page == null ? 0 : page;
        int s = size == null ? 20 : size;
        if (p < 0 || s <= 0 || s > 200) {
            logger.warn("Invalid page parameters, page={}, size={}", p, s);
            ExecutorPageResponse resp = new ExecutorPageResponse();
            resp.setSuccess(false);
            resp.setMessage("Invalid page parameters");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
        }
        Integer st = status == null ? null : status;
        logger.debug("Querying executors from database, offset={}, limit={}", p * s, s);
        List<Executor> content = executorDao.findPage(st, keyword, p * s, s);
        int total = executorDao.count(st, keyword);
        logger.debug("Query completed, found {} executors, total count={}", content.size(), total);
        int totalPages = (int) Math.ceil((double) total / s);

        ExecutorPageData data = new ExecutorPageData();
        data.setContent(content);
        data.setTotalElements(total);
        data.setTotalPages(totalPages);
        data.setSize(s);
        data.setNumber(p);
        data.setFirst(p == 0);
        data.setLast(p >= totalPages - 1);

        ExecutorPageResponse resp = new ExecutorPageResponse();
        resp.setSuccess(true);
        resp.setMessage("OK");
        resp.setData(data);
        logger.info("Listing executors completed successfully, returned {} executors", content.size());
        return ResponseEntity.ok(resp);
    }

    @Override
    public ResponseEntity<OperationResponse> refreshExecutor(RefreshExecutorRequest refreshExecutorRequest) {
        // TODO: V3版本需要重新实现refreshExecutor，使用TLV格式
        String name = refreshExecutorRequest == null ? null : refreshExecutorRequest.getName();
        logger.info("Refresh executor request received for: {} (V3 TLV implementation pending)", name);
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(op(false, "V3 TLV implementation pending"));
    }

    private static OperationResponse op(boolean success, String message) {
        OperationResponse r = new OperationResponse();
        r.setSuccess(success);
        r.setMessage(message);
        return r;
    }
}


