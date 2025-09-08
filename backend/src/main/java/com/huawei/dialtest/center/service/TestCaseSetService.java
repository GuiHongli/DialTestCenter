/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import com.huawei.dialtest.center.entity.TestCaseSet;
import com.huawei.dialtest.center.repository.TestCaseSetRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

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
    private TestCaseSetRepository testCaseSetRepository;

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
        return testCaseSetRepository.findAllByOrderByCreatedTimeDesc(pageable);
    }

    /**
     * 根据ID获取用例集
     * 
     * @param id 用例集ID
     * @return 用例集对象，如果不存在则返回空
     */
    public Optional<TestCaseSet> getTestCaseSetById(Long id) {
        logger.debug("Getting test case set by ID: {}", id);
        return testCaseSetRepository.findById(id);
    }

    /**
     * 上传用例集
     * 
     * @param file 用例集文件，支持.zip和.tar.gz格式
     * @param description 用例集描述信息
     * @param creator 创建者用户名
     * @return 保存后的用例集实体对象
     * @throws IOException 文件读取失败时抛出
     * @throws IllegalArgumentException 当文件格式不正确或参数无效时抛出
     */
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String creator) throws IOException {
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
        if (testCaseSetRepository.existsByNameAndVersion(name, version)) {
            throw new IllegalArgumentException("Test case set with name and version already exists");
        }

        // 读取文件内容
        byte[] fileContent = file.getBytes();

        // 创建用例集记录
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setName(name);
        testCaseSet.setVersion(version);
        testCaseSet.setZipFile(fileContent);
        testCaseSet.setFileFormat(fileFormat);
        testCaseSet.setCreator(creator);
        testCaseSet.setFileSize(file.getSize());
        testCaseSet.setDescription(description);

        TestCaseSet saved = testCaseSetRepository.save(testCaseSet);
        logger.info("Test case set uploaded successfully: {} - {}, format: {}, file size: {} bytes", name, version, fileFormat, fileContent.length);

        return saved;
    }

    /**
     * 删除用例集
     * 
     * @param id 用例集ID
     * @throws IllegalArgumentException 当用例集不存在时抛出
     */
    public void deleteTestCaseSet(Long id) {
        logger.info("Deleting test case set with ID: {}", id);

        Optional<TestCaseSet> testCaseSetOpt = testCaseSetRepository.findById(id);
        if (testCaseSetOpt.isPresent()) {
            TestCaseSet testCaseSet = testCaseSetOpt.get();

            // 删除数据库记录（文件内容也会一起删除）
            testCaseSetRepository.deleteById(id);
            logger.info("Test case set deleted successfully: {} - {}", testCaseSet.getName(), testCaseSet.getVersion());
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

        Optional<TestCaseSet> testCaseSetOpt = testCaseSetRepository.findById(id);
        if (testCaseSetOpt.isPresent()) {
            TestCaseSet testCaseSet = testCaseSetOpt.get();

            // 检查名称和版本是否与其他记录冲突
            if (!testCaseSet.getName().equals(name) || !testCaseSet.getVersion().equals(version)) {
                if (testCaseSetRepository.existsByNameAndVersion(name, version)) {
                    throw new IllegalArgumentException("Test case set with name and version already exists");
                }
            }

            testCaseSet.setName(name);
            testCaseSet.setVersion(version);
            testCaseSet.setDescription(description);

            TestCaseSet updated = testCaseSetRepository.save(testCaseSet);
            logger.info("Test case set updated successfully: {} - {}", name, version);
            return updated;
        } else {
            throw new IllegalArgumentException("Test case set does not exist");
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

}
