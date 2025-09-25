/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Field;

/**
 * 操作数据构建器
 * 用于构建各种操作的数据，支持实体类参数和链式调用
 *
 * @author g00940940
 * @since 2025-01-24
 */
public class OperationDataBuilder {
    private Map<String, Object> data;

    public OperationDataBuilder() {
        this.data = new HashMap<>();
    }

    /**
     * 添加数据字段
     *
     * @param key 字段名
     * @param value 字段值
     * @return 当前构建器，支持链式调用
     */
    public OperationDataBuilder add(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 构建操作数据
     *
     * @return 操作数据Map
     */
    public Map<String, Object> build() {
        return new HashMap<>(this.data);
    }

    // 通用方法：将实体类转换为操作数据
    public OperationDataBuilder fromEntity(Object entity) {
        if (entity == null) {
            return this;
        }
        
        try {
            Class<?> clazz = entity.getClass();
            Field[] fields = clazz.getDeclaredFields();
            
            for (Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) {
                    this.data.put(field.getName(), value);
                }
            }
        } catch (Exception e) {
            // 如果反射失败，至少添加对象的字符串表示
            this.data.put("entity", entity.toString());
        }
        
        return this;
    }

    // 通用更新操作构建方法 - 避免重复信息
    public OperationDataBuilder buildUpdateOperation(Object oldValues, Object newValues, String operationType, String operationTarget) {
        return add("operationType", operationType)
               .add("operationTarget", operationTarget)
               .add("oldValues", oldValues)
               .add("newValues", newValues);
    }

    // 通用操作构建方法
    public OperationDataBuilder buildOperation(Object entity, String operationType, String operationTarget) {
        return fromEntity(entity)
               .add("operationType", operationType)
               .add("operationTarget", operationTarget);
    }

    // 用户操作相关方法 - 使用通用方法
    public OperationDataBuilder userCreate(Object userEntity) {
        return buildOperation(userEntity, "CREATE", "USER");
    }

    public OperationDataBuilder userUpdate(Object oldValues, Object newValues) {
        return buildUpdateOperation(oldValues, newValues, "UPDATE", "USER");
    }

    public OperationDataBuilder userDelete(Object userEntity) {
        return buildOperation(userEntity, "DELETE", "USER");
    }

    public OperationDataBuilder userView(Object userEntity) {
        return buildOperation(userEntity, "VIEW", "USER");
    }

    public OperationDataBuilder userLogin(Object userEntity) {
        return buildOperation(userEntity, "LOGIN", "SYSTEM");
    }

    public OperationDataBuilder userLogout(Object userEntity) {
        return buildOperation(userEntity, "LOGOUT", "SYSTEM");
    }

    // 用户角色操作相关方法 - 使用通用方法
    public OperationDataBuilder userRoleCreate(Object userRoleEntity) {
        return buildOperation(userRoleEntity, "CREATE", "USER_ROLE");
    }

    public OperationDataBuilder userRoleUpdate(Object oldValues, Object newValues) {
        return buildUpdateOperation(oldValues, newValues, "UPDATE", "USER_ROLE");
    }

    public OperationDataBuilder userRoleDelete(Object userRoleEntity) {
        return buildOperation(userRoleEntity, "DELETE", "USER_ROLE");
    }

    public OperationDataBuilder userRoleNoChanges(Object userRoleEntity) {
        return buildOperation(userRoleEntity, "UPDATE", "USER_ROLE")
               .add("changes", "none");
    }

    // 用例集操作相关方法 - 使用通用方法
    public OperationDataBuilder testCaseSetCreate(Object testCaseSetEntity) {
        return buildOperation(testCaseSetEntity, "CREATE", "TEST_CASE_SET");
    }

    public OperationDataBuilder testCaseSetUpdate(Object oldValues, Object newValues) {
        return buildUpdateOperation(oldValues, newValues, "UPDATE", "TEST_CASE_SET");
    }

    public OperationDataBuilder testCaseSetDelete(Object testCaseSetEntity) {
        return buildOperation(testCaseSetEntity, "DELETE", "TEST_CASE_SET");
    }

    // 通用操作相关方法
    public OperationDataBuilder operation(Object entity, String operationType, String operationTarget) {
        return fromEntity(entity)
               .add("operationType", operationType)
               .add("operationTarget", operationTarget);
    }

    // 自定义数据方法
    public OperationDataBuilder custom(String key, Object value) {
        return add(key, value);
    }

    // 便捷方法：添加操作描述
    public OperationDataBuilder withDescription(String descriptionZh, String descriptionEn) {
        return add("descriptionZh", descriptionZh)
               .add("descriptionEn", descriptionEn);
    }

    // 便捷方法：添加时间戳
    public OperationDataBuilder withTimestamp() {
        return add("timestamp", System.currentTimeMillis());
    }

    // 便捷方法：添加操作者信息
    public OperationDataBuilder withOperator(String operatorUsername) {
        return add("operatorUsername", operatorUsername);
    }
}
