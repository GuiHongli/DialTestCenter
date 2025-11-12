// /*
//  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
//  */

// package com.huawei.cloududn.dialingtest.util;

// import com.huawei.cloududn.dialingtest.model.DialUser;
// import com.huawei.cloududn.dialingtest.model.UserRole;
// import com.huawei.cloududn.dialingtest.service.OperationLogService;

// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.MockitoJUnitRunner;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// /**
//  * OperationLogUtil 用户与角色相关方法 LLT
//  *
//  * 覆盖：logUserCreate/logUserUpdate/logUserDelete
//  *      logUserRoleCreate/logUserRoleUpdate/logUserRoleDelete
//  */
// @RunWith(MockitoJUnitRunner.class)
// public class OperationLogUtilUserAndRoleTest {

//     @Mock
//     private OperationLogService operationLogService;

//     @InjectMocks
//     private OperationLogUtil operationLogUtil;

//     @Before
//     public void setUp() {
//         when(operationLogService.createOperationLog(any())).thenReturn(null);
//     }

//     /**
//      * 测试用户创建操作记录
//      */
//     @Test
//     public void testLogUserCreate_CallsService() {
//         String operator = "admin";
//         String target = "u1";
//         DialUser user = new DialUser();
//         user.setUsername(target);
//         user.setDisplayName("User One");

//         operationLogUtil.logUserCreate(operator, target, user);

//         verify(operationLogService).createOperationLog(any());
//     }

//     /**
//      * 测试用户更新操作记录
//      */
//     @Test
//     public void testLogUserUpdate_CallsService() {
//         String operator = "admin";
//         String target = "u2";
//         DialUser oldUser = new DialUser();
//         oldUser.setUsername(target);
//         oldUser.setDisplayName("Old");
//         DialUser newUser = new DialUser();
//         newUser.setUsername(target);
//         newUser.setDisplayName("New");

//         operationLogUtil.logUserUpdate(operator, target, oldUser, newUser);

//         verify(operationLogService).createOperationLog(any());
//     }

//     /**
//      * 测试用户删除操作记录
//      */
//     @Test
//     public void testLogUserDelete_CallsService() {
//         String operator = "admin";
//         String target = "u3";
//         DialUser info = new DialUser();
//         info.setUsername(target);

//         operationLogUtil.logUserDelete(operator, target, info);

//         verify(operationLogService).createOperationLog(any());
//     }

//     /**
//      * 测试用户角色创建操作记录
//      */
//     @Test
//     public void testLogUserRoleCreate_CallsService() {
//         String operator = "admin";
//         UserRole userRole = new UserRole();
//         userRole.setUsername("u4");
//         userRole.setRole("ADMIN");

//         operationLogUtil.logUserRoleCreate(operator, userRole);

//         verify(operationLogService).createOperationLog(any());
//     }

//     /**
//      * 测试用户角色更新操作记录
//      */
//     @Test
//     public void testLogUserRoleUpdate_CallsService() {
//         String operator = "admin";
//         UserRole oldRole = new UserRole();
//         oldRole.setUsername("u5");
//         oldRole.setRole("USER");
//         UserRole newRole = new UserRole();
//         newRole.setUsername("u5");
//         newRole.setRole("ADMIN");

//         operationLogUtil.logUserRoleUpdate(operator, oldRole, newRole);

//         verify(operationLogService).createOperationLog(any());
//     }

//     /**
//      * 测试用户角色删除操作记录
//      */
//     @Test
//     public void testLogUserRoleDelete_CallsService() {
//         String operator = "admin";
//         UserRole toDelete = new UserRole();
//         toDelete.setUsername("u6");
//         toDelete.setRole("USER");

//         operationLogUtil.logUserRoleDelete(operator, toDelete);

//         verify(operationLogService).createOperationLog(any());
//     }
// }


