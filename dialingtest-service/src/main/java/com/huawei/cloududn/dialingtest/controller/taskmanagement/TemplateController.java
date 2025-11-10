/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.taskmanagement;

import com.huawei.cloududn.dialingtest.api.TemplatesApi;
import com.huawei.cloududn.dialingtest.model.TemplateEntity;
import com.huawei.cloududn.dialingtest.service.taskmanagement.TemplateMgmtService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * 模板管理接口（CRUD）。
 *
 * @author g00940940
 * @since 2025-10-24
 */
@RestController
@RequestMapping("/api")
public class TemplateController implements TemplatesApi {
    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Autowired
    private TemplateMgmtService templateService;

    @Override
    public ResponseEntity<TemplateEntity> createTemplate(@RequestBody TemplateEntity body) {
        TemplateEntity saved = templateService.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Override
    public ResponseEntity<Void> deleteTemplate(Integer id) {
        Long longId = id == null ? null : id.longValue();
        templateService.delete(longId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<TemplateEntity> getTemplateById(Integer id) {
        Long longId = id == null ? null : id.longValue();
        TemplateEntity t = templateService.findById(longId);
        if (t == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(t);
        }
    }

    @Override
    public ResponseEntity<List<TemplateEntity>> getTemplates() {
        List<TemplateEntity> list = templateService.findAll();
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<TemplateEntity> updateTemplate(Integer id, @RequestBody TemplateEntity body) {
        body.setId(id);
        TemplateEntity updated = templateService.update(body);
        return ResponseEntity.ok(updated);
    }
}


