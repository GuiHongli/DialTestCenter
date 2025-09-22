package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.UserRolesApi;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.util.UserRoleOperationLogUtil;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户角色管理控制器
 */
@RestController
@RequestMapping("/api")
public class UserRoleController implements UserRolesApi {
    
    @Autowired
    private UserRoleService userRoleService;
    
    @Autowired
    private UserRoleOperationLogUtil operationLogUtil;
    
    @Override
    public ResponseEntity<UserRolePageResponse> userRolesGet(String xUsername, Integer page, Integer size, String search) {
        try {
            // 检查权限（需要ADMIN或OPERATOR权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN") && !userRoles.contains("OPERATOR")) {
                UserRolePageResponse response = new UserRolePageResponse();
                response.setSuccess(false);
                response.setMessage("权限不足，需要ADMIN或OPERATOR权限");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            UserRolePageResponseData data = userRoleService.getUserRolesWithPagination(page, size, search);
            
            UserRolePageResponse response = new UserRolePageResponse();
            response.setSuccess(true);
            response.setData(data);
            response.setMessage("获取用户角色列表成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            UserRolePageResponse response = new UserRolePageResponse();
            response.setSuccess(false);
            response.setMessage("获取用户角色列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<UserRoleResponse> userRolesPost(String xUsername, CreateUserRoleRequest body) {
        try {
            // 检查权限（需要ADMIN权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN")) {
                UserRoleResponse response = new UserRoleResponse();
                response.setSuccess(false);
                response.setMessage("权限不足，需要ADMIN权限");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            UserRole userRole = userRoleService.createUserRole(
                body.getUsername(), 
                body.getRole().toString(), 
                xUsername
            );
            
            UserRoleResponse response = new UserRoleResponse();
            response.setSuccess(true);
            response.setData(userRole);
            response.setMessage("创建用户角色成功");
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            UserRoleResponse response = new UserRoleResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            UserRoleResponse response = new UserRoleResponse();
            response.setSuccess(false);
            response.setMessage("创建用户角色失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<UserRoleResponse> userRolesIdPut(String xUsername, Integer id, UpdateUserRoleRequest body) {
        try {
            // 检查权限（需要ADMIN权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN")) {
                UserRoleResponse response = new UserRoleResponse();
                response.setSuccess(false);
                response.setMessage("权限不足，需要ADMIN权限");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            UserRole userRole = userRoleService.updateUserRole(
                id, 
                body.getUsername(), 
                body.getRole().toString(), 
                xUsername
            );
            
            UserRoleResponse response = new UserRoleResponse();
            response.setSuccess(true);
            response.setData(userRole);
            response.setMessage("更新用户角色成功");
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            UserRoleResponse response = new UserRoleResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            UserRoleResponse response = new UserRoleResponse();
            response.setSuccess(false);
            response.setMessage("更新用户角色失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<Void> userRolesIdDelete(Integer id, String xUsername) {
        try {
            // 检查权限（需要ADMIN权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            userRoleService.deleteUserRole(id, xUsername);
            
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Override
    public ResponseEntity<PermissionCheckResponse> userRolesCheckPermissionPost(String xUsername, CheckPermissionRequest body) {
        try {
            List<String> userRoles = userRoleService.getUserRolesByUsername(body.getUsername());
            boolean hasPermission = userRoleService.hasAnyRole(body.getUsername(), body.getRoles());
            
            PermissionCheckResponseData data = new PermissionCheckResponseData();
            data.setHasPermission(hasPermission);
            data.setUserRoles(userRoles);
            
            PermissionCheckResponse response = new PermissionCheckResponse();
            response.setSuccess(true);
            response.setData(data);
            response.setMessage("权限检查完成");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PermissionCheckResponse response = new PermissionCheckResponse();
            response.setSuccess(false);
            response.setMessage("权限检查失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @Override
    public ResponseEntity<List<RoleResponse>> userRolesRolesGet(String xUsername) {
        try {
            List<Role> roles = userRoleService.getAllRoles();
            
            List<RoleResponse> responses = roles.stream().map(role -> {
                RoleResponse response = new RoleResponse();
                response.setSuccess(true);
                response.setData(role);
                response.setMessage("获取角色信息成功");
                return response;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            RoleResponse errorResponse = new RoleResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("获取角色列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Arrays.asList(errorResponse));
        }
    }
}
