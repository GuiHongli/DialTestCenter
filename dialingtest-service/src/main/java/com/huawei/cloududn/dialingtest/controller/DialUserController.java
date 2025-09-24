package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.DialusersApi;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.DialUserService;
import com.huawei.cloududn.dialingtest.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 拨测用户控制器
 * 实现DialusersApi接口
 * 
 * @author Generated
 */
@RestController
@RequestMapping("/api")
public class DialUserController implements DialusersApi {
    
    @Autowired
    private DialUserService dialUserService;
    
    @Autowired
    private UserRoleService userRoleService;
    
    /**
     * 分页查询拨测用户
     * 
     * @param page 页码，从0开始
     * @param size 每页大小
     * @param username 用户名过滤条件
     * @return 分页用户列表
     */
    @Override
    public ResponseEntity<DialUserPageResponse> dialusersGet(Integer page, Integer size, String username) {
        try {
            // 设置默认值
            if (page == null) {
                page = 0;
            }
            if (size == null) {
                size = 10;
            }
            
            // 查询用户列表和总数
            List<DialUser> users = dialUserService.findUsersWithPagination(page, size, username);
            long totalElements = dialUserService.countUsers(username);
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            // 构建分页响应数据
            DialUserPageResponseData data = new DialUserPageResponseData();
            data.setContent(users);
            data.setTotalElements((int) totalElements);
            data.setTotalPages(totalPages);
            data.setSize(size);
            data.setNumber(page);
            
            // 构建响应
            DialUserPageResponse response = new DialUserPageResponse();
            response.setSuccess(true);
            response.setData(data);
            response.setMessage("查询成功");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (Exception e) {
            DialUserPageResponse response = new DialUserPageResponse();
            response.setSuccess(false);
            response.setMessage("查询失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
    
    /**
     * 根据ID查询拨测用户
     * 
     * @param id 用户ID
     * @return 用户信息
     */
    @Override
    public ResponseEntity<DialUserResponse> dialusersIdGet(Integer id) {
        try {
            DialUser user = dialUserService.findById(id);
            
            if (user == null) {
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("用户不存在");
                
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(true);
            response.setData(user);
            response.setMessage("查询成功");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (Exception e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("查询失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
    
    /**
     * 修改拨测用户
     * 
     * @param xUsername 操作用户名
     * @param id 用户ID
     * @param body 更新请求
     * @return 更新后的用户信息
     */
    @Override
    public ResponseEntity<DialUserResponse> dialusersIdPut(String xUsername, Integer id, UpdateDialUserRequest body) {
        try {
            // 检查用户名是否提供
            if (xUsername == null || xUsername.trim().isEmpty()) {
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("未提供用户名");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 检查权限（需要ADMIN权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN")) {
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("权限不足，仅ADMIN用户可操作");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            DialUser updatedUser = dialUserService.updateUser(id, body.getUsername(), body.getPassword(), xUsername);
            
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(true);
            response.setData(updatedUser);
            response.setMessage("修改成功");
            
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (IllegalArgumentException e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            
            if (e.getMessage().contains("用户不存在")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else if (e.getMessage().contains("用户名已存在")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            
        } catch (IllegalStateException e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("服务器内部错误，请稍后重试");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (Exception e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("修改失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
    
    /**
     * 删除拨测用户
     * 
     * @param id 用户ID
     * @param xUsername 操作用户名
     * @return 删除结果
     */
    @Override
    public ResponseEntity<Void> dialusersIdDelete(Integer id, String xUsername) {
        try {
            // 检查用户名是否提供
            if (xUsername == null || xUsername.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            // 检查权限（需要ADMIN权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            dialUserService.deleteUser(id, xUsername);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
            
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 新增拨测用户
     * 
     * @param xUsername 操作用户名
     * @param body 创建请求
     * @return 创建的用户信息
     */
    @Override
    public ResponseEntity<DialUserResponse> dialusersPost(String xUsername, CreateDialUserRequest body) {
        try {
            // 检查用户名是否提供
            if (xUsername == null || xUsername.trim().isEmpty()) {
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("未提供用户名");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            // 检查权限（需要ADMIN权限）
            List<String> userRoles = userRoleService.getUserRolesByUsername(xUsername);
            if (!userRoles.contains("ADMIN")) {
                DialUserResponse response = new DialUserResponse();
                response.setSuccess(false);
                response.setMessage("权限不足，仅ADMIN用户可操作");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            
            DialUser createdUser = dialUserService.createUser(body.getUsername(), body.getPassword(), xUsername);
            
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(true);
            response.setData(createdUser);
            response.setMessage("创建成功");
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (IllegalArgumentException e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            
            if (e.getMessage().contains("用户名已存在")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
            
        } catch (IllegalStateException e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("服务器内部错误，请稍后重试");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
            
        } catch (Exception e) {
            DialUserResponse response = new DialUserResponse();
            response.setSuccess(false);
            response.setMessage("创建失败: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        }
    }
}
