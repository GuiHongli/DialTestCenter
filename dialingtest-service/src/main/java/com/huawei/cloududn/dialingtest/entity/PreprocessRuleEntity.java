/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.entity;

/**
 * 预处理规则实体类
 * 
 * @author g00940940
 * @since 2025-01-27
 */
public class PreprocessRuleEntity {
    
    private Long id;
    private String ruleName;
    private String businessZh;
    private String businessEn;
    private String category;
    private String appName;
    private String content;
    private Boolean isCustom;
    private Long packageId;
    
    // 默认构造函数
    public PreprocessRuleEntity() {
    }
    
    // 带参数构造函数
    public PreprocessRuleEntity(String ruleName, String businessZh, String businessEn, 
                              String category, String appName, String content, 
                              Boolean isCustom, Long packageId) {
        this.ruleName = ruleName;
        this.businessZh = businessZh;
        this.businessEn = businessEn;
        this.category = category;
        this.appName = appName;
        this.content = content;
        this.isCustom = isCustom;
        this.packageId = packageId;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public String getBusinessZh() {
        return businessZh;
    }
    
    public void setBusinessZh(String businessZh) {
        this.businessZh = businessZh;
    }
    
    public String getBusinessEn() {
        return businessEn;
    }
    
    public void setBusinessEn(String businessEn) {
        this.businessEn = businessEn;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public void setAppName(String appName) {
        this.appName = appName;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Boolean getIsCustom() {
        return isCustom;
    }
    
    public void setIsCustom(Boolean isCustom) {
        this.isCustom = isCustom;
    }
    
    public Long getPackageId() {
        return packageId;
    }
    
    public void setPackageId(Long packageId) {
        this.packageId = packageId;
    }
    
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PreprocessRuleEntity that = (PreprocessRuleEntity) o;
        return java.util.Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "PreprocessRuleEntity{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", businessZh='" + businessZh + '\'' +
                ", businessEn='" + businessEn + '\'' +
                ", category='" + category + '\'' +
                ", appName='" + appName + '\'' +
                ", isCustom=" + isCustom +
                ", packageId=" + packageId +
                '}';
    }
}
