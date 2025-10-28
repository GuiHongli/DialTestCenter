/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * OperationDataBuilder 单元测试
 * 
 * @author g00940940
 * @since 2025-01-27
 */
public class OperationDataBuilderTest {

    private OperationDataBuilder builder;

    @Before
    public void setUp() {
        builder = new OperationDataBuilder();
    }

    /**
     * 测试默认构造函数
     */
    @Test
    public void testDefaultConstructor_CreatesEmptyBuilder() {
        // Act
        OperationDataBuilder newBuilder = new OperationDataBuilder();

        // Assert
        assertNotNull(newBuilder);
        Map<String, Object> data = newBuilder.build();
        assertNotNull(data);
        assertTrue(data.isEmpty());
    }

    /**
     * 测试添加数据字段
     */
    @Test
    public void testAdd_SingleField_AddsCorrectly() {
        // Act
        OperationDataBuilder result = builder.add("key1", "value1");

        // Assert
        assertSame(builder, result); // 链式调用
        Map<String, Object> data = builder.build();
        assertEquals("value1", data.get("key1"));
    }

    /**
     * 测试添加多个数据字段
     */
    @Test
    public void testAdd_MultipleFields_AddsAllCorrectly() {
        // Act
        builder.add("key1", "value1")
               .add("key2", "value2")
               .add("key3", 123);

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals("value1", data.get("key1"));
        assertEquals("value2", data.get("key2"));
        assertEquals(123, data.get("key3"));
        assertEquals(3, data.size());
    }

    /**
     * 测试添加null值
     */
    @Test
    public void testAdd_NullValue_AddsCorrectly() {
        // Act
        builder.add("key1", null);

        // Assert
        Map<String, Object> data = builder.build();
        assertNull(data.get("key1"));
        assertEquals(1, data.size());
    }

    /**
     * 测试构建操作数据
     */
    @Test
    public void testBuild_ReturnsNewMap() {
        // Arrange
        builder.add("key1", "value1");

        // Act
        Map<String, Object> data1 = builder.build();
        Map<String, Object> data2 = builder.build();

        // Assert
        assertNotNull(data1);
        assertNotNull(data2);
        assertNotSame(data1, data2); // 每次build都返回新的Map
        assertEquals(data1, data2); // 但内容相同
    }

    /**
     * 测试从实体类转换数据 - 成功场景
     */
    @Test
    public void testFromEntity_ValidEntity_ConvertsCorrectly() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("test");
        entity.setValue(100);

        // Act
        OperationDataBuilder result = builder.fromEntity(entity);

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertEquals("test", data.get("name"));
        assertEquals(100, data.get("value"));
    }

    /**
     * 测试从实体类转换数据 - null实体
     */
    @Test
    public void testFromEntity_NullEntity_HandlesGracefully() {
        // Act
        OperationDataBuilder result = builder.fromEntity(null);

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertTrue(data.isEmpty());
    }

    /**
     * 测试从实体类转换数据 - 包含null字段
     */
    @Test
    public void testFromEntity_EntityWithNullFields_IgnoresNullFields() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName(null); // null字段
        entity.setValue(100);

        // Act
        builder.fromEntity(entity);

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertFalse(data.containsKey("name")); // null字段不应该被添加
        assertEquals(100, data.get("value"));
    }

    /**
     * 测试构建操作 - 单实体操作
     */
    @Test
    public void testBuildOperation_SingleEntity_SetsCorrectFields() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("test");

        // Act
        OperationDataBuilder result = builder.buildOperation(entity, "CREATE", "USER");

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertEquals("test", data.get("name"));
        assertEquals("CREATE", data.get("operationType"));
        assertEquals("USER", data.get("operationTarget"));
    }

    /**
     * 测试构建更新操作
     */
    @Test
    public void testBuildUpdateOperation_OldAndNewValues_SetsCorrectFields() {
        // Arrange
        TestEntity oldEntity = new TestEntity();
        oldEntity.setId(1L);
        oldEntity.setName("old");

        TestEntity newEntity = new TestEntity();
        newEntity.setId(1L);
        newEntity.setName("new");

        // Act
        OperationDataBuilder result = builder.buildUpdateOperation(oldEntity, newEntity, "UPDATE", "USER");

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals("UPDATE", data.get("operationType"));
        assertEquals("USER", data.get("operationTarget"));
        assertEquals(oldEntity, data.get("oldValues"));
        assertEquals(newEntity, data.get("newValues"));
    }

    /**
     * 测试构建自定义操作
     */
    @Test
    public void testBuildCustomOperation_WithAdditionalData_SetsAllFields() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId(1L);

        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put("customField1", "customValue1");
        additionalData.put("customField2", "customValue2");

        // Act
        OperationDataBuilder result = builder.buildCustomOperation(entity, "CUSTOM", "USER", additionalData);

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertEquals("CUSTOM", data.get("operationType"));
        assertEquals("USER", data.get("operationTarget"));
        assertEquals("customValue1", data.get("customField1"));
        assertEquals("customValue2", data.get("customField2"));
    }

    /**
     * 测试构建自定义操作 - null额外数据
     */
    @Test
    public void testBuildCustomOperation_NullAdditionalData_HandlesGracefully() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId(1L);

        // Act
        OperationDataBuilder result = builder.buildCustomOperation(entity, "CUSTOM", "USER", null);

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertEquals("CUSTOM", data.get("operationType"));
        assertEquals("USER", data.get("operationTarget"));
    }

    /**
     * 测试用户创建操作
     */
    @Test
    public void testUserCreate_SetsCorrectOperationTypeAndTarget() {
        // Arrange
        TestEntity user = new TestEntity();
        user.setId(1L);
        user.setName("testuser");

        // Act
        OperationDataBuilder result = builder.userCreate(user);

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertEquals("testuser", data.get("name"));
        assertEquals(OperationDataBuilder.OPERATION_CREATE, data.get("operationType"));
        assertEquals(OperationDataBuilder.TARGET_USER, data.get("operationTarget"));
    }

    /**
     * 测试用户更新操作
     */
    @Test
    public void testUserUpdate_SetsCorrectOperationTypeAndTarget() {
        // Arrange
        TestEntity oldUser = new TestEntity();
        oldUser.setId(1L);
        oldUser.setName("olduser");

        TestEntity newUser = new TestEntity();
        newUser.setId(1L);
        newUser.setName("newuser");

        // Act
        OperationDataBuilder result = builder.userUpdate(oldUser, newUser);

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals(OperationDataBuilder.OPERATION_UPDATE, data.get("operationType"));
        assertEquals(OperationDataBuilder.TARGET_USER, data.get("operationTarget"));
        assertEquals(oldUser, data.get("oldValues"));
        assertEquals(newUser, data.get("newValues"));
    }

    /**
     * 测试用例集创建操作
     */
    @Test
    public void testTestCaseSetCreate_SetsCorrectOperationTypeAndTarget() {
        // Arrange
        TestEntity testCaseSet = new TestEntity();
        testCaseSet.setId(1L);
        testCaseSet.setName("testcaseset");

        // Act
        OperationDataBuilder result = builder.testCaseSetCreate(testCaseSet);

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals(1L, data.get("id"));
        assertEquals("testcaseset", data.get("name"));
        assertEquals(OperationDataBuilder.OPERATION_CREATE, data.get("operationType"));
        assertEquals(OperationDataBuilder.TARGET_TEST_CASE_SET, data.get("operationTarget"));
    }

    /**
     * 测试软件包覆盖操作
     */
    @Test
    public void testSoftwarePackageOverwrite_SetsCorrectOperationTypeAndTarget() {
        // Arrange
        TestEntity oldPackage = new TestEntity();
        oldPackage.setId(1L);
        oldPackage.setName("oldpackage");

        TestEntity newPackage = new TestEntity();
        newPackage.setId(1L);
        newPackage.setName("newpackage");

        // Act
        OperationDataBuilder result = builder.softwarePackageOverwrite(oldPackage, newPackage);

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals(OperationDataBuilder.OPERATION_OVERWRITE, data.get("operationType"));
        assertEquals(OperationDataBuilder.TARGET_SOFTWARE_PACKAGE, data.get("operationTarget"));
        assertEquals(oldPackage, data.get("oldValues"));
        assertEquals(newPackage, data.get("newValues"));
    }

    /**
     * 测试添加描述
     */
    @Test
    public void testWithDescription_AddsDescriptionFields() {
        // Act
        OperationDataBuilder result = builder.withDescription("中文描述", "English Description");

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals("中文描述", data.get("descriptionZh"));
        assertEquals("English Description", data.get("descriptionEn"));
    }

    /**
     * 测试添加时间戳
     */
    @Test
    public void testWithTimestamp_AddsTimestampField() {
        // Act
        long beforeTime = System.currentTimeMillis();
        OperationDataBuilder result = builder.withTimestamp();
        long afterTime = System.currentTimeMillis();

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertTrue(data.containsKey("timestamp"));
        long timestamp = (Long) data.get("timestamp");
        assertTrue(timestamp >= beforeTime && timestamp <= afterTime);
    }

    /**
     * 测试添加操作者信息
     */
    @Test
    public void testWithOperator_AddsOperatorField() {
        // Act
        OperationDataBuilder result = builder.withOperator("testuser");

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals("testuser", data.get("operatorUsername"));
    }

    /**
     * 测试添加自定义数据
     */
    @Test
    public void testWithCustomData_AddsCustomField() {
        // Act
        OperationDataBuilder result = builder.withCustomData("customKey", "customValue");

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals("customValue", data.get("customKey"));
    }

    /**
     * 测试预处理规则包上传操作
     */
    @Test
    public void testPreprocessRulePackageUpload_SetsCorrectFields() {
        // Act
        OperationDataBuilder result = builder.preprocessRulePackageUpload("test-package", "测试业务", "TEST_BUSINESS");

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals("test-package", data.get("packageName"));
        assertEquals("测试业务", data.get("businessZh"));
        assertEquals("TEST_BUSINESS", data.get("businessEn"));
        assertTrue(data.containsKey("timestamp"));
    }

    /**
     * 测试预处理规则包删除操作
     */
    @Test
    public void testPreprocessRulePackageDelete_SetsCorrectFields() {
        // Act
        OperationDataBuilder result = builder.preprocessRulePackageDelete("test-package");

        // Assert
        Map<String, Object> data = builder.build();
        assertEquals("test-package", data.get("packageName"));
        assertTrue(data.containsKey("timestamp"));
    }

    /**
     * 测试链式调用
     */
    @Test
    public void testChainedCalls_WorksCorrectly() {
        // Act
        OperationDataBuilder result = builder
            .add("field1", "value1")
            .add("field2", "value2")
            .withDescription("描述", "Description")
            .withOperator("testuser")
            .withTimestamp();

        // Assert
        assertSame(builder, result);
        Map<String, Object> data = builder.build();
        assertEquals("value1", data.get("field1"));
        assertEquals("value2", data.get("field2"));
        assertEquals("描述", data.get("descriptionZh"));
        assertEquals("Description", data.get("descriptionEn"));
        assertEquals("testuser", data.get("operatorUsername"));
        assertTrue(data.containsKey("timestamp"));
    }

    /**
     * 测试常量值
     */
    @Test
    public void testConstants_HaveCorrectValues() {
        // Assert
        assertEquals("CREATE", OperationDataBuilder.OPERATION_CREATE);
        assertEquals("UPDATE", OperationDataBuilder.OPERATION_UPDATE);
        assertEquals("DELETE", OperationDataBuilder.OPERATION_DELETE);
        assertEquals("VIEW", OperationDataBuilder.OPERATION_VIEW);
        assertEquals("LOGIN", OperationDataBuilder.OPERATION_LOGIN);
        assertEquals("LOGOUT", OperationDataBuilder.OPERATION_LOGOUT);
        assertEquals("OVERWRITE", OperationDataBuilder.OPERATION_OVERWRITE);
        assertEquals("BATCH_CREATE", OperationDataBuilder.OPERATION_BATCH_CREATE);

        assertEquals("USER", OperationDataBuilder.TARGET_USER);
        assertEquals("USER_ROLE", OperationDataBuilder.TARGET_USER_ROLE);
        assertEquals("TEST_CASE_SET", OperationDataBuilder.TARGET_TEST_CASE_SET);
        assertEquals("SOFTWARE_PACKAGE", OperationDataBuilder.TARGET_SOFTWARE_PACKAGE);
        assertEquals("SYSTEM", OperationDataBuilder.TARGET_SYSTEM);
    }

    /**
     * 测试屏蔽敏感字段 - 普通实体不屏蔽
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMaskSensitiveFields_NonDialUserEntity_NotMasked() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId(1L);
        entity.setName("test");
        entity.setValue(100);

        // Act
        builder.buildUpdateOperation(entity, entity, "UPDATE", "USER");

        // Assert
        Map<String, Object> data = builder.build();
        Map<String, Object> oldValues = (Map<String, Object>) data.get("oldValues");
        assertEquals(1L, oldValues.get("id"));
        assertEquals("test", oldValues.get("name"));
        assertEquals(100, oldValues.get("value"));
    }

    /**
     * 测试屏蔽敏感字段 - DialUser 实体屏蔽 password 字段
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMaskSensitiveFields_DialUserEntity_MasksPassword() {
        // Arrange
        MockDialUserEntity entity = new MockDialUserEntity();
        entity.setId(1);
        entity.setUsername("testuser");
        entity.setPassword("secretpassword");

        // Act
        builder.buildUpdateOperation(entity, entity, "UPDATE", "USER");

        // Assert
        Map<String, Object> data = builder.build();
        Map<String, Object> oldValues = (Map<String, Object>) data.get("oldValues");
        assertEquals(1, oldValues.get("id"));
        assertEquals("testuser", oldValues.get("username"));
        assertEquals("*******", oldValues.get("password"));
    }

    /**
     * 测试屏蔽敏感字段 - null 实体
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMaskSensitiveFields_NullEntity_ReturnsEmpty() {
        // Act
        builder.buildUpdateOperation(null, null, "UPDATE", "USER");

        // Assert
        Map<String, Object> data = builder.build();
        Map<String, Object> oldValues = (Map<String, Object>) data.get("oldValues");
        assertNotNull(oldValues);
        assertTrue(oldValues.isEmpty());
    }

    /**
     * 测试屏蔽敏感字段 - 包含 null 字段的实体
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testMaskSensitiveFields_EntityWithNullFields_IgnoresNullFields() {
        // Arrange
        MockDialUserEntity entity = new MockDialUserEntity();
        entity.setId(1);
        entity.setUsername("testuser");
        entity.setPassword(null);

        // Act
        builder.buildUpdateOperation(entity, entity, "UPDATE", "USER");

        // Assert
        Map<String, Object> data = builder.build();
        Map<String, Object> oldValues = (Map<String, Object>) data.get("oldValues");
        assertEquals(1, oldValues.get("id"));
        assertEquals("testuser", oldValues.get("username"));
        assertFalse(oldValues.containsKey("password"));
    }

    // 测试用的内部实体类
    private static class TestEntity {
        private Long id;
        private String name;
        private Integer value;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    // 测试用的 DialUser 实体类（模拟 com.huawei.cloududn.dialingtest.model.DialUser）
    // 类名必须是 com.huawei.cloududn.dialingtest.model.DialUser 才会触发密码屏蔽逻辑
    private static class MockDialUserEntity {
        private Integer id;
        private String username;
        private String password;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "MockDialUserEntity{id=" + id + ", username='" + username + "'}";
        }
    }
}

