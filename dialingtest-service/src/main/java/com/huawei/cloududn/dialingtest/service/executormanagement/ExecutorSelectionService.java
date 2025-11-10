/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.executormanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.dao.executormanagement.ExecutorDao;
import com.huawei.cloududn.dialingtest.dao.executormanagement.UeDao;
import com.huawei.cloududn.dialingtest.model.Executor;
import com.huawei.cloududn.dialingtest.model.Ue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * 执行机选择服务,负责选择可用的执行机和UE
 *
 * @author g00940940
 * @since 2025-11-09
 */
@Service
public class ExecutorSelectionService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorSelectionService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();

    @Autowired
    private ExecutorDao executorDao;

    @Autowired
    private UeDao ueDao;

    /**
     * 选择一个可用的执行机和UE
     *
     * @return 执行机和UE信息,如果没有可用的返回null
     */
    public ExecutorUeInfo selectIdleExecutorAndUe() {
        logger.debug("Starting to select idle executor and UE");
        List<Executor> onlineExecutors = executorDao.findPage(1, null, 0, 100);
        if (onlineExecutors == null || onlineExecutors.isEmpty()) {
            logger.warn("No online executors found");
            return null;
        } else {
            logger.debug("Found {} online executors", onlineExecutors.size());
            for (Executor executor : onlineExecutors) {
                List<Ue> ueList = ueDao.findByExecutorName(executor.getName());
                if (ueList != null && !ueList.isEmpty()) {
                    for (Ue ue : ueList) {
                        if (isUeIdle(ue)) {
                            logger.info("Selected executor={}, ue={}", executor.getName(), ue.getMsisdn());
                            return new ExecutorUeInfo(executor, ue);
                        }
                    }
                    logger.debug("No idle UE found for executor={}", executor.getName());
                } else {
                    logger.debug("No UE found for executor={}", executor.getName());
                }
            }
            logger.warn("No idle UE found across all online executors");
            return null;
        }
    }

    /**
     * 检查UE是否空闲
     *
     * @param ue UE实体
     * @return true if idle
     */
    private boolean isUeIdle(Ue ue) {
        if (ue.getInfo() == null || ue.getInfo().isEmpty()) {
            return false;
        } else {
            try {
                JsonNode infoNode = objectMapper.readTree(ue.getInfo());
                String status = infoNode.has("status") ? infoNode.get("status").asText("") : "";
                return "idle".equalsIgnoreCase(status);
            } catch (Exception e) {
                logger.warn("Failed to parse UE info for msisdn={}", ue.getMsisdn(), e);
                return false;
            }
        }
    }

    /**
     * 执行机和UE信息封装类
     */
    public static class ExecutorUeInfo {
        private final Executor executor;
        private final Ue ue;

        public ExecutorUeInfo(Executor executor, Ue ue) {
            this.executor = executor;
            this.ue = ue;
        }

        public Executor getExecutor() {
            return executor;
        }

        public Ue getUe() {
            return ue;
        }
    }
}

