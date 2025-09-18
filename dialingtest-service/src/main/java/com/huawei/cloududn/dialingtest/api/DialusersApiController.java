/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.huawei.cloududn.dialingtest.model.CreateDialUserRequest;
import com.huawei.cloududn.dialingtest.model.DialUserPageResponse;
import com.huawei.cloududn.dialingtest.model.DialUserResponse;
import com.huawei.cloududn.dialingtest.model.UpdateDialUserRequest;
import com.huawei.cloududn.dialingtest.service.DialUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.CookieValue;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 拨测用户API控制器实现
 *
 * @author g00940940
 * @since 2025-09-18
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-09-18T16:05:08.624+08:00[Asia/Shanghai]")
@Validated
@org.springframework.web.bind.annotation.RestController
public class DialusersApiController implements DialusersApi {

    private static final Logger log = LoggerFactory.getLogger(DialusersApiController.class);

    private final ObjectMapper objectMapper;

    private final HttpServletRequest request;

    @Autowired
    private DialUserService dialUserService;

    @org.springframework.beans.factory.annotation.Autowired
    public DialusersApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @Override
    public Optional<ObjectMapper> getObjectMapper(){
        return Optional.ofNullable(objectMapper);
    }

    @Override
    public Optional<HttpServletRequest> getRequest(){
        return Optional.ofNullable(request);
    }

    @Override
    public Optional<String> getAcceptHeader() {
        return getRequest().map(r -> r.getHeader("Accept"));
    }

    @Override
    @Operation(summary = "分页查询拨测用户", description = "", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "*/*", schema = @Schema(implementation = DialUserPageResponse.class))) })
    @RequestMapping(value = "/dialusers",
        produces = { "*/*" }, 
        method = RequestMethod.GET)
    public ResponseEntity<DialUserPageResponse> dialusersGet(@Parameter(in = ParameterIn.QUERY, description = "页码，从0开始" ,schema=@Schema( defaultValue="0")) @Valid @RequestParam(value = "page", required = false, defaultValue="0") Integer page
,@Parameter(in = ParameterIn.QUERY, description = "每页大小" ,schema=@Schema( defaultValue="10")) @Valid @RequestParam(value = "size", required = false, defaultValue="10") Integer size
,@Parameter(in = ParameterIn.QUERY, description = "用户名过滤条件" ,schema=@Schema()) @Valid @RequestParam(value = "username", required = false) String username
) {
        log.info("Getting dial users - page: {}, size: {}, username: {}", page, size, username);
        
        try {
            DialUserPageResponse response = dialUserService.getDialUsers(
                username, 
                page, 
                size
            );
            
            if (response.isSuccess()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error getting dial users", e);
            DialUserPageResponse errorResponse = new DialUserPageResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("服务器内部错误");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Operation(summary = "新增拨测用户", description = "", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "201", description = "创建成功", content = @Content(mediaType = "*/*", schema = @Schema(implementation = DialUserResponse.class))),
        
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        
        @ApiResponse(responseCode = "409", description = "用户名已存在") })
    @RequestMapping(value = "/dialusers",
        produces = { "*/*" }, 
        consumes = { "*/*" }, 
        method = RequestMethod.POST)
    public ResponseEntity<DialUserResponse> dialusersPost(@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody CreateDialUserRequest body
) {
        log.info("Creating dial user with username: {}", body.getUsername());
        
        try {
            DialUserResponse response = dialUserService.createDialUser(body);
            
            if (response.isSuccess()) {
                return new ResponseEntity<>(response, HttpStatus.CREATED);
            } else if ("用户名已存在".equals(response.getMessage())) {
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error creating dial user", e);
            DialUserResponse errorResponse = new DialUserResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("服务器内部错误");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Operation(summary = "根据ID查询拨测用户", description = "", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(mediaType = "*/*", schema = @Schema(implementation = DialUserResponse.class))),
        
        @ApiResponse(responseCode = "404", description = "用户不存在") })
    @RequestMapping(value = "/dialusers/{id}",
        produces = { "*/*" }, 
        method = RequestMethod.GET)
    public ResponseEntity<DialUserResponse> dialusersIdGet(@Parameter(in = ParameterIn.PATH, description = "用户ID", required=true, schema=@Schema()) @PathVariable("id") Integer id
) {
        log.info("Getting dial user by ID: {}", id);
        
        try {
            DialUserResponse response = dialUserService.getDialUserById(id);
            
            if (response.isSuccess()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if ("用户不存在".equals(response.getMessage())) {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else {
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (Exception e) {
            log.error("Error getting dial user by ID: {}", id, e);
            DialUserResponse errorResponse = new DialUserResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("服务器内部错误");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Operation(summary = "修改拨测用户", description = "", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "修改成功", content = @Content(mediaType = "*/*", schema = @Schema(implementation = DialUserResponse.class))),
        
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        
        @ApiResponse(responseCode = "404", description = "用户不存在"),
        
        @ApiResponse(responseCode = "409", description = "用户名已存在") })
    @RequestMapping(value = "/dialusers/{id}",
        produces = { "*/*" }, 
        consumes = { "*/*" }, 
        method = RequestMethod.PUT)
    public ResponseEntity<DialUserResponse> dialusersIdPut(@Parameter(in = ParameterIn.PATH, description = "用户ID", required=true, schema=@Schema()) @PathVariable("id") Integer id
,@Parameter(in = ParameterIn.DEFAULT, description = "", required=true, schema=@Schema()) @Valid @RequestBody UpdateDialUserRequest body
) {
        log.info("Updating dial user with ID: {}", id);
        
        try {
            DialUserResponse response = dialUserService.updateDialUser(id, body);
            
            if (response.isSuccess()) {
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else if ("用户不存在".equals(response.getMessage())) {
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            } else if ("用户名已存在".equals(response.getMessage())) {
                return new ResponseEntity<>(response, HttpStatus.CONFLICT);
            } else {
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error updating dial user with ID: {}", id, e);
            DialUserResponse errorResponse = new DialUserResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage("服务器内部错误");
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @Operation(summary = "删除拨测用户", description = "", tags={  })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "204", description = "删除成功"),
        
        @ApiResponse(responseCode = "404", description = "用户不存在") })
    @RequestMapping(value = "/dialusers/{id}",
        method = RequestMethod.DELETE)
    public ResponseEntity<Void> dialusersIdDelete(@Parameter(in = ParameterIn.PATH, description = "用户ID", required=true, schema=@Schema()) @PathVariable("id") Integer id
) {
        log.info("Deleting dial user with ID: {}", id);
        
        try {
            boolean deleted = dialUserService.deleteDialUser(id);
            
            if (deleted) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error deleting dial user with ID: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
