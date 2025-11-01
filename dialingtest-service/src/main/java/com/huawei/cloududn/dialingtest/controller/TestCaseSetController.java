/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.TestCaseSetsApi;
import com.huawei.cloududn.dialingtest.entity.ValidationTask;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.TestCaseSetService;
import com.huawei.cloududn.dialingtest.service.TestCaseValidationService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import com.huawei.cloududn.dialingtest.util.PermissionValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 拨测用例集管理控制器
 * 
 * @author g00940940
 * @since 2025-09-23
 */
@RestController
@RequestMapping("/api")
public class TestCaseSetController implements TestCaseSetsApi {
    
    private static final Logger logger = LoggerFactory.getLogger(TestCaseSetController.class);
    
    @Autowired
    private TestCaseSetService testCaseSetService;
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    @Autowired
    private TestCaseValidationService testCaseValidationService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private PermissionValidator permissionValidator;
    
    @Override
    public ResponseEntity<TestCaseSetListResponse> getTestCaseSets(Integer page, Integer pageSize) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            TestCaseSetListResponseData data = testCaseSetService.getTestCaseSets(page, pageSize);
            
            TestCaseSetListResponse response = new TestCaseSetListResponse();
            response.setSuccess(true);
            response.setMessage("获取用例集列表成功");
            response.setData(data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            TestCaseSetListResponse errorResponse = new TestCaseSetListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取用例集列表失败: " + e.getMessage());
            errorResponse.setData(null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Override
    public ResponseEntity<TestCaseSetResponse> getTestCaseSetById(Long id) {
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
    public ResponseEntity<Resource> downloadTestCaseSet(Long id, @RequestHeader(value = "X-Username", required = true) String xUsername) {
        try {
            // 检查权限（需要ADMIN或OPERATOR权限）
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "下载用例集");
            if (!permissionResult.isValid()) {
                logger.warn("用户权限不足 - username: {}", xUsername);
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).build();
            }
            
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
            
            logger.info("User {} downloaded test case set: {}", xUsername, id);
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(fileContent.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to download test case set: testCaseSetId={}, username={}", id, xUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<TestCaseSetResponse> updateTestCaseSet(String xUsername, Long id, UpdateTestCaseSetRequest body) {
        try {
            // 检查权限（需要ADMIN或OPERATOR权限）
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "更新用例集");
            if (!permissionResult.isValid()) {
                TestCaseSetResponse response = new TestCaseSetResponse();
                response.setSuccess(false);
                response.setMessage(permissionResult.getErrorMessage());
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).body(response);
            }
            
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
    public ResponseEntity<SuccessResponse> deleteTestCaseSet(Long id, String xUsername) {
        try {
            // 检查权限（需要ADMIN或OPERATOR权限）
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "删除用例集");
            if (!permissionResult.isValid()) {
                SuccessResponse response = new SuccessResponse();
                response.setSuccess(false);
                response.setMessage(permissionResult.getErrorMessage());
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).body(response);
            }
            
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
    public ResponseEntity<TestCaseListResponse> getTestCasesByTestCaseSetId(Long id, Integer page, Integer pageSize) {
        try {
            // 设置默认值
            if (page == null) page = 1;
            if (pageSize == null) pageSize = 10;
            
            TestCaseListResponseData data = testCaseSetService.getTestCases(id, page, pageSize);
            
            TestCaseListResponse response = new TestCaseListResponse();
            response.setSuccess(true);
            response.setMessage("获取测试用例列表成功");
            response.setData(data);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            TestCaseListResponse errorResponse = new TestCaseListResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取测试用例列表失败: " + e.getMessage());
            errorResponse.setData(null);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    @Override
    public ResponseEntity<MissingScriptsResponse> getMissingScriptsByTestCaseSetId(Long id) {
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

    /**
     * 触发用例集校验任务
     *
     * @param id 用例集ID
     * @param xCsrfToken CSRF防护令牌
     * @param xUsername 操作用户名
     * @return 校验任务响应
     */
    @Override
    public ResponseEntity<ValidationTaskResponse> triggerTestCaseSetValidation(Long id, String xCsrfToken, String xUsername) {
        logger.info("触发用例集校验任务 - testCaseSetId: {}, username: {}", id, xUsername);
        try {
            // 检查用户权限（ADMIN或OPERATOR）
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "触发用例集校验");
            if (!permissionResult.isValid()) {
                logger.warn("用户权限不足 - username: {}", xUsername);
                ValidationTaskResponse response = new ValidationTaskResponse();
                response.setSuccess(false);
                response.setMessage(permissionResult.getErrorMessage());
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).body(response);
            }
            
            // 触发校验任务
            logger.debug("开始调用 triggerValidation - testCaseSetId: {}", id);
            TestCaseValidationService.ValidationTaskInfo taskInfo = testCaseValidationService.triggerValidation(id);
            logger.info("校验任务已创建 - taskId: {}, testCaseSetId: {}", taskInfo.getTaskId(), id);
            
            // 获取用例集信息用于记录操作日志
            TestCaseSet testCaseSet = testCaseSetService.getTestCaseSetById(id);
            if (testCaseSet != null) {
                operationLogUtil.logTestCaseSetValidation(xUsername, testCaseSet);
            }
            
            // 返回202 Accepted表示任务已提交
            ValidationTaskResponse response = new ValidationTaskResponse();
            response.setSuccess(true);
            response.setMessage("校验任务已提交");
            
            ValidationTaskResponseData data = new ValidationTaskResponseData();
            data.setTaskId(taskInfo.getTaskId());
            data.setTestCaseSetId(taskInfo.getTestCaseSetId());
            data.setStatus(taskInfo.getStatus());
            data.setEstimatedTime(taskInfo.getEstimatedTime());
            response.setData(data);
            
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.error("触发校验任务失败 - testCaseSetId: {}, 错误: {}", id, e.getMessage(), e);
            ValidationTaskResponse response = new ValidationTaskResponse();
            response.setSuccess(false);
            response.setMessage("用例集不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("触发校验任务失败 - testCaseSetId: {}, username: {}, 异常类型: {}, 错误信息: {}", 
                    id, xUsername, e.getClass().getName(), e.getMessage(), e);
            ValidationTaskResponse response = new ValidationTaskResponse();
            response.setSuccess(false);
            response.setMessage("触发校验任务失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取用例集校验结果
     *
     * @param id 用例集ID
     * @param xCsrfToken CSRF防护令牌（可选）
     * @param forceRefresh 是否强制刷新
     * @return 校验结果响应
     */
    @Override
    public ResponseEntity<ValidationResponse> getTestCaseSetValidation(Long id, String xCsrfToken, Boolean forceRefresh) {
        try {
            com.huawei.cloududn.dialingtest.model.ValidationResult serviceResult = null;
            
            // 如果强制刷新，重新执行校验（同步）
            if (forceRefresh != null && forceRefresh) {
                serviceResult = testCaseValidationService.validateTestCaseSet(id);
            } else {
                // 优先从缓存读取
                serviceResult = testCaseValidationService.getValidationResult(id);
            }
            
            // 如果结果为空，查询任务状态
            if (serviceResult == null) {
                ValidationTask task = testCaseValidationService.getTaskStatus(id);
                if (task == null) {
                    ValidationResponse response = new ValidationResponse();
                    response.setSuccess(false);
                    response.setMessage("校验结果不存在，请先触发校验");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                }
                
                // 任务正在执行中
                ValidationResponse response = new ValidationResponse();
                response.setSuccess(true);
                response.setMessage("校验任务正在执行中，状态: " + task.getStatus());
                return ResponseEntity.ok(response);
            }
            
            // 返回完整校验结果
            ValidationResponse response = new ValidationResponse();
            response.setSuccess(true);
            response.setMessage("获取校验结果成功");
            
            ValidationResponseData data = new ValidationResponseData();
            data.setTestCaseSetId(serviceResult.getTestCaseSetId());
            data.setTestCaseSetName(serviceResult.getTestCaseSetName());
            data.setTestCaseSetVersion(serviceResult.getTestCaseSetVersion());
            data.setBusinessZh(serviceResult.getBusinessZh());
            data.setBusinessEn(serviceResult.getBusinessEn());
            data.setTotalCaseCount(serviceResult.getTotalCaseCount());
            data.setPassedCaseCount(serviceResult.getPassedCaseCount());
            data.setFailedCaseCount(serviceResult.getFailedCaseCount());
            data.setMatchRate(serviceResult.getMatchRate());
            
            // 获取任务状态信息
            ValidationTask task = testCaseValidationService.getTaskStatus(id);
            if (task != null) {
                data.setValidationTaskStatus(task.getStatus());
                if (task.getCreatedTime() != null) {
                    data.setValidationTaskCreatedTime(task.getCreatedTime().toString());
                }
                if (task.getStartedTime() != null) {
                    data.setValidationTaskStartedTime(task.getStartedTime().toString());
                }
                if (task.getCompletedTime() != null) {
                    data.setValidationTaskCompletedTime(task.getCompletedTime().toString());
                }
            }
            
            List<CaseValidationResult> caseResults = new ArrayList<>();
            for (com.huawei.cloududn.dialingtest.model.CaseValidationResult caseResult : 
                    serviceResult.getCaseResults()) {
                CaseValidationResult apiCaseResult = new CaseValidationResult();
                apiCaseResult.setCaseNumber(caseResult.getCaseNumber());
                apiCaseResult.setCaseName(caseResult.getCaseName());
                apiCaseResult.setBusinessCategory(caseResult.getBusinessCategory());
                apiCaseResult.setAppName(caseResult.getAppName());
                apiCaseResult.setScriptMatchValid(caseResult.isScriptMatchValid());
                apiCaseResult.setPreprocessRuleValid(caseResult.isPreprocessRuleValid());
                apiCaseResult.setSoftwarePackageValid(caseResult.isSoftwarePackageValid());
                apiCaseResult.setOverallValid(caseResult.isOverallValid());
                apiCaseResult.setValidReasons(caseResult.getValidReasons());
                apiCaseResult.setInvalidReasons(caseResult.getInvalidReasons());
                caseResults.add(apiCaseResult);
            }
            data.setCaseResults(caseResults);
            
            response.setData(data);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ValidationResponse response = new ValidationResponse();
            response.setSuccess(false);
            response.setMessage("用例集不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            ValidationResponse response = new ValidationResponse();
            response.setSuccess(false);
            response.setMessage("获取校验结果失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 导出用例集校验结果Excel
     *
     * @param id 用例集ID
     * @param xCsrfToken CSRF防护令牌（可选）
     * @param xUsername 操作用户名
     * @return Excel文件资源
     */
    @Override
    public ResponseEntity<Resource> exportTestCaseSetValidation(Long id, String xCsrfToken, @RequestHeader(value = "X-Username", required = true) String xUsername) {
        try {
            // 检查权限（需要ADMIN或OPERATOR权限）
            PermissionValidator.PermissionValidationResult permissionResult = permissionValidator.checkAdminOrOperator(xUsername, "导出用例集校验结果");
            if (!permissionResult.isValid()) {
                logger.warn("用户权限不足 - username: {}", xUsername);
                HttpStatus status = permissionResult.getErrorMessage().contains("未提供用户名") 
                    ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
                return ResponseEntity.status(status).build();
            }
            
            // 获取校验结果
            com.huawei.cloududn.dialingtest.model.ValidationResult serviceResult = 
                testCaseValidationService.getValidationResult(id);
            
            if (serviceResult == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // 生成Excel文件
            Resource resource = com.huawei.cloududn.dialingtest.util.ExcelUtil.generateValidationResultExcel(serviceResult);
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 生成文件名：用例集名称_校验结果_当前时间戳.xlsx
            String testCaseSetName = serviceResult.getTestCaseSetName() != null ? serviceResult.getTestCaseSetName() : "用例集";
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("%s_校验结果_%s.xlsx", testCaseSetName, timestamp);
            headers.setContentDispositionFormData("attachment", filename);
            
            logger.info("User {} exported validation result Excel for test case set: {}", xUsername, id);
            return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
                
        } catch (IllegalArgumentException e) {
            logger.error("Test case set not found: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Failed to export validation result Excel: testCaseSetId={}, username={}", id, xUsername, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}