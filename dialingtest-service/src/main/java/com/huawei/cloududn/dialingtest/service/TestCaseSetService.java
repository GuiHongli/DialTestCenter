package com.huawei.cloududn.dialingtest.service;

import com.huawei.cloududn.dialingtest.dao.TestCaseSetDao;
import com.huawei.cloududn.dialingtest.dao.TestCaseDao;
import com.huawei.cloududn.dialingtest.dao.AppTypeDao;
import com.huawei.cloududn.dialingtest.model.TestCaseSet;
import com.huawei.cloududn.dialingtest.model.TestCase;
import com.huawei.cloududn.dialingtest.entity.AppType;
import com.huawei.cloududn.dialingtest.model.TestCaseInfo;
import com.huawei.cloududn.dialingtest.model.UpdateTestCaseSetRequest;
import com.huawei.cloududn.dialingtest.util.OperationLogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.*;

/**
 * 用例集管理服务
 * 
 * @author Generated
 */
@Service
@Transactional
public class TestCaseSetService {
    
    @Autowired
    private TestCaseSetDao testCaseSetDao;
    
    @Autowired
    private TestCaseDao testCaseDao;
    
    @Autowired
    private AppTypeDao appTypeDao;
    
    @Autowired
    private ArchiveParseService archiveParseService;
    
    @Autowired
    private ExcelParseService excelParseService;
    
    
    @Autowired
    private OperationLogUtil operationLogUtil;
    
    /**
     * 上传用例集
     */
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String businessZh) {
        return uploadTestCaseSet(file, description, businessZh, null, false, "admin");
    }
    
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String businessZh, boolean overwrite) {
        return uploadTestCaseSet(file, description, businessZh, null, overwrite, "admin");
    }
    
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String businessZh, boolean overwrite, String operatorUsername) {
        return uploadTestCaseSet(file, description, businessZh, null, overwrite, operatorUsername);
    }
    
    public TestCaseSet uploadTestCaseSet(MultipartFile file, String description, String businessZh, String businessEn, boolean overwrite, String operatorUsername) {
        // 1. 文件验证
        validateFile(file);
        
        // 2. 文件名解析
        FileNameInfo fileNameInfo = parseFileName(file.getOriginalFilename());
        
        // 3. 重复性检查（如果不覆盖）
        if (!overwrite) {
            checkDuplicate(fileNameInfo.getName(), fileNameInfo.getVersion());
        } else {
            // 如果覆盖，先删除已存在的用例集
            deleteExistingTestCaseSet(fileNameInfo.getName(), fileNameInfo.getVersion());
        }
        
        try {
            // 4. 读取文件内容
            byte[] fileContent = file.getBytes();
            
            // 5. 计算SHA256哈希值
            String sha256 = calculateSHA256(fileContent);
            
            // 6. 压缩包解析
            ArchiveParseResult archiveResult = archiveParseService.parseArchive(fileContent);
            
            // 7. Excel文件解析
            List<TestCaseInfo> testCases = excelParseService.parseExcel(archiveResult.getExcelData());
            
            // 8. 脚本文件匹配
            List<TestCaseInfo> matchedTestCases = matchScripts(testCases, archiveResult.getScriptFileNames());
            
            // 9. 保存用例集
            TestCaseSet testCaseSet = new TestCaseSet();
            testCaseSet.setName(fileNameInfo.getName());
            testCaseSet.setVersion(fileNameInfo.getVersion());
            testCaseSet.setFileContent(fileContent);
            testCaseSet.setFileSize(file.getSize());
            testCaseSet.setDescription(description);
            testCaseSet.setSha256(sha256);
            testCaseSet.setBusinessZh(businessZh);
            testCaseSet.setBusinessEn("VPN_BLOCK"); // 默认英文业务类型
            
            testCaseSetDao.insert(testCaseSet);
            
            // 10. 保存测试用例
            saveTestCases(testCaseSet.getId(), matchedTestCases);
            
            // 11. 记录操作日志
            operationLogUtil.logTestCaseSetUpload(operatorUsername, testCaseSet);
            
            return testCaseSet;
        } catch (Exception e) {
            throw new RuntimeException("上传用例集失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 分页获取用例集列表
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTestCaseSets(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<TestCaseSet> testCaseSets = testCaseSetDao.findWithPagination(offset, pageSize);
        long total = testCaseSetDao.count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("data", testCaseSets);
        
        return result;
    }
    
    /**
     * 根据ID获取用例集
     */
    @Transactional(readOnly = true)
    public TestCaseSet getTestCaseSetById(Long id) {
        TestCaseSet testCaseSet = testCaseSetDao.findById(id);
        if (testCaseSet == null) {
            throw new IllegalArgumentException("用例集不存在，ID: " + id);
        }
        return testCaseSet;
    }
    
    /**
     * 更新用例集
     */
    public TestCaseSet updateTestCaseSet(Long id, UpdateTestCaseSetRequest request) {
        return updateTestCaseSet(id, request, "admin");
    }
    
    public TestCaseSet updateTestCaseSet(Long id, UpdateTestCaseSetRequest request, String operatorUsername) {
        TestCaseSet testCaseSet = getTestCaseSetById(id);
        
        // 创建更新前的对象用于操作记录
        TestCaseSet oldTestCaseSet = new TestCaseSet();
        oldTestCaseSet.setId(testCaseSet.getId());
        oldTestCaseSet.setName(testCaseSet.getName());
        oldTestCaseSet.setVersion(testCaseSet.getVersion());
        oldTestCaseSet.setDescription(testCaseSet.getDescription());
        oldTestCaseSet.setBusinessZh(testCaseSet.getBusinessZh());
        oldTestCaseSet.setBusinessEn(testCaseSet.getBusinessEn());
        oldTestCaseSet.setFileSize(testCaseSet.getFileSize());
        oldTestCaseSet.setSha256(testCaseSet.getSha256());
        
        // 更新字段
        if (request.getDescription() != null) {
            testCaseSet.setDescription(request.getDescription());
        }
        if (request.getBusinessZh() != null) {
            testCaseSet.setBusinessZh(request.getBusinessZh());
        }
        if (request.getBusinessEn() != null) {
            testCaseSet.setBusinessEn(request.getBusinessEn());
        }
        
        testCaseSetDao.update(testCaseSet);
        
        // 记录操作日志
        operationLogUtil.logTestCaseSetUpdate(operatorUsername, oldTestCaseSet, testCaseSet);
        
        return testCaseSet;
    }
    
    /**
     * 删除用例集
     */
    public boolean deleteTestCaseSet(Long id) {
        return deleteTestCaseSet(id, "admin");
    }
    
    public boolean deleteTestCaseSet(Long id, String operatorUsername) {
        TestCaseSet testCaseSet = getTestCaseSetById(id);
        if (testCaseSet == null) {
            return false;
        }
        
        // 删除测试用例（级联删除）
        testCaseDao.deleteByTestCaseSetId(id);
        
        // 删除用例集
        testCaseSetDao.deleteById(id);
        
        // 记录操作日志
        operationLogUtil.logTestCaseSetDelete(operatorUsername, testCaseSet);
        
        return true;
    }
    
    /**
     * 分页获取测试用例列表
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTestCases(Long testCaseSetId, int page, int pageSize) {
        // 验证用例集是否存在
        getTestCaseSetById(testCaseSetId);
        
        int offset = (page - 1) * pageSize;
        List<TestCase> testCases = testCaseDao.findByTestCaseSetIdWithPagination(testCaseSetId, offset, pageSize);
        long total = testCaseDao.countByTestCaseSetId(testCaseSetId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("total", total);
        result.put("data", testCases);
        
        return result;
    }
    
    /**
     * 获取缺失脚本列表
     */
    @Transactional(readOnly = true)
    public List<TestCase> getMissingScripts(Long testCaseSetId) {
        // 验证用例集是否存在
        getTestCaseSetById(testCaseSetId);
        
        return testCaseDao.findMissingScriptsByTestCaseSetId(testCaseSetId);
    }
    
    /**
     * 文件验证
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        // 文件大小检查（100MB限制）
        long maxSize = 100 * 1024 * 1024; // 100MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过100MB");
        }
        
        // 文件类型检查
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("只支持ZIP格式文件");
        }
    }
    
    /**
     * 文件名解析
     */
    private FileNameInfo parseFileName(String fileName) {
        if (!fileName.endsWith(".zip")) {
            throw new IllegalArgumentException("只支持ZIP格式文件");
        }
        
        String nameWithoutExt = fileName.substring(0, fileName.lastIndexOf(".zip"));
        int lastUnderscoreIndex = nameWithoutExt.lastIndexOf("_");
        if (lastUnderscoreIndex == -1) {
            throw new IllegalArgumentException("文件名格式错误，应为：用例集名称_版本号.zip");
        }
        
        String name = nameWithoutExt.substring(0, lastUnderscoreIndex);
        String version = nameWithoutExt.substring(lastUnderscoreIndex + 1);
        
        return new FileNameInfo(name, version);
    }
    
    /**
     * 重复性检查
     */
    private void checkDuplicate(String name, String version) {
        int count = testCaseSetDao.existsByNameAndVersion(name, version);
        if (count > 0) {
            throw new IllegalArgumentException("用例集名称和版本已存在: " + name + "_" + version);
        }
    }
    
    /**
     * 删除已存在的用例集
     */
    private void deleteExistingTestCaseSet(String name, String version) {
        TestCaseSet existingTestCaseSet = testCaseSetDao.findByNameAndVersion(name, version);
        if (existingTestCaseSet != null) {
            // 删除关联的测试用例（由于外键约束，会自动级联删除）
            testCaseDao.deleteByTestCaseSetId(existingTestCaseSet.getId());
            // 删除用例集
            testCaseSetDao.deleteById(existingTestCaseSet.getId());
        }
    }
    
    /**
     * 计算SHA256哈希值
     */
    private String calculateSHA256(byte[] data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算SHA256哈希值失败", e);
        }
    }
    
    /**
     * 保存测试用例
     */
    private void saveTestCases(Long testCaseSetId, List<TestCaseInfo> testCaseInfos) {
        List<TestCase> testCases = new ArrayList<>();
        
        for (TestCaseInfo testCaseInfo : testCaseInfos) {
            TestCase testCase = new TestCase();
            testCase.setTestCaseSetId(testCaseSetId);
            testCase.setCaseName(testCaseInfo.getCaseName());
            testCase.setCaseNumber(testCaseInfo.getCaseNumber());
            testCase.setTestSteps(testCaseInfo.getTestSteps());
            testCase.setExpectedResult(testCaseInfo.getExpectedResult());
            testCase.setBusinessCategory(testCaseInfo.getBusinessCategory());
            testCase.setAppName(testCaseInfo.getAppName());
            testCase.setDependenciesPackage(testCaseInfo.getDependenciesPackage());
            testCase.setDependenciesRule(testCaseInfo.getDependenciesRule());
            testCase.setEnvironmentConfig(testCaseInfo.getEnvironmentConfig());
            testCase.setScriptExists(testCaseInfo.isScriptExists());
            
            testCases.add(testCase);
            
            // 保存应用类型信息
            saveAppType(testCaseInfo.getBusinessCategory(), testCaseInfo.getAppName());
        }
        
        if (!testCases.isEmpty()) {
            testCaseDao.batchInsert(testCases);
        }
    }
    
    /**
     * 保存应用类型信息
     */
    private void saveAppType(String businessCategory, String appName) {
        if (businessCategory != null && appName != null) {
            int count = appTypeDao.existsByBusinessCategoryAndAppName(businessCategory, appName);
            if (count == 0) {
                AppType appType = new AppType();
                appType.setBusinessCategory(businessCategory);
                appType.setAppName(appName);
                appType.setDescription("自动创建的应用类型");
                appTypeDao.insert(appType);
            }
        }
    }
    
    /**
     * 文件名信息内部类
     */
    private static class FileNameInfo {
        private final String name;
        private final String version;
        
        public FileNameInfo(String name, String version) {
            this.name = name;
            this.version = version;
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
    }
    
    /**
     * 匹配脚本文件
     * 
     * @param testCases 测试用例列表
     * @param scriptFileNames 脚本文件名列表
     * @return 匹配后的测试用例列表
     */
    private List<TestCaseInfo> matchScripts(List<TestCaseInfo> testCases, List<String> scriptFileNames) {
        for (TestCaseInfo testCase : testCases) {
            String caseNumber = testCase.getCaseNumber();
            if (caseNumber != null && !caseNumber.trim().isEmpty()) {
                String expectedScriptFileName = caseNumber + ".py";
                boolean scriptExists = scriptFileNames.contains(expectedScriptFileName);
                testCase.setScriptExists(scriptExists);
            } else {
                testCase.setScriptExists(false);
            }
        }
        
        return testCases;
    }
}
