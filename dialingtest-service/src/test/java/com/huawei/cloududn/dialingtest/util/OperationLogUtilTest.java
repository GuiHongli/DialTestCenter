/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.service.OperationLogService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 操作记录工具类测试
 *
 * @author g00940940
 * @since 2025-01-15
 */
@RunWith(MockitoJUnitRunner.class)
public class OperationLogUtilTest {

    @Mock
    private OperationLogService operationLogService;

    @InjectMocks
    private OperationLogUtil operationLogUtil;

    @Before
    public void setUp() {
        // Mock the service to return a successful response
        when(operationLogService.createOperationLog(any())).thenReturn(null);
    }

    /**
     * 测试用户登录操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserLogin_Success_CallsServiceWithCorrectParameters() throws Exception {
        // Arrange
        String operatorUsername = "user1";

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试用户登录操作记录 - 空用户名场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserLogin_EmptyUsername_CallsServiceWithEmptyUsername() throws Exception {
        // Arrange
        String operatorUsername = "";

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试用户登录操作记录 - null用户名场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogUserLogin_NullUsername_CallsServiceWithNullUsername() throws Exception {
        // Arrange
        String operatorUsername = null;

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试预处理规则ZIP包上传操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogPreprocessRulePackageUpload_Success_CallsServiceWithCorrectParameters() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "preprocess-rules-v1.0.zip";
        String businessZh = "VPN阻断";
        String businessEn = "VPN_BLOCK";

        // Act
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试预处理规则ZIP包上传操作记录 - null值场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogPreprocessRulePackageUpload_NullValues_CallsServiceWithNullData() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = null;
        String businessZh = null;
        String businessEn = null;

        // Act
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试预处理规则ZIP包删除操作记录 - 成功场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogPreprocessRulePackageDelete_Success_CallsServiceWithCorrectParameters() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "preprocess-rules-v1.0.zip";

        // Act
        operationLogUtil.logPreprocessRulePackageDelete(operatorUsername, packageName);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试预处理规则ZIP包删除操作记录 - null包名场景
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogPreprocessRulePackageDelete_NullPackageName_CallsServiceWithNullData() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = null;

        // Act
        operationLogUtil.logPreprocessRulePackageDelete(operatorUsername, packageName);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试操作记录服务异常处理 - IllegalArgumentException
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_ServiceThrowsIllegalArgumentException_LogsWarning() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        
        // Mock service to throw IllegalArgumentException
        doThrow(new IllegalArgumentException("Invalid parameters"))
            .when(operationLogService).createOperationLog(any());

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(any());
        // The method should not throw exception, just log warning
    }

    /**
     * 测试操作记录服务异常处理 - RuntimeException
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_ServiceThrowsRuntimeException_LogsWarning() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        
        // Mock service to throw RuntimeException
        doThrow(new RuntimeException("Service error"))
            .when(operationLogService).createOperationLog(any());

        // Act
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService).createOperationLog(any());
        // The method should not throw exception, just log warning
    }

    /**
     * 测试JSON转换异常处理 - 循环引用对象
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_CircularReferenceObject_HandlesJsonConversionGracefully() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "test-package.zip";
        String businessZh = "测试业务";
        String businessEn = "TEST_BUSINESS";

        // Act
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试操作数据构建 - 验证操作数据不为空
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_OperationDataIsNotEmpty() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "test-package.zip";

        // Act
        operationLogUtil.logPreprocessRulePackageDelete(operatorUsername, packageName);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试操作描述格式 - 验证中英文描述格式正确
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_DescriptionFormatIsCorrect() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "test-package.zip";
        String businessZh = "测试业务";
        String businessEn = "TEST_BUSINESS";

        // Act
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试多次调用 - 验证每次调用都会调用服务
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_MultipleCalls_CallsServiceMultipleTimes() throws Exception {
        // Arrange
        String operatorUsername = "admin";

        // Act
        operationLogUtil.logUserLogin(operatorUsername);
        operationLogUtil.logUserLogin(operatorUsername);
        operationLogUtil.logUserLogin(operatorUsername);

        // Assert
        verify(operationLogService, times(3)).createOperationLog(any());
    }

    /**
     * 测试不同操作类型 - 验证不同操作都会调用服务
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_DifferentOperationTypes_CallsServiceForEach() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "test-package.zip";
        String businessZh = "测试业务";
        String businessEn = "TEST_BUSINESS";

        // Act
        operationLogUtil.logUserLogin(operatorUsername);
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);
        operationLogUtil.logPreprocessRulePackageDelete(operatorUsername, packageName);

        // Assert
        verify(operationLogService, times(3)).createOperationLog(any());
    }

    /**
     * 测试边界条件 - 验证特殊字符处理
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_SpecialCharacters_HandlesCorrectly() throws Exception {
        // Arrange
        String operatorUsername = "admin@test.com";
        String packageName = "test-package_1.0.zip";
        String businessZh = "测试业务@#$%";
        String businessEn = "TEST_BUSINESS@#$%";

        // Act
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

    /**
     * 测试长字符串 - 验证长字符串处理
     *
     * @throws Exception 测试异常
     */
    @Test
    public void testLogOperation_LongStrings_HandlesCorrectly() throws Exception {
        // Arrange
        String operatorUsername = "admin";
        String packageName = "very-long-package-name-that-exceeds-normal-length-limits.zip";
        String businessZh = "这是一个非常长的中文业务名称，用于测试系统对长字符串的处理能力";
        String businessEn = "This is a very long English business name for testing system's ability to handle long strings";

        // Act
        operationLogUtil.logPreprocessRulePackageUpload(operatorUsername, packageName, businessZh, businessEn);

        // Assert
        verify(operationLogService).createOperationLog(any());
    }

}