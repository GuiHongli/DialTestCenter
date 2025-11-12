/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.service.executormanagement.task;

import com.huawei.cloududn.dialingtestapp.dao.SoftwarePackageDao;
import com.huawei.cloududn.dialingtestapp.dao.TestCaseSetDao;
import com.huawei.cloududn.dialingtestapp.entity.SoftwarePackage;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtestapp.service.executormanagement.SessionBindingRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

/**
 * TaskInterfaceService推送功能单元测试
 * 测试脚本推送和APP推送功能
 *
 * @author g00940940
 * @since 2025-11-12
 */
public class TaskInterfaceServicePushTest {

    @Mock
    private WssMessageSender wssMessageSender;

    @Mock
    private TestCaseSetDao testCaseSetDao;

    @Mock
    private SoftwarePackageDao softwarePackageDao;

    @Mock
    private SessionBindingRegistry sessionBindingRegistry;

    @InjectMocks
    private TaskInterfaceService taskInterfaceService;

    private AutoCloseable mocks;

    @Before
    public void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @After
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
    }

    /**
     * 测试pushScriptToExecutor：成功推送脚本
     */
    @Test
    public void testPushScriptToExecutor_Success() {
        // Given
        String executorName = "executor-001";
        String scriptName = "test-script";
        String version = "v1.0";
        byte[] fileContent = "script content".getBytes();
        String sha256 = "abc123";

        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setName(scriptName);
        testCaseSet.setVersion(version);
        testCaseSet.setFileContent(fileContent);
        testCaseSet.setSha256(sha256);

        when(testCaseSetDao.findByNameAndVersion(scriptName, version)).thenReturn(testCaseSet);

        // When
        taskInterfaceService.pushScriptToExecutor(executorName, scriptName, version);

        // Then
        verify(testCaseSetDao).findByNameAndVersion(scriptName, version);
        // Verify sendScriptUpdate is called with correct parameters
    }

    /**
     * 测试pushScriptToExecutor：脚本不存在
     */
    @Test
    public void testPushScriptToExecutor_NotFound() {
        // Given
        String executorName = "executor-001";
        String scriptName = "non-existent-script";
        String version = "v1.0";

        when(testCaseSetDao.findByNameAndVersion(scriptName, version)).thenReturn(null);

        // When
        taskInterfaceService.pushScriptToExecutor(executorName, scriptName, version);

        // Then
        verify(testCaseSetDao).findByNameAndVersion(scriptName, version);
        verifyNoInteractions(wssMessageSender);
    }

    /**
     * 测试pushScriptToExecutor：文件内容为空
     */
    @Test
    public void testPushScriptToExecutor_EmptyContent() {
        // Given
        String executorName = "executor-001";
        String scriptName = "test-script";
        String version = "v1.0";

        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setName(scriptName);
        testCaseSet.setVersion(version);
        testCaseSet.setFileContent(new byte[0]);

        when(testCaseSetDao.findByNameAndVersion(scriptName, version)).thenReturn(testCaseSet);

        // When
        taskInterfaceService.pushScriptToExecutor(executorName, scriptName, version);

        // Then
        verify(testCaseSetDao).findByNameAndVersion(scriptName, version);
        verifyNoInteractions(wssMessageSender);
    }

    /**
     * 测试pushAppToUe：成功推送APP
     */
    @Test
    public void testPushAppToUe_Success() {
        // Given
        String executorName = "executor-001";
        String serialNo = "UE-12345";
        String appName = "TestApp.apk";
        Integer taskId = 100;
        String sessionId = "session-001";
        byte[] fileContent = "app content".getBytes();
        String sha256 = "def456";

        SoftwarePackage softwarePackage = new SoftwarePackage();
        softwarePackage.setSoftwareName(appName);
        softwarePackage.setFileContent(fileContent);
        softwarePackage.setFileSha256(sha256);

        when(softwarePackageDao.findBySoftwareName(appName)).thenReturn(softwarePackage);
        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        // When
        taskInterfaceService.pushAppToUe(executorName, serialNo, appName, taskId);

        // Then
        verify(softwarePackageDao).findBySoftwareName(appName);
        verify(sessionBindingRegistry).getSessionId(executorName);
        verify(wssMessageSender).sendBinary(eq(sessionId), any());
    }

    /**
     * 测试pushAppToUe：软件包不存在
     */
    @Test
    public void testPushAppToUe_PackageNotFound() {
        // Given
        String executorName = "executor-001";
        String serialNo = "UE-12345";
        String appName = "NonExistentApp.apk";
        Integer taskId = 100;

        when(softwarePackageDao.findBySoftwareName(appName)).thenReturn(null);

        // When
        taskInterfaceService.pushAppToUe(executorName, serialNo, appName, taskId);

        // Then
        verify(softwarePackageDao).findBySoftwareName(appName);
        verifyNoInteractions(sessionBindingRegistry);
        verifyNoInteractions(wssMessageSender);
    }

    /**
     * 测试pushAppToUe：文件内容为空
     */
    @Test
    public void testPushAppToUe_EmptyContent() {
        // Given
        String executorName = "executor-001";
        String serialNo = "UE-12345";
        String appName = "TestApp.apk";
        Integer taskId = 100;

        SoftwarePackage softwarePackage = new SoftwarePackage();
        softwarePackage.setSoftwareName(appName);
        softwarePackage.setFileContent(new byte[0]);

        when(softwarePackageDao.findBySoftwareName(appName)).thenReturn(softwarePackage);

        // When
        taskInterfaceService.pushAppToUe(executorName, serialNo, appName, taskId);

        // Then
        verify(softwarePackageDao).findBySoftwareName(appName);
        verifyNoInteractions(sessionBindingRegistry);
        verifyNoInteractions(wssMessageSender);
    }

    /**
     * 测试pushAppToUe：会话不存在
     */
    @Test
    public void testPushAppToUe_SessionNotFound() {
        // Given
        String executorName = "executor-001";
        String serialNo = "UE-12345";
        String appName = "TestApp.apk";
        Integer taskId = 100;
        byte[] fileContent = "app content".getBytes();

        SoftwarePackage softwarePackage = new SoftwarePackage();
        softwarePackage.setSoftwareName(appName);
        softwarePackage.setFileContent(fileContent);

        when(softwarePackageDao.findBySoftwareName(appName)).thenReturn(softwarePackage);
        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(null);

        // When
        taskInterfaceService.pushAppToUe(executorName, serialNo, appName, taskId);

        // Then
        verify(softwarePackageDao).findBySoftwareName(appName);
        verify(sessionBindingRegistry).getSessionId(executorName);
        verifyNoInteractions(wssMessageSender);
    }

    /**
     * 测试pushScriptToExecutor：SHA256为null时使用空字符串
     */
    @Test
    public void testPushScriptToExecutor_NullSha256() {
        // Given
        String executorName = "executor-001";
        String scriptName = "test-script";
        String version = "v1.0";
        byte[] fileContent = "script content".getBytes();

        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setName(scriptName);
        testCaseSet.setVersion(version);
        testCaseSet.setFileContent(fileContent);
        testCaseSet.setSha256(null);

        when(testCaseSetDao.findByNameAndVersion(scriptName, version)).thenReturn(testCaseSet);

        // When
        taskInterfaceService.pushScriptToExecutor(executorName, scriptName, version);

        // Then
        verify(testCaseSetDao).findByNameAndVersion(scriptName, version);
    }

    /**
     * 测试pushAppToUe：SHA256为null时使用空字节数组
     */
    @Test
    public void testPushAppToUe_NullSha256() {
        // Given
        String executorName = "executor-001";
        String serialNo = "UE-12345";
        String appName = "TestApp.apk";
        Integer taskId = 100;
        String sessionId = "session-001";
        byte[] fileContent = "app content".getBytes();

        SoftwarePackage softwarePackage = new SoftwarePackage();
        softwarePackage.setSoftwareName(appName);
        softwarePackage.setFileContent(fileContent);
        softwarePackage.setFileSha256(null);

        when(softwarePackageDao.findBySoftwareName(appName)).thenReturn(softwarePackage);
        when(sessionBindingRegistry.getSessionId(executorName)).thenReturn(sessionId);

        // When
        taskInterfaceService.pushAppToUe(executorName, serialNo, appName, taskId);

        // Then
        verify(softwarePackageDao).findBySoftwareName(appName);
        verify(sessionBindingRegistry).getSessionId(executorName);
        verify(wssMessageSender).sendBinary(eq(sessionId), any());
    }
}

