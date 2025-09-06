package com.dialtest.center.service;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.dialtest.center.entity.TestCaseSet;
import com.dialtest.center.repository.TestCaseSetRepository;

/**
 * 用例集服务类
 */
@Service
public class TestCaseSetService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestCaseSetService.class);
    
    @Autowired
    private TestCaseSetRepository testCaseSetRepository;
    
    
    /**
     * 获取用例集列表（分页）
     */
    public Page<TestCaseSet> getTestCaseSets(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        return testCaseSetRepository.findAllByOrderByCreatedTimeDesc(pageable);
    }
    
    /**
     * 根据ID获取用例集
     */
    public Optional<TestCaseSet> getTestCaseSetById(Long id) {
        return testCaseSetRepository.findById(id);
    }
    
    /**
     * 上传用例集
     */
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String creator) throws IOException {
        // 验证文件
        validateFile(file);
        
        // 解析文件名和格式
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
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
            throw new IllegalArgumentException("只支持ZIP和TAR.GZ格式文件");
        }
        
        int lastUnderscoreIndex = nameWithoutExt.lastIndexOf("_");
        if (lastUnderscoreIndex == -1) {
            throw new IllegalArgumentException("文件名格式错误，应为：用例集名称_版本号." + fileFormat);
        }
        
        String name = nameWithoutExt.substring(0, lastUnderscoreIndex);
        String version = nameWithoutExt.substring(lastUnderscoreIndex + 1);
        
        // 检查是否已存在
        if (testCaseSetRepository.existsByNameAndVersion(name, version)) {
            throw new IllegalArgumentException("用例集名称和版本已存在");
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
        logger.info("用例集上传成功: {} - {}, 格式: {}, 文件大小: {} bytes", name, version, fileFormat, fileContent.length);
        
        return saved;
    }
    
    /**
     * 删除用例集
     */
    public void deleteTestCaseSet(Long id) {
        Optional<TestCaseSet> testCaseSetOpt = testCaseSetRepository.findById(id);
        if (testCaseSetOpt.isPresent()) {
            TestCaseSet testCaseSet = testCaseSetOpt.get();
            
            // 删除数据库记录（文件内容也会一起删除）
            testCaseSetRepository.deleteById(id);
            logger.info("用例集删除成功: {} - {}", testCaseSet.getName(), testCaseSet.getVersion());
        } else {
            throw new IllegalArgumentException("用例集不存在");
        }
    }
    
    /**
     * 更新用例集信息
     */
    public TestCaseSet updateTestCaseSet(Long id, String name, String version, String description) {
        Optional<TestCaseSet> testCaseSetOpt = testCaseSetRepository.findById(id);
        if (testCaseSetOpt.isPresent()) {
            TestCaseSet testCaseSet = testCaseSetOpt.get();
            
            // 检查名称和版本是否与其他记录冲突
            if (!testCaseSet.getName().equals(name) || !testCaseSet.getVersion().equals(version)) {
                if (testCaseSetRepository.existsByNameAndVersion(name, version)) {
                    throw new IllegalArgumentException("用例集名称和版本已存在");
                }
            }
            
            testCaseSet.setName(name);
            testCaseSet.setVersion(version);
            testCaseSet.setDescription(description);
            
            return testCaseSetRepository.save(testCaseSet);
        } else {
            throw new IllegalArgumentException("用例集不存在");
        }
    }
    
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 检查文件大小 (100MB)
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }
        
        // 检查文件类型 - 支持 .zip 和 .tar.gz
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        String lowerFileName = fileName.toLowerCase();
        if (!lowerFileName.endsWith(".zip") && !lowerFileName.endsWith(".tar.gz")) {
            throw new IllegalArgumentException("只支持ZIP和TAR.GZ格式文件");
        }
    }
    
}
