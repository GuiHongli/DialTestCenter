/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.taskmanagement;

import com.huawei.cloududn.dialingtest.model.TemplateEntity;
import com.huawei.cloududn.dialingtestapp.service.taskmanagement.TemplateMgmtService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 模板管理控制器测试类
 *
 * @author g00940940
 * @since 2025-11-13
 */
@RunWith(MockitoJUnitRunner.class)
public class TemplateControllerTest {
    @Mock
    private TemplateMgmtService templateService;
    @InjectMocks
    private TemplateController templateController;

    @Test
    public void testCreateTemplate_Success_ReturnsCreated() {
        // Arrange
        TemplateEntity template = new TemplateEntity();
        template.setId(1);
        when(templateService.create(any())).thenReturn(template);

        // Act
        ResponseEntity<TemplateEntity> response = templateController.createTemplate(new TemplateEntity());

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(template, response.getBody());
        verify(templateService).create(any());
    }

    @Test
    public void testGetTemplates_Success_ReturnsOk() {
        // Arrange
        List<TemplateEntity> list = Arrays.asList(new TemplateEntity());
        when(templateService.findAll()).thenReturn(list);

        // Act
        ResponseEntity<List<TemplateEntity>> response = templateController.getTemplates();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        verify(templateService).findAll();
    }

    @Test
    public void testGetTemplateById_Success_ReturnsOk() {
        // Arrange
        TemplateEntity template = new TemplateEntity();
        when(templateService.findById(1L)).thenReturn(template);

        // Act
        ResponseEntity<TemplateEntity> response = templateController.getTemplateById(1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(template, response.getBody());
    }

    @Test
    public void testGetTemplateById_NotFound_ReturnsNotFound() {
        // Arrange
        when(templateService.findById(999L)).thenReturn(null);

        // Act
        ResponseEntity<TemplateEntity> response = templateController.getTemplateById(999);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testUpdateTemplate_Success_ReturnsOk() {
        // Arrange
        TemplateEntity template = new TemplateEntity();
        when(templateService.update(any())).thenReturn(template);

        // Act
        ResponseEntity<TemplateEntity> response = templateController.updateTemplate(1, new TemplateEntity());

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(templateService).update(any());
    }

    @Test
    public void testDeleteTemplate_Success_ReturnsNoContent() {
        // Act
        ResponseEntity<Void> response = templateController.deleteTemplate(1);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(templateService).delete(1L);
    }
}


