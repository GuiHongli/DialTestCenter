/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.dialtest.center.entity;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.huawei.dialtest.center.entity.TestCaseSet;

/**
 * 用例集实体测试类，测试TestCaseSet实体的各种功能
 * 包括构造函数、getter/setter、equals/hashCode、toString等方法
 * 验证实体类的数据完整性和业务逻辑正确性
 * 
 * @author g00940940
 * @since 2025-09-06
 */
public class TestCaseSetTest {

    private TestCaseSet testCaseSet1;
    private TestCaseSet testCaseSet2;
    private TestCaseSet testCaseSet3;

    @Before
    public void setUp() {
        testCaseSet1 = new TestCaseSet();
        testCaseSet1.setId(1L);
        testCaseSet1.setName("test");
        testCaseSet1.setVersion("v1");
        testCaseSet1.setFileContent("test content".getBytes());
        testCaseSet1.setFileFormat("zip");
        testCaseSet1.setCreator("admin");
        testCaseSet1.setFileSize(179L);
        testCaseSet1.setSha512("sha512_hash_1");
        testCaseSet1.setBusiness("VPN阻断业务");
        testCaseSet1.setDescription("Test description");
        testCaseSet1.setCreatedTime(LocalDateTime.now());
        testCaseSet1.setUpdatedTime(LocalDateTime.now());

        testCaseSet2 = new TestCaseSet();
        testCaseSet2.setId(1L);
        testCaseSet2.setName("test");
        testCaseSet2.setVersion("v1");
        testCaseSet2.setFileContent("test content".getBytes());
        testCaseSet2.setFileFormat("zip");
        testCaseSet2.setCreator("admin");
        testCaseSet2.setFileSize(179L);
        testCaseSet2.setSha512("sha512_hash_1");
        testCaseSet2.setBusiness("VPN阻断业务");
        testCaseSet2.setDescription("Test description");
        testCaseSet2.setCreatedTime(LocalDateTime.now());
        testCaseSet2.setUpdatedTime(LocalDateTime.now());

        testCaseSet3 = new TestCaseSet();
        testCaseSet3.setId(2L);
        testCaseSet3.setName("test2");
        testCaseSet3.setVersion("v2");
        testCaseSet3.setFileContent("test content 2".getBytes());
        testCaseSet3.setFileFormat("tar.gz");
        testCaseSet3.setCreator("user");
        testCaseSet3.setFileSize(200L);
        testCaseSet3.setSha512("sha512_hash_2");
        testCaseSet3.setBusiness("VPN阻断业务");
        testCaseSet3.setDescription("Test description 2");
        testCaseSet3.setCreatedTime(LocalDateTime.now());
        testCaseSet3.setUpdatedTime(LocalDateTime.now());
    }

    @Test
    public void testDefaultConstructor() {
        TestCaseSet testCaseSet = new TestCaseSet();
        assertNotNull(testCaseSet);
        assertNull(testCaseSet.getId());
        assertNull(testCaseSet.getName());
        assertNull(testCaseSet.getVersion());
        assertNull(testCaseSet.getFileContent());
        assertNull(testCaseSet.getFileFormat());
        assertNull(testCaseSet.getCreator());
        assertNull(testCaseSet.getFileSize());
        assertNull(testCaseSet.getDescription());
        assertNull(testCaseSet.getCreatedTime());
        assertNull(testCaseSet.getUpdatedTime());
    }

    @Test
    public void testParameterizedConstructor() {
        TestCaseSet testCaseSet = new TestCaseSet("test", "v1", "test content".getBytes(), "zip", "admin", 179L, "sha512_hash", "VPN阻断业务");
        assertNotNull(testCaseSet);
        assertEquals("test", testCaseSet.getName());
        assertEquals("v1", testCaseSet.getVersion());
        assertArrayEquals("test content".getBytes(), testCaseSet.getFileContent());
        assertEquals("zip", testCaseSet.getFileFormat());
        assertEquals("admin", testCaseSet.getCreator());
        assertEquals(Long.valueOf(179L), testCaseSet.getFileSize());
        assertEquals("sha512_hash", testCaseSet.getSha512());
        assertEquals("VPN阻断业务", testCaseSet.getBusiness());
    }

    @Test
    public void testGettersAndSetters() {
        TestCaseSet testCaseSet = new TestCaseSet();

        // Test ID
        testCaseSet.setId(1L);
        assertEquals(Long.valueOf(1L), testCaseSet.getId());

        // Test Name
        testCaseSet.setName("test");
        assertEquals("test", testCaseSet.getName());

        // Test Version
        testCaseSet.setVersion("v1");
        assertEquals("v1", testCaseSet.getVersion());

        // Test ZipFile
        byte[] zipFile = "test content".getBytes();
        testCaseSet.setFileContent(zipFile);
        assertArrayEquals(zipFile, testCaseSet.getFileContent());

        // Test FileFormat
        testCaseSet.setFileFormat("zip");
        assertEquals("zip", testCaseSet.getFileFormat());

        // Test Creator
        testCaseSet.setCreator("admin");
        assertEquals("admin", testCaseSet.getCreator());

        // Test FileSize
        testCaseSet.setFileSize(179L);
        assertEquals(Long.valueOf(179L), testCaseSet.getFileSize());

        // Test SHA512
        testCaseSet.setSha512("abc123def456");
        assertEquals("abc123def456", testCaseSet.getSha512());

        // Test Business
        testCaseSet.setBusiness("VPN阻断业务");
        assertEquals("VPN阻断业务", testCaseSet.getBusiness());

        // Test Description
        testCaseSet.setDescription("Test description");
        assertEquals("Test description", testCaseSet.getDescription());

        // Test CreatedTime
        LocalDateTime now = LocalDateTime.now();
        testCaseSet.setCreatedTime(now);
        assertEquals(now, testCaseSet.getCreatedTime());

        // Test UpdatedTime
        testCaseSet.setUpdatedTime(now);
        assertEquals(now, testCaseSet.getUpdatedTime());
    }

    @Test
    public void testEquals() {
        // Same object
        assertTrue(testCaseSet1.equals(testCaseSet1));

        // Equal objects (same id, name, version)
        assertTrue(testCaseSet1.equals(testCaseSet2));

        // Different objects (different id)
        assertFalse(testCaseSet1.equals(testCaseSet3));

        // Null object
        assertFalse(testCaseSet1.equals(null));

        // Different class
        assertFalse(testCaseSet1.equals("string"));
    }

    @Test
    public void testHashCode() {
        // Equal objects should have same hash code
        assertEquals(testCaseSet1.hashCode(), testCaseSet2.hashCode());

        // Different objects should have different hash codes
        assertNotEquals(testCaseSet1.hashCode(), testCaseSet3.hashCode());
    }

    @Test
    public void testToString() {
        String toString = testCaseSet1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestCaseSet"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name='test'"));
        assertTrue(toString.contains("version='v1'"));
        assertTrue(toString.contains("fileFormat='zip'"));
        assertTrue(toString.contains("creator='admin'"));
        assertTrue(toString.contains("fileSize=179"));
        assertTrue(toString.contains("sha512='sha512_hash_1'"));
        assertTrue(toString.contains("business='VPN阻断业务'"));
        assertTrue(toString.contains("description='Test description'"));
    }

    @Test
    public void testToStringWithNullFileContent() {
        TestCaseSet testCaseSet = new TestCaseSet();
        testCaseSet.setId(1L);
        testCaseSet.setName("test");
        testCaseSet.setVersion("v1");
        testCaseSet.setFileContent(null);
        testCaseSet.setCreator("admin");
        testCaseSet.setFileSize(179L);
        testCaseSet.setSha512("sha512_hash_1");
        testCaseSet.setBusiness("VPN阻断业务");
        testCaseSet.setDescription("Test description");
        testCaseSet.setCreatedTime(LocalDateTime.now());
        testCaseSet.setUpdatedTime(LocalDateTime.now());

        String toString = testCaseSet.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("fileContentSize=0"));
    }

    @Test
    public void testEqualsWithNullFields() {
        TestCaseSet testCaseSet1 = new TestCaseSet();
        TestCaseSet testCaseSet2 = new TestCaseSet();

        // Both have null id, name, version
        assertTrue(testCaseSet1.equals(testCaseSet2));

        // One has id, other doesn't
        testCaseSet1.setId(1L);
        assertFalse(testCaseSet1.equals(testCaseSet2));

        // Reset and test name
        testCaseSet1.setId(null);
        testCaseSet1.setName("test");
        assertFalse(testCaseSet1.equals(testCaseSet2));

        // Reset and test version
        testCaseSet1.setName(null);
        testCaseSet1.setVersion("v1");
        assertFalse(testCaseSet1.equals(testCaseSet2));
    }

    @Test
    public void testFileFormat() {
        TestCaseSet testCaseSet = new TestCaseSet();

        // Test ZIP format
        testCaseSet.setFileFormat("zip");
        assertEquals("zip", testCaseSet.getFileFormat());

        // Test TAR.GZ format
        testCaseSet.setFileFormat("tar.gz");
        assertEquals("tar.gz", testCaseSet.getFileFormat());

        // Test null format
        testCaseSet.setFileFormat(null);
        assertNull(testCaseSet.getFileFormat());
    }
}
