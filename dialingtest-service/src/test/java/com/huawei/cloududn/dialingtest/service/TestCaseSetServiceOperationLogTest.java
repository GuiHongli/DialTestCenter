/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.TestCaseSetDao;
import com.huawei.cloududn.dialingtest.dao.TestCaseDao;
import com.huawei.cloududn.dialingtest.dao.AppTypeDao;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.UpdateTestCaseSetRequest;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * TestCaseSetService操作记录测试
 * 
 * @author Generated
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetServiceOperationLogTest {

    @Mock
    private TestCaseSetDao testCaseSetDao;

    @Mock
    private TestCaseDao testCaseDao;

    @Mock
    private AppTypeDao appTypeDao;

    @Mock
    private ArchiveParseService archiveParseService;

    @Mock
    private ExcelParseService excelParseService;

    @Mock
    private OperationLogUtil operationLogUtil;

    @InjectMocks
    private TestCaseSetService testCaseSetService;

    private MultipartFile mockFile;
    private TestCaseSet mockTestCaseSet;

    @Before
    public void setUp() throws IOException {
        // 创建模拟文件
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("testcase_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(mockFile.isEmpty()).thenReturn(false);

        // 创建模拟用例集
        mockTestCaseSet = new TestCaseSet();
        mockTestCaseSet.setId(1L);
        mockTestCaseSet.setName("testcase");
        mockTestCaseSet.setVersion("1.0");
    }

    /**
     * 测试上传用例集时记录操作日志
     */
    @Test
    public void testUploadTestCaseSet_WithOperatorUsername_ShouldLogOperation() {
        // Arrange
        String operatorUsername = "testuser";
        String description = "测试描述";
        String businessZh = "测试业务";
        
        when(testCaseSetDao.existsByNameAndVersion(any(), any())).thenReturn(0);
        when(testCaseSetDao.insert(any(TestCaseSet.class))).thenReturn(1);
        when(archiveParseService.parseArchive(any())).thenReturn(mock(ArchiveParseResult.class));
        when(excelParseService.parseExcel(any())).thenReturn(java.util.Collections.emptyList());

        // Act
        testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, false, operatorUsername);

        // Assert
        verify(operationLogUtil, times(1)).logTestCaseSetUpload(eq(operatorUsername), any(TestCaseSet.class));
    }

    /**
     * 测试更新用例集时记录操作日志
     */
    @Test
    public void testUpdateTestCaseSet_WithOperatorUsername_ShouldLogOperation() {
        // Arrange
        String operatorUsername = "testuser";
        Long testCaseSetId = 1L;
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        request.setDescription("更新描述");
        
        when(testCaseSetDao.findById(testCaseSetId)).thenReturn(mockTestCaseSet);
        when(testCaseSetDao.update(any(TestCaseSet.class))).thenReturn(1);

        // Act
        testCaseSetService.updateTestCaseSet(testCaseSetId, request, operatorUsername);

        // Assert
        verify(operationLogUtil, times(1)).logTestCaseSetUpdate(eq(operatorUsername), any(TestCaseSet.class));
    }

    /**
     * 测试删除用例集时记录操作日志
     */
    @Test
    public void testDeleteTestCaseSet_WithOperatorUsername_ShouldLogOperation() {
        // Arrange
        String operatorUsername = "testuser";
        Long testCaseSetId = 1L;
        
        when(testCaseSetDao.findById(testCaseSetId)).thenReturn(mockTestCaseSet);
        when(testCaseDao.deleteByTestCaseSetId(testCaseSetId)).thenReturn(1);
        when(testCaseSetDao.deleteById(testCaseSetId)).thenReturn(1);

        // Act
        testCaseSetService.deleteTestCaseSet(testCaseSetId, operatorUsername);

        // Assert
        verify(operationLogUtil, times(1)).logTestCaseSetDelete(eq(operatorUsername), any(TestCaseSet.class));
    }

    /**
     * 测试默认用户名（admin）的情况
     */
    @Test
    public void testUploadTestCaseSet_WithDefaultUsername_ShouldLogWithAdmin() {
        // Arrange
        when(testCaseSetDao.existsByNameAndVersion(any(), any())).thenReturn(0);
        when(testCaseSetDao.insert(any(TestCaseSet.class))).thenReturn(1);
        when(archiveParseService.parseArchive(any())).thenReturn(mock(ArchiveParseResult.class));
        when(excelParseService.parseExcel(any())).thenReturn(java.util.Collections.emptyList());

        // Act
        testCaseSetService.uploadTestCaseSet(mockFile, "描述", "业务");

        // Assert
        verify(operationLogUtil, times(1)).logTestCaseSetUpload(eq("admin"), any(TestCaseSet.class));
    }
}
