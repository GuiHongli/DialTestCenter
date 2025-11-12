package com.huawei.cloududn.dialingtestapp.service;

import com.huawei.cloududn.dialingtestapp.dao.PreprocessRulePackageDao;
import com.huawei.cloududn.dialingtestapp.dao.PreprocessRuleDao;
import com.huawei.cloududn.dialingtestapp.entity.PreprocessRulePackageEntity;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtestapp.util.OperationLogUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预处理规则管理服务测试类
 * 
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class PreprocessRuleServiceTest {
    
    @Mock
    private PreprocessRulePackageDao preprocessRulePackageDao;
    
    @Mock
    private PreprocessRuleDao preprocessRuleDao;
    
    @Mock
    private OperationLogUtil operationLogUtil;
    
    @Mock
    private MultipartFile multipartFile;
    
    @InjectMocks
    private PreprocessRuleService preprocessRuleService;
    
    private PreprocessRulePackageEntity testPackage;
    private PreprocessRule testRule;
    
    @Before
    public void setUp() {
        // 初始化测试数据
        testPackage = new PreprocessRulePackageEntity();
        testPackage.setId(1L);
        testPackage.setPackageName("test_package.zip");
        testPackage.setBusinessZh("直播业务");
        testPackage.setBusinessEn("LIVE_STREAMING");
        testPackage.setFileSize(1024L);
        testPackage.setDescription("测试包");
        
        testRule = new PreprocessRule();
        testRule.setId(1L);
        testRule.setRuleName("LIVE_douyin");
        testRule.setBusinessZh("直播业务");
        testRule.setBusinessEn("LIVE_STREAMING");
        testRule.setCategory("LIVE");
        testRule.setAppName("douyin");
        testRule.setContent("{\"text\": \"douyin\"}");
        testRule.setIsCustom(false);
        testRule.setPackageId(1L);
    }
    
    @Test
    public void testGetPreprocessRulePackages_Success() {
        // 准备测试数据
        List<PreprocessRulePackageEntity> packages = Arrays.asList(testPackage);
        when(preprocessRulePackageDao.countByConditions(null, null)).thenReturn(1);
        when(preprocessRulePackageDao.findByConditions(null, null, 0, 10)).thenReturn(packages);
        
        // 执行测试
        PreprocessRulePackageListResponse result = 
            preprocessRuleService.getPreprocessRulePackages(1, 10, null, null);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取ZIP包列表成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getTotal().intValue());
        assertEquals(1, result.getData().getData().size());
        
        // 验证DAO方法被调用
        verify(preprocessRulePackageDao).countByConditions(null, null);
        verify(preprocessRulePackageDao).findByConditions(null, null, 0, 10);
    }
    
    @Test
    public void testGetPreprocessRulePackages_Exception() {
        // 模拟异常
        when(preprocessRulePackageDao.countByConditions(null, null))
            .thenThrow(new RuntimeException("数据库连接失败"));
        
        // 执行测试
        PreprocessRulePackageListResponse result = 
            preprocessRuleService.getPreprocessRulePackages(1, 10, null, null);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("获取ZIP包列表失败"));
    }
    
    @Test
    public void testDeletePreprocessRulePackage_Success() {
        // 准备测试数据
        when(preprocessRulePackageDao.findById(1L)).thenReturn(testPackage);
        
        // 执行测试
        preprocessRuleService.deletePreprocessRulePackage(1L, "admin");
        
        // 验证DAO方法被调用
        verify(preprocessRulePackageDao).findById(1L);
        verify(preprocessRulePackageDao).deleteById(1L);
        verify(operationLogUtil).logPreprocessRulePackageDelete("admin", "test_package.zip");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDeletePreprocessRulePackage_PackageNotFound() {
        // 模拟包不存在
        when(preprocessRulePackageDao.findById(1L)).thenReturn(null);
        
        // 执行测试
        preprocessRuleService.deletePreprocessRulePackage(1L, "admin");
    }
    
    @Test
    public void testDownloadPreprocessRulePackage_Success() {
        // 准备测试数据
        byte[] fileContent = "test zip content".getBytes();
        testPackage.setFileContent(fileContent);
        when(preprocessRulePackageDao.findById(1L)).thenReturn(testPackage);
        
        // 执行测试
        byte[] result = preprocessRuleService.downloadPreprocessRulePackage(1L);
        
        // 验证结果
        assertArrayEquals(fileContent, result);
        verify(preprocessRulePackageDao).findById(1L);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testDownloadPreprocessRulePackage_PackageNotFound() {
        // 模拟包不存在
        when(preprocessRulePackageDao.findById(1L)).thenReturn(null);
        
        // 执行测试
        preprocessRuleService.downloadPreprocessRulePackage(1L);
    }
    
    @Test
    public void testGetPreprocessRules_Success() {
        // 准备测试数据
        List<PreprocessRule> rules = Arrays.asList(testRule);
        when(preprocessRuleDao.countByConditions(null, null, null, null, null)).thenReturn(1);
        when(preprocessRuleDao.findByConditions(null, null, null, null, null, 0, 10)).thenReturn(rules);
        
        // 执行测试
        PreprocessRuleListResponse result = 
            preprocessRuleService.getPreprocessRules(1, 10, null, null, null, null, null);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取预处理规则列表成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(1, result.getData().getTotal().intValue());
        assertEquals(1, result.getData().getData().size());
        
        // 验证DAO方法被调用
        verify(preprocessRuleDao).countByConditions(null, null, null, null, null);
        verify(preprocessRuleDao).findByConditions(null, null, null, null, null, 0, 10);
    }
    
    @Test
    public void testGetAllBusinessTypes_Success() {
        // 准备测试数据
        List<String> businessTypes = Arrays.asList("直播业务", "视频业务");
        when(preprocessRuleDao.findDistinctBusinessTypes()).thenReturn(businessTypes);
        
        // 执行测试
        BusinessTypeListResponse result = preprocessRuleService.getAllBusinessTypes();
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取业务类型列表成功", result.getMessage());
        assertEquals(2, result.getData().size());
        assertEquals("直播业务", result.getData().get(0));
        
        // 验证DAO方法被调用
        verify(preprocessRuleDao).findDistinctBusinessTypes();
    }
    
    @Test
    public void testGetCategoriesByBusiness_Success() {
        // 准备测试数据
        List<String> categories = Arrays.asList("LIVE", "VIDEO");
        when(preprocessRuleDao.findDistinctCategoriesByBusiness("直播业务")).thenReturn(categories);
        
        // 执行测试
        CategoryListResponse result = preprocessRuleService.getCategoriesByBusiness("直播业务");
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取分类列表成功", result.getMessage());
        assertEquals(2, result.getData().size());
        assertEquals("LIVE", result.getData().get(0));
        
        // 验证DAO方法被调用
        verify(preprocessRuleDao).findDistinctCategoriesByBusiness("直播业务");
    }
    
    @Test
    public void testGetAppNamesByBusinessAndCategory_Success() {
        // 准备测试数据
        List<String> appNames = Arrays.asList("douyin", "kuaishou");
        when(preprocessRuleDao.findDistinctAppNamesByBusinessAndCategory("直播业务", "LIVE")).thenReturn(appNames);
        
        // 执行测试
        AppNameListResponse result = preprocessRuleService.getAppNamesByBusinessAndCategory("直播业务", "LIVE");
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取应用名称列表成功", result.getMessage());
        assertEquals(2, result.getData().size());
        assertEquals("douyin", result.getData().get(0));
        
        // 验证DAO方法被调用
        verify(preprocessRuleDao).findDistinctAppNamesByBusinessAndCategory("直播业务", "LIVE");
    }
    
    @Test
    public void testGetRuleNamesByBusinessAndCategoryAndApp_Success() {
        // 准备测试数据
        List<String> ruleNames = Arrays.asList("LIVE_douyin", "LIVE_kuaishou");
        when(preprocessRuleDao.findDistinctRuleNamesByBusinessAndCategoryAndApp("直播业务", "LIVE", "douyin")).thenReturn(ruleNames);
        
        // 执行测试
        RuleNameListResponse result = preprocessRuleService.getRuleNamesByBusinessAndCategoryAndApp("直播业务", "LIVE", "douyin");
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取规则名称列表成功", result.getMessage());
        assertEquals(2, result.getData().size());
        assertEquals("LIVE_douyin", result.getData().get(0));
        
        // 验证DAO方法被调用
        verify(preprocessRuleDao).findDistinctRuleNamesByBusinessAndCategoryAndApp("直播业务", "LIVE", "douyin");
    }
    
    @Test
    public void testGetFilterOptions_Success() {
        // 准备测试数据
        List<String> businessTypes = Arrays.asList("直播业务", "视频业务");
        List<String> categories = Arrays.asList("LIVE", "VIDEO");
        List<String> appNames = Arrays.asList("douyin", "kuaishou");
        List<String> ruleNames = Arrays.asList("LIVE_douyin", "LIVE_kuaishou");
        
        when(preprocessRuleDao.findDistinctBusinessTypes()).thenReturn(businessTypes);
        when(preprocessRuleDao.findDistinctCategories()).thenReturn(categories);
        when(preprocessRuleDao.findDistinctAppNames()).thenReturn(appNames);
        when(preprocessRuleDao.findDistinctRuleNames()).thenReturn(ruleNames);
        
        // 执行测试
        FilterOptionsResponse result = preprocessRuleService.getFilterOptions();
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("获取筛选选项成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().getBusinessTypes().size());
        assertEquals(2, result.getData().getCategories().size());
        assertEquals(2, result.getData().getAppNames().size());
        assertEquals(2, result.getData().getRuleNames().size());
        
        // 验证DAO方法被调用
        verify(preprocessRuleDao).findDistinctBusinessTypes();
        verify(preprocessRuleDao).findDistinctCategories();
        verify(preprocessRuleDao).findDistinctAppNames();
        verify(preprocessRuleDao).findDistinctRuleNames();
    }
    
    @Test
    public void testUploadPreprocessRulePackage_Success() throws IOException {
        // 准备测试数据
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn("test content".getBytes());
        when(multipartFile.getSize()).thenReturn(1024L);
        when(preprocessRulePackageDao.countByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(0);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        
        // 执行测试
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
        
        // 验证结果
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("预处理规则包上传成功"));
        assertTrue(result.contains("test_package.zip"));
        
        // 验证DAO方法被调用
        verify(preprocessRulePackageDao).findByPackageNameAndBusiness("test_package.zip", "直播业务");
        verify(preprocessRulePackageDao).insert(any(PreprocessRulePackageEntity.class));
        verify(operationLogUtil).logPreprocessRulePackageUpload("admin", "test_package.zip", "直播业务", "LIVE_STREAMING");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUploadPreprocessRulePackage_FileEmpty() throws IOException {
        // 模拟空文件
        when(multipartFile.isEmpty()).thenReturn(true);
        
        // 执行测试
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUploadPreprocessRulePackage_InvalidFileFormat() throws IOException {
        // 模拟非ZIP文件
        when(multipartFile.getOriginalFilename()).thenReturn("test.txt");
        when(multipartFile.isEmpty()).thenReturn(false);
        
        // 执行测试
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUploadPreprocessRulePackage_FileTooLarge() throws IOException {
        // 模拟文件过大
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(101L * 1024 * 1024); // 101MB
        
        // 执行测试
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testUploadPreprocessRulePackage_DuplicatePackage() throws IOException {
        // 模拟重复包名
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        PreprocessRulePackageEntity existingPackage = new PreprocessRulePackageEntity();
        existingPackage.setId(1L);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(existingPackage);
        
        // 执行测试（不覆盖）
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    /**
     * 测试uploadPreprocessRulePackage - 解析包含apps的JSON文件
     */
    @Test
    public void testUploadPreprocessRulePackage_ParseJsonWithApps() throws IOException {
        // Arrange
        String jsonContent = "{\"category\":\"LIVE\",\"apps\":{\"douyin\":{\"text\":\"douyin\"},\"kuaishou\":{\"text\":\"kuaishou\"}}}";
        byte[] zipContent = createZipFile("test.json", jsonContent.getBytes("UTF-8"));
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class))).thenReturn(1);
        
        // Act
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
        
        // Assert
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("预处理规则包上传成功"));
        assertTrue(result.contains("\"rulesCount\":2"));
        
        // 验证规则被正确插入（2个规则：douyin和kuaishou）
        verify(preprocessRuleDao, times(2)).insert(any(PreprocessRule.class));
    }
    
    /**
     * 测试uploadPreprocessRulePackage - 解析包含custom_rules的JSON文件
     */
    @Test
    public void testUploadPreprocessRulePackage_ParseJsonWithCustomRules() throws IOException {
        // Arrange
        String jsonContent = "{\"category\":\"VIDEO\",\"custom_rules\":{\"rule1\":{\"app1\":{\"text\":\"app1\"},\"app2\":{\"text\":\"app2\"}}}}";
        byte[] zipContent = createZipFile("test.json", jsonContent.getBytes("UTF-8"));
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "视频业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class))).thenReturn(1);
        
        // Act
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "视频业务", "VIDEO_STREAMING", "测试包", "admin", false);
        
        // Assert
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"rulesCount\":2"));
        
        // 验证规则被正确插入（2个自定义规则）
        verify(preprocessRuleDao, times(2)).insert(any(PreprocessRule.class));
    }
    
    /**
     * 测试uploadPreprocessRulePackage - 解析同时包含apps和custom_rules的JSON文件
     */
    @Test
    public void testUploadPreprocessRulePackage_ParseJsonWithAppsAndCustomRules() throws IOException {
        // Arrange
        String jsonContent = "{\"category\":\"LIVE\",\"apps\":{\"douyin\":{\"text\":\"douyin\"}},\"custom_rules\":{\"rule1\":{\"app1\":{\"text\":\"app1\"}}}}";
        byte[] zipContent = createZipFile("test.json", jsonContent.getBytes("UTF-8"));
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class))).thenReturn(1);
        
        // Act
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
        
        // Assert
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"rulesCount\":2"));
        
        // 验证规则被正确插入（1个apps规则 + 1个custom_rules规则）
        verify(preprocessRuleDao, times(2)).insert(any(PreprocessRule.class));
    }
    
    /**
     * 测试uploadPreprocessRulePackage - JSON文件缺少category字段
     */
    @Test(expected = IOException.class)
    public void testUploadPreprocessRulePackage_JsonMissingCategory() throws IOException {
        // Arrange
        String jsonContent = "{\"apps\":{\"douyin\":{\"text\":\"douyin\"}}}";
        byte[] zipContent = createZipFile("test.json", jsonContent.getBytes("UTF-8"));
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        
        // Act
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    /**
     * 测试uploadPreprocessRulePackage - ZIP文件包含多个JSON文件
     */
    @Test
    public void testUploadPreprocessRulePackage_MultipleJsonFiles() throws IOException {
        // Arrange
        String jsonContent1 = "{\"category\":\"LIVE\",\"apps\":{\"douyin\":{\"text\":\"douyin\"}}}";
        String jsonContent2 = "{\"category\":\"VIDEO\",\"apps\":{\"app1\":{\"text\":\"app1\"}}}";
        byte[] zipContent = createZipFileWithMultipleEntries(
            new String[]{"file1.json", "file2.json"},
            new byte[][]{jsonContent1.getBytes("UTF-8"), jsonContent2.getBytes("UTF-8")});
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class))).thenReturn(1);
        
        // Act
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
        
        // Assert
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"rulesCount\":2"));
        
        // 验证规则被正确插入（2个JSON文件，每个1个规则）
        verify(preprocessRuleDao, times(2)).insert(any(PreprocessRule.class));
    }
    
    /**
     * 测试uploadPreprocessRulePackage - ZIP文件包含目录和非JSON文件
     */
    @Test
    public void testUploadPreprocessRulePackage_ZipWithDirectoryAndNonJsonFiles() throws IOException {
        // Arrange
        String jsonContent = "{\"category\":\"LIVE\",\"apps\":{\"douyin\":{\"text\":\"douyin\"}}}";
        byte[] zipContent = createZipFileWithMultipleEntries(
            new String[]{"folder/", "test.json", "readme.txt"},
            new byte[][]{null, jsonContent.getBytes("UTF-8"), "This is a readme".getBytes("UTF-8")});
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class))).thenReturn(1);
        
        // Act
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
        
        // Assert
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"rulesCount\":1"));
        
        // 验证只有JSON文件被处理
        verify(preprocessRuleDao, times(1)).insert(any(PreprocessRule.class));
    }
    
    /**
     * 测试uploadPreprocessRulePackage - 强制覆盖模式
     */
    @Test
    public void testUploadPreprocessRulePackage_ForceOverwrite() throws IOException {
        // Arrange
        String jsonContent = "{\"category\":\"LIVE\",\"apps\":{\"douyin\":{\"text\":\"douyin\"}}}";
        byte[] zipContent = createZipFile("test.json", jsonContent.getBytes("UTF-8"));
        
        PreprocessRulePackageEntity existingPackage = new PreprocessRulePackageEntity();
        existingPackage.setId(1L);
        existingPackage.setPackageName("test_package.zip");
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(existingPackage);
        when(preprocessRulePackageDao.update(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.deleteByPackageId(1L)).thenReturn(1);
        when(preprocessRuleDao.deleteByRuleNameAndBusiness("LIVE-douyin", "直播业务")).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class))).thenReturn(1);
        
        // Act
        String result = preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", true);
        
        // Assert
        assertTrue(result.contains("\"success\":true"));
        assertTrue(result.contains("\"rulesCount\":1"));
        
        // 验证删除和插入操作
        verify(preprocessRuleDao, times(1)).deleteByPackageId(1L);
        verify(preprocessRuleDao, times(1)).deleteByRuleNameAndBusiness("LIVE-douyin", "直播业务");
        verify(preprocessRuleDao, times(1)).insert(any(PreprocessRule.class));
    }
    
    /**
     * 测试uploadPreprocessRulePackage - 重复规则异常（DuplicateKeyException）
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUploadPreprocessRulePackage_DuplicateRuleException() throws IOException {
        // Arrange
        String jsonContent = "{\"category\":\"LIVE\",\"apps\":{\"douyin\":{\"text\":\"douyin\"}}}";
        byte[] zipContent = createZipFile("test.json", jsonContent.getBytes("UTF-8"));
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        when(preprocessRuleDao.insert(any(PreprocessRule.class)))
            .thenThrow(new DuplicateKeyException("uk_rule_name_business violation"));
        
        // Act
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    /**
     * 测试uploadPreprocessRulePackage - 无效的JSON格式
     */
    @Test(expected = IOException.class)
    public void testUploadPreprocessRulePackage_InvalidJsonFormat() throws IOException {
        // Arrange
        String invalidJson = "{\"category\":\"LIVE\",\"apps\":{invalid}}";
        byte[] zipContent = createZipFile("test.json", invalidJson.getBytes("UTF-8"));
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(zipContent);
        when(multipartFile.getSize()).thenReturn((long) zipContent.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        
        // Act
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    /**
     * 测试uploadPreprocessRulePackage - ZIP文件为空
     */
    @Test(expected = IOException.class)
    public void testUploadPreprocessRulePackage_EmptyZipFile() throws IOException {
        // Arrange
        byte[] emptyZip = new byte[0];
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(emptyZip);
        when(multipartFile.getSize()).thenReturn(0L);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        
        // Act
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    /**
     * 测试uploadPreprocessRulePackage - ZIP文件解析异常
     */
    @Test(expected = IOException.class)
    public void testUploadPreprocessRulePackage_InvalidZipFormat() throws IOException {
        // Arrange
        byte[] invalidZip = "This is not a zip file".getBytes("UTF-8");
        
        when(multipartFile.getOriginalFilename()).thenReturn("test_package.zip");
        when(multipartFile.getBytes()).thenReturn(invalidZip);
        when(multipartFile.getSize()).thenReturn((long) invalidZip.length);
        when(preprocessRulePackageDao.findByPackageNameAndBusiness("test_package.zip", "直播业务")).thenReturn(null);
        when(preprocessRulePackageDao.insert(any(PreprocessRulePackageEntity.class))).thenReturn(1);
        
        // Act
        preprocessRuleService.uploadPreprocessRulePackage(
            multipartFile, "直播业务", "LIVE_STREAMING", "测试包", "admin", false);
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 创建包含单个文件的ZIP字节数组
     *
     * @param fileName 文件名
     * @param fileContent 文件内容
     * @return ZIP文件字节数组
     * @throws IOException IO异常
     */
    private byte[] createZipFile(String fileName, byte[] fileContent) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write(fileContent);
            zos.closeEntry();
        }
        return baos.toByteArray();
    }
    
    /**
     * 创建包含多个文件的ZIP字节数组
     *
     * @param fileNames 文件名数组
     * @param fileContents 文件内容数组
     * @return ZIP文件字节数组
     * @throws IOException IO异常
     */
    private byte[] createZipFileWithMultipleEntries(String[] fileNames, byte[][] fileContents) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < fileNames.length; i++) {
                ZipEntry entry = new ZipEntry(fileNames[i]);
                zos.putNextEntry(entry);
                if (fileContents[i] != null) {
                    zos.write(fileContents[i]);
                }
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}

