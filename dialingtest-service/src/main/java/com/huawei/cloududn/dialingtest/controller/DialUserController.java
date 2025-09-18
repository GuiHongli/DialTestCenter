package com.huawei.cloududn.dialingtest.controller;

import com.huawei.cloududn.dialingtest.api.DialusersApi;
import com.huawei.cloududn.dialingtest.model.*;
import com.huawei.cloududn.dialingtest.service.DialUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
     * 修改拨测用户
     * 
     * @param id 用户ID
     * @param body 更新请求
     * @return 更新后的用户信息
     */
    @Override
    public ResponseEntity<DialUserResponse> dialusersIdPut(Integer id, UpdateDialUserRequest body) {
        try {
            DialUser updatedUser = dialUserService.updateUser(id, body.getUsername(), body.getPassword());
            
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
     * @return 删除结果
     */
    @Override
    public ResponseEntity<Void> dialusersIdDelete(Integer id) {
        try {
            dialUserService.deleteUser(id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 新增拨测用户
     * 
     * @param body 创建请求
     * @return 创建的用户信息
     */
    @Override
    public ResponseEntity<DialUserResponse> dialusersPost(CreateDialUserRequest body) {
        try {
            DialUser createdUser = dialUserService.createUser(body.getUsername(), body.getPassword());
            
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
