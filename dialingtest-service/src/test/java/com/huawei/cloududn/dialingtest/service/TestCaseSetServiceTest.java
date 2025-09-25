/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.TestCaseSetDao;
import com.huawei.cloududn.dialingtest.dao.TestCaseDao;
import com.huawei.cloududn.dialingtest.dao.AppTypeDao;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCase;
import com.huawei.cloududn.dialingtest.model.TestCaseInfo;
import com.huawei.cloududn.dialingtest.entity.AppType;
import com.huawei.cloududn.dialingtest.model.UpdateTestCaseSetRequest;
import com.huawei.cloududn.dialingtest.service.ArchiveParseResult;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TestCaseSetService 单元测试
 * 
 * @author Generated
 * @since 2024-01-XX
 */
@RunWith(MockitoJUnitRunner.class)
public class TestCaseSetServiceTest {

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

    @Mock
    private MultipartFile mockFile;

    @InjectMocks
    private TestCaseSetService testCaseSetService;

    private TestCaseSet testTestCaseSet;
    private TestCase testTestCase;
    private TestCaseInfo testTestCaseInfo;
    private ArchiveParseResult mockArchiveResult;

    @Before
    public void setUp() {
        // 初始化测试数据
        testTestCaseSet = createTestTestCaseSet();
        testTestCase = createTestTestCase();
        testTestCaseInfo = createTestTestCaseInfo();
        mockArchiveResult = createMockArchiveResult();
    }

    /**
     * 测试上传用例集 - 成功场景
     */
    @Test
    public void testUploadTestCaseSet_Success_ReturnsTestCaseSet() throws Exception {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(testCaseSetDao.existsByNameAndVersion("testcaseset", "v1.0")).thenReturn(0);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(mockArchiveResult);
        when(excelParseService.parseExcel(any(byte[].class))).thenReturn(Arrays.asList(testTestCaseInfo));
        when(testCaseSetDao.insert(any(TestCaseSet.class))).thenReturn(1);
        when(testCaseDao.batchInsert(anyList())).thenReturn(1);
        when(appTypeDao.existsByBusinessCategoryAndAppName("VPN", "TestApp")).thenReturn(0);
        when(appTypeDao.insert(any(AppType.class))).thenReturn(1);

        // Act
        TestCaseSet result = testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);

        // Assert
        assertNotNull(result);
        assertEquals("testcaseset", result.getName());
        assertEquals("v1.0", result.getVersion());
        assertEquals(description, result.getDescription());
        assertEquals(businessZh, result.getBusinessZh());
        assertEquals("VPN_BLOCK", result.getBusinessEn());
        assertEquals(1024L, result.getFileSize().longValue());
        assertNotNull(result.getFileContent());
        assertNotNull(result.getSha256());

        verify(testCaseSetDao, times(1)).existsByNameAndVersion("testcaseset", "v1.0");
        verify(archiveParseService, times(1)).parseArchive(any(byte[].class));
        verify(excelParseService, times(1)).parseExcel(any(byte[].class));
        verify(testCaseSetDao, times(1)).insert(any(TestCaseSet.class));
        verify(testCaseDao, times(1)).batchInsert(anyList());
        verify(appTypeDao, times(1)).insert(any(AppType.class));
        verify(operationLogUtil, times(1)).logTestCaseSetUpload(eq("admin"), any(TestCaseSet.class));
    }

    /**
     * 测试上传用例集 - 覆盖模式
     */
    @Test
    public void testUploadTestCaseSet_WithOverwrite_ReturnsTestCaseSet() throws Exception {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = true;
        
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("test content".getBytes());
        when(testCaseSetDao.findByNameAndVersion("testcaseset", "v1.0")).thenReturn(testTestCaseSet);
        when(testCaseDao.deleteByTestCaseSetId(1L)).thenReturn(1);
        when(testCaseSetDao.deleteById(1L)).thenReturn(1);
        when(archiveParseService.parseArchive(any(byte[].class))).thenReturn(mockArchiveResult);
        when(excelParseService.parseExcel(any(byte[].class))).thenReturn(Arrays.asList(testTestCaseInfo));
        when(testCaseSetDao.insert(any(TestCaseSet.class))).thenReturn(1);
        when(testCaseDao.batchInsert(anyList())).thenReturn(1);
        when(appTypeDao.existsByBusinessCategoryAndAppName("VPN", "TestApp")).thenReturn(0);
        when(appTypeDao.insert(any(AppType.class))).thenReturn(1);

        // Act
        TestCaseSet result = testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);

        // Assert
        assertNotNull(result);
        verify(testCaseSetDao, times(1)).findByNameAndVersion("testcaseset", "v1.0");
        verify(testCaseDao, times(1)).deleteByTestCaseSetId(1L);
        verify(testCaseSetDao, times(1)).deleteById(1L);
    }

    /**
     * 测试上传用例集 - 文件为空
     */
    @Test
    public void testUploadTestCaseSet_EmptyFile_ThrowsIllegalArgumentException() {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.isEmpty()).thenReturn(true);

        // Act & Assert
        try {
            testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("文件不能为空", e.getMessage());
        }
    }

    /**
     * 测试上传用例集 - 文件过大
     */
    @Test
    public void testUploadTestCaseSet_FileTooLarge_ThrowsIllegalArgumentException() {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB

        // Act & Assert
        try {
            testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("文件大小不能超过100MB", e.getMessage());
        }
    }

    /**
     * 测试上传用例集 - 文件格式不支持
     */
    @Test
    public void testUploadTestCaseSet_UnsupportedFileFormat_ThrowsIllegalArgumentException() {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset.txt");

        // Act & Assert
        try {
            testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("只支持ZIP格式文件", e.getMessage());
        }
    }

    /**
     * 测试上传用例集 - 文件名格式错误
     */
    @Test
    public void testUploadTestCaseSet_InvalidFileNameFormat_ThrowsIllegalArgumentException() {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset.zip"); // 缺少版本号

        // Act & Assert
        try {
            testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("文件名格式错误，应为：用例集名称_版本号.zip", e.getMessage());
        }
    }

    /**
     * 测试上传用例集 - 重复用例集
     */
    @Test
    public void testUploadTestCaseSet_DuplicateTestCaseSet_ThrowsIllegalArgumentException() throws Exception {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(testCaseSetDao.existsByNameAndVersion("testcaseset", "v1.0")).thenReturn(1);

        // Act & Assert
        try {
            testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用例集名称和版本已存在: testcaseset_v1.0", e.getMessage());
        }
    }

    /**
     * 测试上传用例集 - 文件读取异常
     */
    @Test
    public void testUploadTestCaseSet_FileReadException_ThrowsRuntimeException() throws Exception {
        // Arrange
        String description = "测试用例集";
        String businessZh = "VPN阻断";
        boolean overwrite = false;
        
        when(mockFile.getOriginalFilename()).thenReturn("testcaseset_v1.0.zip");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenThrow(new RuntimeException("文件读取失败"));
        when(testCaseSetDao.existsByNameAndVersion("testcaseset", "v1.0")).thenReturn(0);

        // Act & Assert
        try {
            testCaseSetService.uploadTestCaseSet(mockFile, description, businessZh, overwrite);
            fail("应该抛出RuntimeException");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("上传用例集失败"));
            assertTrue(e.getMessage().contains("文件读取失败"));
        }
    }

    /**
     * 测试分页获取用例集列表 - 成功场景
     */
    @Test
    public void testGetTestCaseSets_Success_ReturnsResult() {
        // Arrange
        int page = 1;
        int pageSize = 10;
        List<TestCaseSet> testCaseSets = Arrays.asList(testTestCaseSet);
        long total = 1L;
        
        when(testCaseSetDao.findWithPagination(0, pageSize)).thenReturn(testCaseSets);
        when(testCaseSetDao.count()).thenReturn(total);

        // Act
        Map<String, Object> result = testCaseSetService.getTestCaseSets(page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(page, result.get("page"));
        assertEquals(pageSize, result.get("pageSize"));
        assertEquals(total, result.get("total"));
        assertEquals(testCaseSets, result.get("data"));

        verify(testCaseSetDao, times(1)).findWithPagination(0, pageSize);
        verify(testCaseSetDao, times(1)).count();
    }

    /**
     * 测试根据ID获取用例集 - 成功场景
     */
    @Test
    public void testGetTestCaseSetById_Success_ReturnsTestCaseSet() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetDao.findById(testId)).thenReturn(testTestCaseSet);

        // Act
        TestCaseSet result = testCaseSetService.getTestCaseSetById(testId);

        // Assert
        assertNotNull(result);
        assertEquals(testTestCaseSet.getId(), result.getId());
        assertEquals(testTestCaseSet.getName(), result.getName());

        verify(testCaseSetDao, times(1)).findById(testId);
    }

    /**
     * 测试根据ID获取用例集 - 用例集不存在
     */
    @Test
    public void testGetTestCaseSetById_TestCaseSetNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Long testId = 999L;
        when(testCaseSetDao.findById(testId)).thenReturn(null);

        // Act & Assert
        try {
            testCaseSetService.getTestCaseSetById(testId);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用例集不存在，ID: 999", e.getMessage());
        }
    }

    /**
     * 测试更新用例集 - 成功场景
     */
    @Test
    public void testUpdateTestCaseSet_Success_ReturnsUpdatedTestCaseSet() {
        // Arrange
        Long testId = 1L;
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        request.setDescription("更新后的描述");
        request.setBusinessZh("更新后的业务类型");
        request.setBusinessEn("UPDATED_BUSINESS");
        
        when(testCaseSetDao.findById(testId)).thenReturn(testTestCaseSet);
        when(testCaseSetDao.update(any(TestCaseSet.class))).thenReturn(1);

        // Act
        TestCaseSet result = testCaseSetService.updateTestCaseSet(testId, request);

        // Assert
        assertNotNull(result);
        assertEquals("更新后的描述", result.getDescription());
        assertEquals("更新后的业务类型", result.getBusinessZh());
        assertEquals("UPDATED_BUSINESS", result.getBusinessEn());

        verify(testCaseSetDao, times(1)).findById(testId);
        verify(testCaseSetDao, times(1)).update(any(TestCaseSet.class));
        verify(operationLogUtil, times(1)).logTestCaseSetUpdate(eq("admin"), any(TestCaseSet.class), any(TestCaseSet.class));
    }

    /**
     * 测试更新用例集 - 部分字段更新
     */
    @Test
    public void testUpdateTestCaseSet_PartialUpdate_ReturnsUpdatedTestCaseSet() {
        // Arrange
        Long testId = 1L;
        UpdateTestCaseSetRequest request = new UpdateTestCaseSetRequest();
        request.setDescription("更新后的描述");
        // businessZh 和 businessEn 为 null
        
        when(testCaseSetDao.findById(testId)).thenReturn(testTestCaseSet);
        when(testCaseSetDao.update(any(TestCaseSet.class))).thenReturn(1);

        // Act
        TestCaseSet result = testCaseSetService.updateTestCaseSet(testId, request);

        // Assert
        assertNotNull(result);
        assertEquals("更新后的描述", result.getDescription());
        // 其他字段应该保持不变
        assertEquals(testTestCaseSet.getBusinessZh(), result.getBusinessZh());
        assertEquals(testTestCaseSet.getBusinessEn(), result.getBusinessEn());
    }

    /**
     * 测试删除用例集 - 成功场景
     */
    @Test
    public void testDeleteTestCaseSet_Success_ReturnsTrue() {
        // Arrange
        Long testId = 1L;
        when(testCaseSetDao.findById(testId)).thenReturn(testTestCaseSet);
        when(testCaseDao.deleteByTestCaseSetId(testId)).thenReturn(1);
        when(testCaseSetDao.deleteById(testId)).thenReturn(1);

        // Act
        boolean result = testCaseSetService.deleteTestCaseSet(testId);

        // Assert
        assertTrue(result);
        verify(testCaseSetDao, times(1)).findById(testId);
        verify(testCaseDao, times(1)).deleteByTestCaseSetId(testId);
        verify(testCaseSetDao, times(1)).deleteById(testId);
        verify(operationLogUtil, times(1)).logTestCaseSetDelete(eq("admin"), any(TestCaseSet.class));
    }

    /**
     * 测试删除用例集 - 用例集不存在
     */
    @Test
    public void testDeleteTestCaseSet_TestCaseSetNotFound_ReturnsFalse() {
        // Arrange
        Long testId = 999L;
        when(testCaseSetDao.findById(testId)).thenReturn(null);

        // Act
        boolean result = testCaseSetService.deleteTestCaseSet(testId);

        // Assert
        assertFalse(result);
        verify(testCaseSetDao, times(1)).findById(testId);
        verify(testCaseDao, never()).deleteByTestCaseSetId(anyLong());
        verify(testCaseSetDao, never()).deleteById(anyLong());
    }

    /**
     * 测试分页获取测试用例列表 - 成功场景
     */
    @Test
    public void testGetTestCases_Success_ReturnsResult() {
        // Arrange
        Long testCaseSetId = 1L;
        int page = 1;
        int pageSize = 10;
        List<TestCase> testCases = Arrays.asList(testTestCase);
        long total = 1L;
        
        when(testCaseSetDao.findById(testCaseSetId)).thenReturn(testTestCaseSet);
        when(testCaseDao.findByTestCaseSetIdWithPagination(testCaseSetId, 0, pageSize)).thenReturn(testCases);
        when(testCaseDao.countByTestCaseSetId(testCaseSetId)).thenReturn(total);

        // Act
        Map<String, Object> result = testCaseSetService.getTestCases(testCaseSetId, page, pageSize);

        // Assert
        assertNotNull(result);
        assertEquals(page, result.get("page"));
        assertEquals(pageSize, result.get("pageSize"));
        assertEquals(total, result.get("total"));
        assertEquals(testCases, result.get("data"));

        verify(testCaseSetDao, times(1)).findById(testCaseSetId);
        verify(testCaseDao, times(1)).findByTestCaseSetIdWithPagination(testCaseSetId, 0, pageSize);
        verify(testCaseDao, times(1)).countByTestCaseSetId(testCaseSetId);
    }

    /**
     * 测试分页获取测试用例列表 - 用例集不存在
     */
    @Test
    public void testGetTestCases_TestCaseSetNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Long testCaseSetId = 999L;
        int page = 1;
        int pageSize = 10;
        
        when(testCaseSetDao.findById(testCaseSetId)).thenReturn(null);

        // Act & Assert
        try {
            testCaseSetService.getTestCases(testCaseSetId, page, pageSize);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用例集不存在，ID: 999", e.getMessage());
        }
    }

    /**
     * 测试获取缺失脚本列表 - 成功场景
     */
    @Test
    public void testGetMissingScripts_Success_ReturnsMissingScripts() {
        // Arrange
        Long testCaseSetId = 1L;
        List<TestCase> missingScripts = Arrays.asList(testTestCase);
        
        when(testCaseSetDao.findById(testCaseSetId)).thenReturn(testTestCaseSet);
        when(testCaseDao.findMissingScriptsByTestCaseSetId(testCaseSetId)).thenReturn(missingScripts);

        // Act
        List<TestCase> result = testCaseSetService.getMissingScripts(testCaseSetId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTestCase.getId(), result.get(0).getId());

        verify(testCaseSetDao, times(1)).findById(testCaseSetId);
        verify(testCaseDao, times(1)).findMissingScriptsByTestCaseSetId(testCaseSetId);
    }

    /**
     * 测试获取缺失脚本列表 - 用例集不存在
     */
    @Test
    public void testGetMissingScripts_TestCaseSetNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Long testCaseSetId = 999L;
        
        when(testCaseSetDao.findById(testCaseSetId)).thenReturn(null);

        // Act & Assert
        try {
            testCaseSetService.getMissingScripts(testCaseSetId);
            fail("应该抛出IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("用例集不存在，ID: 999", e.getMessage());
        }
    }

    // 辅助方法

    private TestCaseSet createTestTestCaseSet() {
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);
        testCaseSet.setName("testcaseset");
        testCaseSet.setVersion("v1.0");
        testCaseSet.setDescription("测试用例集");
        testCaseSet.setBusinessZh("VPN阻断");
        testCaseSet.setBusinessEn("VPN_BLOCK");
        testCaseSet.setFileSize(1024L);
        testCaseSet.setSha256("testsha256hash");
        testCaseSet.setFileContent("test content".getBytes());
        return testCaseSet;
    }

    private TestCase createTestTestCase() {
        TestCase testCase = new TestCase();
        testCase.setId(1L);
        testCase.setTestCaseSetId(1L);
        testCase.setCaseName("测试用例1");
        testCase.setCaseNumber("TC001");
        testCase.setTestSteps("测试步骤");
        testCase.setExpectedResult("预期结果");
        testCase.setBusinessCategory("VPN");
        testCase.setAppName("TestApp");
        testCase.setScriptExists(true);
        return testCase;
    }

    private TestCaseInfo createTestTestCaseInfo() {
        TestCaseInfo testCaseInfo = new TestCaseInfo();
        testCaseInfo.setCaseName("测试用例1");
        testCaseInfo.setCaseNumber("TC001");
        testCaseInfo.setTestSteps("测试步骤");
        testCaseInfo.setExpectedResult("预期结果");
        testCaseInfo.setBusinessCategory("VPN");
        testCaseInfo.setAppName("TestApp");
        testCaseInfo.setScriptExists(true);
        return testCaseInfo;
    }

    private ArchiveParseResult createMockArchiveResult() {
        ArchiveParseResult result = new ArchiveParseResult();
        result.setExcelData("excel data".getBytes());
        result.setScriptFileNames(Arrays.asList("TC001.py", "TC002.py"));
        return result;
    }
}
