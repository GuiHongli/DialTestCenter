/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.TestCaseSetsApi;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拨测用例集管理控制器
 * 
 * @author g00940940
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/api")
public class TestCaseSetController implements TestCaseSetsApi {
    
    @Autowired
    private TestCaseSetService testCaseSetService;
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    @Override
    public ResponseEntity<TestCaseSetListResponse> testCaseSetsGet(Integer page, Integer pageSize) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            // 调用服务层获取用例集列表
            Map<String, Object> result = testCaseSetService.getTestCaseSets(page, pageSize);
            
            // 构建响应
            TestCaseSetListResponse response = new TestCaseSetListResponse();
            response.setSuccess(true);
            response.setMessage("获取用例集列表成功");
            
            TestCaseSetListResponseData data = new TestCaseSetListResponseData();
            data.setPage((Integer) result.get("page"));
            data.setPageSize((Integer) result.get("pageSize"));
            data.setTotal(((Long) result.get("total")).intValue());
            data.setData((List<TestCaseSet>) result.get("data"));
            response.setData(data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            TestCaseSetListResponse response = new TestCaseSetListResponse();
            response.setSuccess(false);
            response.setMessage("获取用例集列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<TestCaseSetResponse> testCaseSetsIdGet(Long id) {
        try {
            TestCaseSet testCaseSet = testCaseSetService.getTestCaseSetById(id);
            if (testCaseSet == null) {
                TestCaseSetResponse response = new TestCaseSetResponse();
                response.setSuccess(false);
                response.setMessage("用例集不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            TestCaseSetResponse response = new TestCaseSetResponse();
            response.setSuccess(true);
            response.setMessage("获取用例集详情成功");
            response.setData(testCaseSet);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            TestCaseSetResponse response = new TestCaseSetResponse();
            response.setSuccess(false);
            response.setMessage("获取用例集详情失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    
    @Override
    public ResponseEntity<Resource> testCaseSetsIdDownloadGet(Long id) {
        try {
            TestCaseSet testCaseSet = testCaseSetService.getTestCaseSetById(id);
            if (testCaseSet == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            byte[] fileContent = testCaseSet.getFileContent();
            if (fileContent == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + testCaseSet.getName() + "_" + testCaseSet.getVersion() + ".zip\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TestCaseSetResponse> testCaseSetsIdPut(String xUsername, Long id, UpdateTestCaseSetRequest body) {
        try {
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "admin";
            TestCaseSet testCaseSet = testCaseSetService.updateTestCaseSet(id, body, operatorUsername);
            if (testCaseSet == null) {
                TestCaseSetResponse response = new TestCaseSetResponse();
                response.setSuccess(false);
                response.setMessage("用例集不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            TestCaseSetResponse response = new TestCaseSetResponse();
            response.setSuccess(true);
            response.setMessage("更新用例集成功");
            response.setData(testCaseSet);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            TestCaseSetResponse response = new TestCaseSetResponse();
            response.setSuccess(false);
            response.setMessage("更新失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            TestCaseSetResponse response = new TestCaseSetResponse();
            response.setSuccess(false);
            response.setMessage("更新失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<SuccessResponse> testCaseSetsIdDelete(Long id, String xUsername) {
        try {
            String operatorUsername = (xUsername != null && !xUsername.trim().isEmpty()) ? xUsername : "admin";
            boolean deleted = testCaseSetService.deleteTestCaseSet(id, operatorUsername);
            if (!deleted) {
                SuccessResponse response = new SuccessResponse();
                response.setSuccess(false);
                response.setMessage("用例集不存在");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(true);
            response.setMessage("删除用例集成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SuccessResponse response = new SuccessResponse();
            response.setSuccess(false);
            response.setMessage("删除失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<TestCaseListResponse> testCaseSetsIdTestCasesGet(Long id, Integer page, Integer pageSize) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            Map<String, Object> result = testCaseSetService.getTestCases(id, page, pageSize);
            
            TestCaseListResponse response = new TestCaseListResponse();
            response.setSuccess(true);
            response.setMessage("获取测试用例列表成功");
            
            TestCaseListResponseData data = new TestCaseListResponseData();
            data.setPage((Integer) result.get("page"));
            data.setPageSize((Integer) result.get("pageSize"));
            data.setTotal(((Long) result.get("total")).intValue());
            data.setData((List<TestCase>) result.get("data"));
            response.setData(data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            TestCaseListResponse response = new TestCaseListResponse();
            response.setSuccess(false);
            response.setMessage("获取测试用例列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<MissingScriptsResponse> testCaseSetsIdMissingScriptsGet(Long id) {
        try {
            List<TestCase> missingScripts = testCaseSetService.getMissingScripts(id);
            
            MissingScriptsResponse response = new MissingScriptsResponse();
            response.setSuccess(true);
            response.setMessage("获取缺失脚本列表成功");
            
            MissingScriptsResponseData data = new MissingScriptsResponseData();
            data.setTestCases(missingScripts);
            data.setCount(missingScripts.size());
            response.setData(data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            MissingScriptsResponse response = new MissingScriptsResponse();
            response.setSuccess(false);
            response.setMessage("获取缺失脚本列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}