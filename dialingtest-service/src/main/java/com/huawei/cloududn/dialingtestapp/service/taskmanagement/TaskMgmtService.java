/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.taskmanagement;

import com.huawei.cloududn.dialingtestapp.dao.taskmanagement.TaskDao;
import com.huawei.cloududn.dialingtest.model.TaskEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 任务/模板管理-任务表原子CRUD服务。
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Service
@Transactional
public class TaskMgmtService {
    private static final Logger logger = LoggerFactory.getLogger(TaskMgmtService.class);

    @Autowired
    private TaskDao taskDao;

    public TaskEntity create(TaskEntity entity) {
        // Backward compatible: treat as main task when mainTaskId is null
        return createMainTask(entity);
    }

    /**
     * 创建主任务：要求 parentTaskId 为空；mainTaskId 由本方法在插入后初始化为自身 id。
     */
    public TaskEntity createMainTask(TaskEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        } else {
            // 主任务的父ID必须为空
            entity.setParentTaskId(null);
            int r = taskDao.insert(entity);
            if (r <= 0) {
                throw new IllegalStateException("Insert task failed");
            } else {
                Long id = entity.getId() == null ? null : entity.getId().longValue();
                taskDao.initMainTaskId(id);
                logger.info("Main task created: {}", entity.getId());
                TaskEntity fresh = taskDao.findById(id);
                return fresh == null ? entity : fresh;
            }
        }
    }

    /**
     * 创建子任务：需要指定主任务ID与父任务ID。
     * 多层嵌套时 mainTaskId 固定为顶层主任务ID，parentTaskId 为直接父任务ID。
     */
    public TaskEntity createSubTask(Long mainTaskId, Long parentTaskId, TaskEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity cannot be null");
        } else if (mainTaskId == null || parentTaskId == null) {
            throw new IllegalArgumentException("mainTaskId and parentTaskId cannot be null for sub task");
        } else {
            entity.setMainTaskId(mainTaskId.intValue());
            entity.setParentTaskId(parentTaskId.intValue());
            int r = taskDao.insert(entity);
            if (r <= 0) {
                throw new IllegalStateException("Insert sub task failed");
            } else {
                Long id = entity.getId() == null ? null : entity.getId().longValue();
                logger.info("Sub task created: {} -> main={}, parent={}", id, mainTaskId, parentTaskId);
                TaskEntity fresh = taskDao.findById(id);
                return fresh == null ? entity : fresh;
            }
        }
    }

    public void updateStatusAndContext(Long id, String status, String result, String context) {
        int r = taskDao.updateStatusAndContext(id, status, result, context);
        if (r <= 0) {
            throw new IllegalStateException("Update task failed");
        } else {
            logger.info("Task updated: {} status={}, result= {}", id, status, result);
        }
    }

    @Transactional(readOnly = true)
    public TaskEntity findById(Long id) {
        return taskDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> findMainTasks(int page, int size) {
        int offset = page * size;
        return taskDao.findMainTasks(offset, size);
    }

    @Transactional(readOnly = true)
    public long countMainTasks() {
        return taskDao.countMainTasks();
    }

    @Transactional(readOnly = true)
    public List<TaskEntity> findSubTasksByMainTaskId(Long mainTaskId) {
        if (mainTaskId == null) {
            throw new IllegalArgumentException("mainTaskId cannot be null");
        } else {
            return taskDao.findSubTasksByMainTaskId(mainTaskId);
        }
    }
}


