/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.entity;

import com.huawei.dialtest.center.entity.SoftwarePackage;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.time.LocalDateTime;

/**
 * SoftwarePackage实体类测试
 * 测试实体类的基本功能，包括构造函数、getter/setter、equals/hashCode、toString等方法
 *
 * @author g00940940
 * @since 2025-09-09
 */
public class SoftwarePackageTest {

    private SoftwarePackage softwarePackage;
    private byte[] testFileContent;
    private LocalDateTime testTime;

    @Before
    public void setUp() {
        testFileContent = "test file content".getBytes();
        testTime = LocalDateTime.now();
        
        softwarePackage = new SoftwarePackage();
        softwarePackage.setId(1L);
        softwarePackage.setSoftwareName("TestApp_1.0.0.apk");
        softwarePackage.setFileContent(testFileContent);
        softwarePackage.setFileFormat("apk");
        softwarePackage.setSha512("test_sha512_hash");
        softwarePackage.setPlatform("android");
        softwarePackage.setCreator("admin");
        softwarePackage.setFileSize(1024L);
        softwarePackage.setDescription("Test description");
        softwarePackage.setCreatedTime(testTime);
        softwarePackage.setUpdatedTime(testTime);
    }

    @Test
    public void testDefaultConstructor() {
        SoftwarePackage package1 = new SoftwarePackage();
        assertNotNull("Default constructor should create object", package1);
        assertNull("ID should be null initially", package1.getId());
        assertNull("Software name should be null initially", package1.getSoftwareName());
    }

    @Test
    public void testParameterizedConstructor() {
        SoftwarePackage package1 = new SoftwarePackage(
            "TestApp_1.0.0.apk", testFileContent, "apk", 
            "android", "admin", 1024L, "test_sha512"
        );
        
        assertEquals("Software name should match", "TestApp_1.0.0.apk", package1.getSoftwareName());
        assertArrayEquals("File content should match", testFileContent, package1.getFileContent());
        assertEquals("File format should match", "apk", package1.getFileFormat());
        assertEquals("Platform should match", "android", package1.getPlatform());
        assertEquals("Creator should match", "admin", package1.getCreator());
        assertEquals("File size should match", Long.valueOf(1024L), package1.getFileSize());
        assertEquals("SHA512 should match", "test_sha512", package1.getSha512());
    }

    @Test
    public void testGettersAndSetters() {
        // Test ID
        assertEquals("ID getter should work", Long.valueOf(1L), softwarePackage.getId());
        softwarePackage.setId(2L);
        assertEquals("ID setter should work", Long.valueOf(2L), softwarePackage.getId());

        // Test Software Name
        assertEquals("Software name getter should work", "TestApp_1.0.0.apk", softwarePackage.getSoftwareName());
        softwarePackage.setSoftwareName("NewApp_2.0.0.apk");
        assertEquals("Software name setter should work", "NewApp_2.0.0.apk", softwarePackage.getSoftwareName());


        // Test File Content
        assertArrayEquals("File content getter should work", testFileContent, softwarePackage.getFileContent());
        byte[] newContent = "new content".getBytes();
        softwarePackage.setFileContent(newContent);
        assertArrayEquals("File content setter should work", newContent, softwarePackage.getFileContent());

        // Test File Format
        assertEquals("File format getter should work", "apk", softwarePackage.getFileFormat());
        softwarePackage.setFileFormat("ipa");
        assertEquals("File format setter should work", "ipa", softwarePackage.getFileFormat());

        // Test SHA512
        assertEquals("SHA512 getter should work", "test_sha512_hash", softwarePackage.getSha512());
        softwarePackage.setSha512("new_sha512");
        assertEquals("SHA512 setter should work", "new_sha512", softwarePackage.getSha512());

        // Test Platform
        assertEquals("Platform getter should work", "android", softwarePackage.getPlatform());
        softwarePackage.setPlatform("ios");
        assertEquals("Platform setter should work", "ios", softwarePackage.getPlatform());

        // Test Creator
        assertEquals("Creator getter should work", "admin", softwarePackage.getCreator());
        softwarePackage.setCreator("user");
        assertEquals("Creator setter should work", "user", softwarePackage.getCreator());

        // Test File Size
        assertEquals("File size getter should work", Long.valueOf(1024L), softwarePackage.getFileSize());
        softwarePackage.setFileSize(2048L);
        assertEquals("File size setter should work", Long.valueOf(2048L), softwarePackage.getFileSize());

        // Test Description
        assertEquals("Description getter should work", "Test description", softwarePackage.getDescription());
        softwarePackage.setDescription("New description");
        assertEquals("Description setter should work", "New description", softwarePackage.getDescription());


        // Test Created Time
        assertEquals("Created time getter should work", testTime, softwarePackage.getCreatedTime());
        LocalDateTime newTime = LocalDateTime.now().plusHours(1);
        softwarePackage.setCreatedTime(newTime);
        assertEquals("Created time setter should work", newTime, softwarePackage.getCreatedTime());

        // Test Updated Time
        assertEquals("Updated time getter should work", testTime, softwarePackage.getUpdatedTime());
        softwarePackage.setUpdatedTime(newTime);
        assertEquals("Updated time setter should work", newTime, softwarePackage.getUpdatedTime());
    }

    @Test
    public void testEquals() {
        SoftwarePackage package1 = new SoftwarePackage();
        package1.setId(1L);
        package1.setSoftwareName("TestApp_1.0.0.apk");

        SoftwarePackage package2 = new SoftwarePackage();
        package2.setId(1L);
        package2.setSoftwareName("TestApp_1.0.0.apk");

        SoftwarePackage package3 = new SoftwarePackage();
        package3.setId(2L);
        package3.setSoftwareName("TestApp_1.0.0.apk");

        SoftwarePackage package4 = new SoftwarePackage();
        package4.setId(1L);
        package4.setSoftwareName("DifferentApp_2.0.0.apk");

        // Test equality
        assertTrue("Same objects should be equal", package1.equals(package1));
        assertTrue("Objects with same ID and name should be equal", package1.equals(package2));
        assertTrue("Equals should be symmetric", package2.equals(package1));

        // Test inequality
        assertFalse("Objects with different IDs should not be equal", package1.equals(package3));
        assertFalse("Objects with different software names should not be equal", package1.equals(package4));
        assertFalse("Object should not equal null", package1.equals(null));
        assertFalse("Object should not equal different class", package1.equals("string"));
    }

    @Test
    public void testHashCode() {
        SoftwarePackage package1 = new SoftwarePackage();
        package1.setId(1L);
        package1.setSoftwareName("TestApp_1.0.0.apk");

        SoftwarePackage package2 = new SoftwarePackage();
        package2.setId(1L);
        package2.setSoftwareName("TestApp_1.0.0.apk");

        SoftwarePackage package3 = new SoftwarePackage();
        package3.setId(2L);
        package3.setSoftwareName("TestApp_1.0.0.apk");

        // Test hash code consistency
        assertEquals("Hash codes should be consistent", package1.hashCode(), package1.hashCode());
        assertEquals("Equal objects should have same hash code", package1.hashCode(), package2.hashCode());
        assertNotEquals("Different objects should have different hash codes", package1.hashCode(), package3.hashCode());
    }

    @Test
    public void testToString() {
        String toString = softwarePackage.toString();
        
        assertNotNull("ToString should not be null", toString);
        assertTrue("ToString should contain class name", toString.contains("SoftwarePackage"));
        assertTrue("ToString should contain ID", toString.contains("id=1"));
        assertTrue("ToString should contain software name", toString.contains("softwareName='TestApp_1.0.0.apk'"));
        assertTrue("ToString should contain file format", toString.contains("fileFormat='apk'"));
        assertTrue("ToString should contain platform", toString.contains("platform='android'"));
        assertTrue("ToString should contain creator", toString.contains("creator='admin'"));
        assertTrue("ToString should contain file size", toString.contains("fileSize=1024"));
        assertTrue("ToString should contain SHA512", toString.contains("sha512='test_sha512_hash'"));
        assertTrue("ToString should contain description", toString.contains("description='Test description'"));
    }

    @Test
    public void testFileContentSizeInToString() {
        String toString = softwarePackage.toString();
        assertTrue("ToString should contain file content size", toString.contains("fileContentSize=" + testFileContent.length));
    }

    @Test
    public void testNullValues() {
        SoftwarePackage package1 = new SoftwarePackage();
        
        // Test that null values are handled properly
        assertNull("ID should be null", package1.getId());
        assertNull("Software name should be null", package1.getSoftwareName());
        assertNull("File content should be null", package1.getFileContent());
        assertNull("File format should be null", package1.getFileFormat());
        assertNull("SHA512 should be null", package1.getSha512());
        assertNull("Platform should be null", package1.getPlatform());
        assertNull("Creator should be null", package1.getCreator());
        assertNull("File size should be null", package1.getFileSize());
        assertNull("Description should be null", package1.getDescription());
        assertNull("Created time should be null", package1.getCreatedTime());
        assertNull("Updated time should be null", package1.getUpdatedTime());
    }
}
