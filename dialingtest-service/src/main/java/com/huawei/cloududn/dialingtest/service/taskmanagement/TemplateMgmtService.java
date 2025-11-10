/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service.taskmanagement;

import com.huawei.cloududn.dialingtest.dao.taskmanagement.TemplateDao;
import com.huawei.cloududn.dialingtest.model.TemplateEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 模板表原子CRUD服务。
 *
 * @author g00940940
 * @since 2025-10-24
 */
@Service
@Transactional
public class TemplateMgmtService {
    private static final Logger logger = LoggerFactory.getLogger(TemplateMgmtService.class);

    @Autowired
    private TemplateDao templateDao;

    public TemplateEntity create(TemplateEntity entity) {
        int r = templateDao.insert(entity);
        if (r <= 0) {
            throw new IllegalStateException("Insert template failed");
        } else {
            logger.info("Template created: {}", entity.getId());
            return entity;
        }
    }

    public TemplateEntity update(TemplateEntity entity) {
        int r = templateDao.update(entity);
        if (r <= 0) {
            throw new IllegalStateException("Update template failed");
        } else {
            return entity;
        }
    }

    public void delete(Long id) {
        templateDao.delete(id);
        logger.info("Template deleted: {}", id);
    }

    @Transactional(readOnly = true)
    public TemplateEntity findById(Long id) {
        return templateDao.findById(id);
    }

    @Transactional(readOnly = true)
    public List<TemplateEntity> findAll() {
        return templateDao.findAll();
    }

    @Transactional(readOnly = true)
    public List<TemplateEntity> findEnabled() {
        return templateDao.findEnabled();
    }
}


