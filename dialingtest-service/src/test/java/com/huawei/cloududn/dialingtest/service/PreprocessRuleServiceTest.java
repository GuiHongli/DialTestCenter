package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.PreprocessRulePackageDao;
import com.huawei.cloududn.dialingtest.dao.PreprocessRuleDao;
import com.huawei.cloududn.dialingtest.entity.PreprocessRulePackageEntity;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
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
}

