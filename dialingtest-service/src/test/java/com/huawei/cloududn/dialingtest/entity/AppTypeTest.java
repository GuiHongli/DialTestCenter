/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.entity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

/**
 * AppType实体类测试
 * 测试AppType类的构造函数、getter/setter方法、equals、hashCode和toString方法
 *
 * @author g00940940
 * @since 2025-01-24
 */
@RunWith(JUnit4.class)
public class AppTypeTest {

    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor() {
        AppType appType = new AppType();
        
        assertNotNull(appType);
        assertNull(appType.getId());
        assertNull(appType.getBusinessCategory());
        assertNull(appType.getAppName());
        assertNull(appType.getDescription());
    }

    /**
     * 测试带参数的构造函数
     */
    @Test
    public void testParameterizedConstructor() {
        String businessCategory = "VPN";
        String appName = "TestApp";
        String description = "Test application";
        
        AppType appType = new AppType(businessCategory, appName, description);
        
        assertNotNull(appType);
        assertEquals(businessCategory, appType.getBusinessCategory());
        assertEquals(appName, appType.getAppName());
        assertEquals(description, appType.getDescription());
    }

    /**
     * 测试ID的getter和setter方法
     */
    @Test
    public void testIdGetterSetter() {
        AppType appType = new AppType();
        Long id = 1L;
        
        appType.setId(id);
        assertEquals(id, appType.getId());
    }

    /**
     * 测试业务大类的getter和setter方法
     */
    @Test
    public void testBusinessCategoryGetterSetter() {
        AppType appType = new AppType();
        String businessCategory = "VPN";
        
        appType.setBusinessCategory(businessCategory);
        assertEquals(businessCategory, appType.getBusinessCategory());
    }

    /**
     * 测试应用名称的getter和setter方法
     */
    @Test
    public void testAppNameGetterSetter() {
        AppType appType = new AppType();
        String appName = "TestApp";
        
        appType.setAppName(appName);
        assertEquals(appName, appType.getAppName());
    }

    /**
     * 测试描述的getter和setter方法
     */
    @Test
    public void testDescriptionGetterSetter() {
        AppType appType = new AppType();
        String description = "Test application";
        
        appType.setDescription(description);
        assertEquals(description, appType.getDescription());
    }

    /**
     * 测试equals方法 - 相同对象
     */
    @Test
    public void testEquals_SameObject_ShouldReturnTrue() {
        AppType appType = new AppType("VPN", "TestApp", "Test application");
        
        assertTrue(appType.equals(appType));
    }

    /**
     * 测试equals方法 - 相等对象
     */
    @Test
    public void testEquals_EqualObjects_ShouldReturnTrue() {
        AppType appType1 = new AppType("VPN", "TestApp", "Test application");
        appType1.setId(1L);
        
        AppType appType2 = new AppType("VPN", "TestApp", "Test application");
        appType2.setId(1L);
        
        assertTrue(appType1.equals(appType2));
    }

    /**
     * 测试equals方法 - 不同对象
     */
    @Test
    public void testEquals_DifferentObjects_ShouldReturnFalse() {
        AppType appType1 = new AppType("VPN", "TestApp", "Test application");
        AppType appType2 = new AppType("VPN", "DifferentApp", "Test application");
        
        assertFalse(appType1.equals(appType2));
    }

    /**
     * 测试equals方法 - null对象
     */
    @Test
    public void testEquals_NullObject_ShouldReturnFalse() {
        AppType appType = new AppType("VPN", "TestApp", "Test application");
        
        assertFalse(appType.equals(null));
    }

    /**
     * 测试equals方法 - 不同类型对象
     */
    @Test
    public void testEquals_DifferentClass_ShouldReturnFalse() {
        AppType appType = new AppType("VPN", "TestApp", "Test application");
        String differentObject = "Not an AppType";
        
        assertFalse(appType.equals(differentObject));
    }

    /**
     * 测试hashCode方法 - 相等对象应该有相同哈希值
     */
    @Test
    public void testHashCode_EqualObjects_ShouldHaveSameHashCode() {
        AppType appType1 = new AppType("VPN", "TestApp", "Test application");
        appType1.setId(1L);
        
        AppType appType2 = new AppType("VPN", "TestApp", "Test application");
        appType2.setId(1L);
        
        assertEquals(appType1.hashCode(), appType2.hashCode());
    }

    /**
     * 测试hashCode方法 - 不同对象应该有不同的哈希值
     */
    @Test
    public void testHashCode_DifferentObjects_ShouldHaveDifferentHashCode() {
        AppType appType1 = new AppType("VPN", "TestApp", "Test application");
        AppType appType2 = new AppType("VPN", "DifferentApp", "Test application");
        
        assertNotEquals(appType1.hashCode(), appType2.hashCode());
    }

    /**
     * 测试toString方法
     */
    @Test
    public void testToString_ShouldContainAllFields() {
        AppType appType = new AppType("VPN", "TestApp", "Test application");
        appType.setId(1L);
        
        String toString = appType.toString();
        
        assertTrue(toString.contains("AppType"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("businessCategory='VPN'"));
        assertTrue(toString.contains("appName='TestApp'"));
        assertTrue(toString.contains("description='Test application'"));
    }

    /**
     * 测试toString方法 - null值处理
     */
    @Test
    public void testToString_WithNullValues_ShouldHandleGracefully() {
        AppType appType = new AppType();
        
        String toString = appType.toString();
        
        assertTrue(toString.contains("AppType"));
        assertTrue(toString.contains("id=null"));
        assertTrue(toString.contains("businessCategory='null'"));
        assertTrue(toString.contains("appName='null'"));
        assertTrue(toString.contains("description='null'"));
    }
}
