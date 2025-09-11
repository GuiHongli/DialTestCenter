/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.TestCase;
import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.mapper.TestCaseSetMapper;
import com.huawei.dialtest.center.service.ArchiveParseService.ArchiveValidationResult;
import com.huawei.dialtest.center.service.ExcelParseService.TestCaseInfo;
import com.huawei.dialtest.center.service.ScriptMatchService.ScriptMatchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用例集服务类，提供用例集的业务逻辑处理
 * 包括上传、下载、删除、查询等操作
 * 支持ZIP和TAR.GZ格式的文件处理
 *
 * @author g00940940
 * @since 2025-09-06
 */
@Service
public class TestCaseSetService {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseSetService.class);

    @Autowired
    private TestCaseSetMapper testCaseSetMapper;

    @Autowired
    private TestCaseService testCaseService;

    @Autowired
    private ArchiveParseService archiveParseService;

    @Autowired
    private ExcelParseService excelParseService;

    @Autowired
    private ScriptMatchService scriptMatchService;

    /**
     * 获取用例集列表（分页）
     *
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @return 用例集分页数据
     */
    public Page<TestCaseSet> getTestCaseSets(int page, int pageSize) {
        logger.debug("Getting test case sets - page: {}, size: {}", page, pageSize);
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        List<TestCaseSet> content = testCaseSetMapper.findAllByOrderByCreatedTimeDesc(page - 1, pageSize);
        long total = testCaseSetMapper.count();
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 根据ID获取用例集
     *
     * @param id 用例集ID
     * @return 用例集对象，如果不存在则返回空
     */
    public Optional<TestCaseSet> getTestCaseSetById(Long id) {
        logger.debug("Getting test case set by ID: {}", id);
        TestCaseSet testCaseSet = testCaseSetMapper.findById(id);
        return Optional.ofNullable(testCaseSet);
    }

    /**
     * 上传用例集
     *
     * @param file 用例集文件，支持.zip和.tar.gz格式
     * @param description 用例集描述信息
     * @param creator 创建者用户名
     * @param business 业务类型
     * @return 保存后的用例集实体对象
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    @Transactional
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String creator, String business) throws IOException {
        logger.info("Starting test case set upload: {}", file.getOriginalFilename());

        // 验证文件
        validateFile(file);

        // 解析文件名和格式
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String fileFormat;
        String nameWithoutExt;

        if (fileName.toLowerCase().endsWith(".tar.gz")) {
            fileFormat = "tar.gz";
            nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".tar.gz"));
        } else if (fileName.toLowerCase().endsWith(".zip")) {
            fileFormat = "zip";
            nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".zip"));
        } else {
            throw new IllegalArgumentException("Only ZIP and TAR.GZ format files are supported");
        }

        int lastUnderscoreIndex = nameWithoutExt.lastIndexOf("_");
        if (lastUnderscoreIndex == -1) {
            throw new IllegalArgumentException("Invalid file name format, should be: testcase_name_version." + fileFormat);
        }

        String name = nameWithoutExt.substring(0, lastUnderscoreIndex);
        String version = nameWithoutExt.substring(lastUnderscoreIndex + 1);

        // 检查是否已存在
        if (testCaseSetMapper.existsByNameAndVersion(name, version)) {
            throw new IllegalArgumentException("Test case set with name and version already exists");
        }

        // 读取文件内容
        byte[] fileContent = file.getBytes();

        // 验证压缩包结构
        ArchiveValidationResult validationResult = archiveParseService.validateArchive(fileContent, fileFormat);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Invalid archive structure: missing cases.xlsx or scripts directory");
        }

        // 计算文件内容的SHA512哈希值
        String sha512 = calculateSHA512(fileContent);

        // 创建用例集记录
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setName(name);
        testCaseSet.setVersion(version);
        testCaseSet.setFileContent(fileContent);
        testCaseSet.setFileFormat(fileFormat);
        testCaseSet.setCreator(creator);
        testCaseSet.setFileSize(file.getSize());
        testCaseSet.setSha512(sha512);
        testCaseSet.setBusiness(business);
        testCaseSet.setDescription(description);

        int result = testCaseSetMapper.insert(testCaseSet);
        if (result > 0) {
            logger.info("Test case set uploaded successfully: {} - {}, format: {}, file size: {} bytes, SHA512: {}, business: {}", name, version, fileFormat, fileContent.length, sha512, business);

            // 解析并存储用例信息
            parseAndStoreTestCases(testCaseSet, fileContent, fileFormat);

            return testCaseSet;
        } else {
            throw new RuntimeException("Failed to save test case set");
        }
    }

    /**
     * 删除用例集
     *
     * @param id 用例集ID
     * @throws IllegalArgumentException 当用例集不存在时抛出
     */
    public void deleteTestCaseSet(Long id) {
        logger.info("Deleting test case set with ID: {}", id);

        TestCaseSet testCaseSet = testCaseSetMapper.findById(id);
        if (testCaseSet != null) {
            // 删除数据库记录（文件内容也会一起删除）
            int result = testCaseSetMapper.deleteById(id);
            if (result > 0) {
                logger.info("Test case set deleted successfully: {} - {}", testCaseSet.getName(), testCaseSet.getVersion());
            } else {
                throw new RuntimeException("Failed to delete test case set");
            }
        } else {
            throw new IllegalArgumentException("Test case set does not exist");
        }
    }

    /**
     * 更新用例集信息
     *
     * @param id 用例集ID
     * @param name 新的用例集名称
     * @param version 新的版本号
     * @param description 新的描述信息
     * @return 更新后的用例集对象
     * @throws IllegalArgumentException 当用例集不存在或名称版本冲突时抛出
     */
    public TestCaseSet updateTestCaseSet(Long id, String name, String version, String description) {
        logger.info("Updating test case set with ID: {}", id);

        TestCaseSet testCaseSet = testCaseSetMapper.findById(id);
        if (testCaseSet != null) {
            // 检查名称和版本是否与其他记录冲突
            if (!testCaseSet.getName().equals(name) || !testCaseSet.getVersion().equals(version)) {
                if (testCaseSetMapper.existsByNameAndVersion(name, version)) {
                    throw new IllegalArgumentException("Test case set with name and version already exists");
                }
            }

            testCaseSet.setName(name);
            testCaseSet.setVersion(version);
            testCaseSet.setDescription(description);

            int result = testCaseSetMapper.update(testCaseSet);
            if (result > 0) {
                logger.info("Test case set updated successfully: {} - {}", name, version);
                return testCaseSet;
            } else {
                throw new RuntimeException("Failed to update test case set");
            }
        } else {
            throw new IllegalArgumentException("Test case set does not exist");
        }
    }


    /**
     * 计算文件内容的SHA512哈希值
     *
     * @param fileContent 文件内容字节数组
     * @return SHA512哈希值的十六进制字符串
     * @throws RuntimeException 当计算哈希值失败时抛出
     */
    private String calculateSHA512(byte[] fileContent) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(fileContent);
            
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-512 algorithm not available", e);
            throw new RuntimeException("Failed to calculate SHA512 hash", e);
        }
    }

    /**
     * 验证文件格式和大小
     *
     * @param file 上传的文件
     * @throws IllegalArgumentException 当文件为空、大小超限或格式不支持时抛出
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // 检查文件大小 (100MB)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size cannot exceed 100MB");
        }

        // 检查文件类型 - 支持 .zip 和 .tar.gz
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("File name cannot be null");
        }

        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".zip") && !lowerFileName.endsWith(".tar.gz")) {
            throw new IllegalArgumentException("Only ZIP and TAR.GZ format files are supported");
        }

        logger.debug("File validation passed for: {}", fileName);
    }

    /**
     * 解析并存储测试用例信息
     *
     * @param testCaseSet 用例集对象
     * @param fileContent 压缩包文件内容
     * @param fileFormat 文件格式
     * @throws IOException 解析过程中发生IO异常时抛出
     */
    private void parseAndStoreTestCases(TestCaseSet testCaseSet, byte[] fileContent, String fileFormat) throws IOException {
        logger.info("Parsing and storing test cases for: {} - {}", testCaseSet.getName(), testCaseSet.getVersion());

        // 提取cases.xlsx文件内容
        byte[] excelData = archiveParseService.extractCasesExcel(fileContent, fileFormat);
        if (excelData == null) {
            throw new IOException("Failed to extract cases.xlsx from archive");
        }

        // 解析Excel文件获取用例信息
        List<TestCaseInfo> testCaseInfos = excelParseService.parseCasesExcel(excelData);
        if (testCaseInfos.isEmpty()) {
            logger.warn("No valid test cases found in Excel file");
            return;
        }

        // 提取脚本文件名列表
        List<String> scriptFileNames = archiveParseService.extractScriptFileNames(fileContent, fileFormat);

        // 匹配用例编号与脚本文件
        List<String> caseNumbers = testCaseInfos.stream()
                .map(TestCaseInfo::getCaseNumber)
                .collect(Collectors.toList());
        ScriptMatchResult matchResult = scriptMatchService.matchScripts(caseNumbers, scriptFileNames);

        // 创建TestCase实体并保存
        List<TestCase> testCases = new ArrayList<>();
        for (TestCaseInfo testCaseInfo : testCaseInfos) {
            TestCase testCase = new TestCase(
                testCaseSet,
                testCaseInfo.getCaseName(),
                testCaseInfo.getCaseNumber(),
                testCaseInfo.getNetworkTopology(),
                testCaseInfo.getBusinessCategory(),
                testCaseInfo.getAppName(),
                testCaseInfo.getTestSteps(),
                testCaseInfo.getExpectedResult()
            );
            
            // 设置脚本存在状态
            boolean scriptExists = matchResult.getMatchMap().getOrDefault(testCaseInfo.getCaseNumber(), false);
            testCase.setScriptExists(scriptExists);
            
            testCases.add(testCase);
        }

        // 批量保存测试用例
        testCaseService.saveTestCases(testCases);

        logger.info("Successfully stored {} test cases, {} with scripts, {} missing scripts", 
                   testCases.size(), matchResult.getMatchedCount(), matchResult.getMissingCount());
    }

    /**
     * 获取用例集的测试用例列表（分页）
     *
     * @param testCaseSetId 用例集ID
     * @param page 页码，从1开始
     * @param pageSize 每页大小
     * @return 测试用例分页数据
     */
    public Page<TestCase> getTestCases(Long testCaseSetId, int page, int pageSize) {
        logger.debug("Getting test cases for test case set: {}, page: {}, size: {}", testCaseSetId, page, pageSize);
        return testCaseService.getTestCasesByTestCaseSet(testCaseSetId, page, pageSize);
    }

    /**
     * 获取用例集中没有脚本的测试用例列表
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的测试用例列表
     */
    public List<TestCase> getMissingScripts(Long testCaseSetId) {
        logger.debug("Getting missing scripts for test case set: {}", testCaseSetId);
        return testCaseService.getMissingScripts(testCaseSetId);
    }

    /**
     * 统计用例集中没有脚本的测试用例数量
     *
     * @param testCaseSetId 用例集ID
     * @return 没有脚本的测试用例数量
     */
    public long countMissingScripts(Long testCaseSetId) {
        logger.debug("Counting missing scripts for test case set: {}", testCaseSetId);
        return testCaseService.countMissingScripts(testCaseSetId);
    }

}
