package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.PreprocessRuleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预处理规则管理控制器测试类
 * 
 * @author g00940940
 * @since 2025-01-27
 */
@RunWith(MockitoJUnitRunner.class)
public class PreprocessRuleControllerTest {
    
    @Mock
    private PreprocessRuleService preprocessRuleService;
    
    @Mock
    private OperationLogUtil operationLogUtil;
    
    @InjectMocks
    private PreprocessRuleController preprocessRuleController;
    
    private PreprocessRulePackage testPackage;
    private PreprocessRule testRule;
    
    @Before
    public void setUp() {
        // 初始化测试数据
        testPackage = new PreprocessRulePackage();
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
    public void testPreprocessRulePackagesGet_Success() {
        // 准备测试数据
        PreprocessRulePackageListResponse response = new PreprocessRulePackageListResponse();
        response.setSuccess(true);
        response.setMessage("获取ZIP包列表成功");
        
        PreprocessRulePackageListResponseData data = new PreprocessRulePackageListResponseData();
        data.setPage(1);
        data.setPageSize(10);
        data.setTotal(1);
        data.setData(Arrays.asList(testPackage));
        response.setData(data);
        
        when(preprocessRuleService.getPreprocessRulePackages(1, 10, null, null))
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<PreprocessRulePackageListResponse> result = 
            preprocessRuleController.preprocessRulePackagesGet(1, 10, null, null);
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取ZIP包列表成功", result.getBody().getMessage());
        assertEquals(1, result.getBody().getData().getTotal().intValue());
    }
    
    @Test
    public void testPreprocessRulePackagesGet_Exception() {
        // 模拟异常
        when(preprocessRuleService.getPreprocessRulePackages(1, 10, null, null))
            .thenThrow(new RuntimeException("数据库连接失败"));
        
        // 执行测试
        ResponseEntity<PreprocessRulePackageListResponse> result = 
            preprocessRuleController.preprocessRulePackagesGet(1, 10, null, null);
        
        // 验证结果
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertTrue(result.getBody().getMessage().contains("获取ZIP包列表失败"));
    }
    
    @Test
    public void testPreprocessRulePackagesIdDelete_Success() {
        // 执行测试
        ResponseEntity<SuccessResponse> result = 
            preprocessRuleController.preprocessRulePackagesIdDelete(1L, "admin");
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("删除ZIP包成功", result.getBody().getMessage());
        
        // 验证服务方法被调用
        verify(preprocessRuleService).deletePreprocessRulePackage(1L, "admin");
    }
    
    @Test
    public void testPreprocessRulePackagesIdDelete_IllegalArgumentException() {
        // 模拟参数错误异常
        doThrow(new IllegalArgumentException("ZIP包不存在"))
            .when(preprocessRuleService).deletePreprocessRulePackage(1L, "admin");
        
        // 执行测试
        ResponseEntity<SuccessResponse> result = 
            preprocessRuleController.preprocessRulePackagesIdDelete(1L, "admin");
        
        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertNotNull(result.getBody());
        assertFalse(result.getBody().isSuccess());
        assertEquals("ZIP包不存在", result.getBody().getMessage());
    }
    
    @Test
    public void testPreprocessRulePackagesIdDownloadGet_Success() throws IOException {
        // 准备测试数据
        byte[] fileContent = "test zip content".getBytes();
        when(preprocessRuleService.downloadPreprocessRulePackage(1L))
            .thenReturn(fileContent);
        
        // 执行测试
        ResponseEntity<org.springframework.core.io.Resource> result = 
            preprocessRuleController.preprocessRulePackagesIdDownloadGet(1L);
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody() instanceof ByteArrayResource);
        assertEquals(fileContent.length, result.getBody().contentLength());
    }
    
    @Test
    public void testPreprocessRulePackagesIdDownloadGet_IllegalArgumentException() {
        // 模拟参数错误异常
        when(preprocessRuleService.downloadPreprocessRulePackage(1L))
            .thenThrow(new IllegalArgumentException("ZIP包不存在"));
        
        // 执行测试
        ResponseEntity<org.springframework.core.io.Resource> result = 
            preprocessRuleController.preprocessRulePackagesIdDownloadGet(1L);
        
        // 验证结果
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
    
    @Test
    public void testPreprocessRulesGet_Success() {
        // 准备测试数据
        PreprocessRuleListResponse response = new PreprocessRuleListResponse();
        response.setSuccess(true);
        response.setMessage("获取预处理规则列表成功");
        
        PreprocessRuleListResponseData data = new PreprocessRuleListResponseData();
        data.setPage(1);
        data.setPageSize(10);
        data.setTotal(1);
        data.setData(Arrays.asList(testRule));
        response.setData(data);
        
        when(preprocessRuleService.getPreprocessRules(1, 10, null, null, null, null, null))
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<PreprocessRuleListResponse> result = 
            preprocessRuleController.preprocessRulesGet(1, 10, null, null, null, null, null);
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取预处理规则列表成功", result.getBody().getMessage());
        assertEquals(1, result.getBody().getData().getTotal().intValue());
    }
    
    @Test
    public void testPreprocessRulesBusinessTypesGet_Success() {
        // 准备测试数据
        BusinessTypeListResponse response = new BusinessTypeListResponse();
        response.setSuccess(true);
        response.setMessage("获取业务类型列表成功");
        response.setData(Arrays.asList("直播业务", "视频业务"));
        
        when(preprocessRuleService.getAllBusinessTypes())
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<BusinessTypeListResponse> result = 
            preprocessRuleController.preprocessRulesBusinessTypesGet();
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取业务类型列表成功", result.getBody().getMessage());
        assertEquals(2, result.getBody().getData().size());
    }
    
    @Test
    public void testPreprocessRulesCategoriesGet_Success() {
        // 准备测试数据
        CategoryListResponse response = new CategoryListResponse();
        response.setSuccess(true);
        response.setMessage("获取分类列表成功");
        response.setData(Arrays.asList("LIVE", "VIDEO"));
        
        when(preprocessRuleService.getCategoriesByBusiness("直播业务"))
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<CategoryListResponse> result = 
            preprocessRuleController.preprocessRulesCategoriesGet("直播业务");
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取分类列表成功", result.getBody().getMessage());
        assertEquals(2, result.getBody().getData().size());
    }
    
    @Test
    public void testPreprocessRulesAppNamesGet_Success() {
        // 准备测试数据
        AppNameListResponse response = new AppNameListResponse();
        response.setSuccess(true);
        response.setMessage("获取应用名称列表成功");
        response.setData(Arrays.asList("douyin", "kuaishou"));
        
        when(preprocessRuleService.getAppNamesByBusinessAndCategory("直播业务", "LIVE"))
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<AppNameListResponse> result = 
            preprocessRuleController.preprocessRulesAppNamesGet("直播业务", "LIVE");
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取应用名称列表成功", result.getBody().getMessage());
        assertEquals(2, result.getBody().getData().size());
    }
    
    @Test
    public void testPreprocessRulesRuleNamesGet_Success() {
        // 准备测试数据
        RuleNameListResponse response = new RuleNameListResponse();
        response.setSuccess(true);
        response.setMessage("获取规则名称列表成功");
        response.setData(Arrays.asList("LIVE_douyin", "LIVE_kuaishou"));
        
        when(preprocessRuleService.getRuleNamesByBusinessAndCategoryAndApp("直播业务", "LIVE", "douyin"))
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<RuleNameListResponse> result = 
            preprocessRuleController.preprocessRulesRuleNamesGet("直播业务", "LIVE", "douyin");
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取规则名称列表成功", result.getBody().getMessage());
        assertEquals(2, result.getBody().getData().size());
    }
    
    @Test
    public void testPreprocessRulesFilterOptionsGet_Success() {
        // 准备测试数据
        FilterOptionsResponse response = new FilterOptionsResponse();
        response.setSuccess(true);
        response.setMessage("获取筛选选项成功");
        
        PreprocessRuleFilterOptions options = new PreprocessRuleFilterOptions();
        options.setBusinessTypes(Arrays.asList("直播业务", "视频业务"));
        options.setCategories(Arrays.asList("LIVE", "VIDEO"));
        options.setAppNames(Arrays.asList("douyin", "kuaishou"));
        options.setRuleNames(Arrays.asList("LIVE_douyin", "LIVE_kuaishou"));
        response.setData(options);
        
        when(preprocessRuleService.getFilterOptions())
            .thenReturn(response);
        
        // 执行测试
        ResponseEntity<FilterOptionsResponse> result = 
            preprocessRuleController.preprocessRulesFilterOptionsGet();
        
        // 验证结果
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isSuccess());
        assertEquals("获取筛选选项成功", result.getBody().getMessage());
        assertNotNull(result.getBody().getData());
        assertEquals(2, result.getBody().getData().getBusinessTypes().size());
    }
}

