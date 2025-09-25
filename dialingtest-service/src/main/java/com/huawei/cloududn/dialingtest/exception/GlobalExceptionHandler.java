package com.huawei.cloududn.dialingtest.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理应用程序中的异常，并将异常信息记录到日志中
 * 
 * @author DialTestCenter
 * @version 1.0.0
 * @since 2024-01-01
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    
    /**
     * 处理参数验证异常
     * 
     * @param e 参数验证异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.error("参数验证异常 - URI: {}, Method: {}, Message: {}", 
                request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        
        StringBuilder errorMessage = new StringBuilder("参数验证失败: ");
        e.getBindingResult().getFieldErrors().forEach(error -> 
                errorMessage.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; "));
        
        Map<String, Object> response = createErrorResponse("VALIDATION_ERROR", errorMessage.toString(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理约束违反异常
     * 
     * @param e 约束违反异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        logger.error("约束违反异常 - URI: {}, Method: {}, Message: {}", 
                request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        
        Map<String, Object> response = createErrorResponse("CONSTRAINT_VIOLATION", e.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理缺少请求参数异常
     * 
     * @param e 缺少请求参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        logger.error("缺少请求参数异常 - URI: {}, Method: {}, Parameter: {}", 
                request.getRequestURI(), request.getMethod(), e.getParameterName(), e);
        
        String message = "缺少必需的请求参数: " + e.getParameterName();
        Map<String, Object> response = createErrorResponse("MISSING_PARAMETER", message, 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理方法参数类型不匹配异常
     * 
     * @param e 方法参数类型不匹配异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        logger.error("参数类型不匹配异常 - URI: {}, Method: {}, Parameter: {}, Required Type: {}", 
                request.getRequestURI(), request.getMethod(), e.getName(), 
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型", e);
        
        String message = String.format("参数 '%s' 类型不匹配，期望类型: %s", e.getName(), 
                e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型");
        Map<String, Object> response = createErrorResponse("TYPE_MISMATCH", message, 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理文件上传大小超限异常
     * 
     * @param e 文件上传大小超限异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e, HttpServletRequest request) {
        logger.error("文件上传大小超限异常 - URI: {}, Method: {}, Max Size: {}", 
                request.getRequestURI(), request.getMethod(), e.getMaxUploadSize(), e);
        
        String message = "文件上传大小超过限制，最大允许: " + (e.getMaxUploadSize() / 1024 / 1024) + "MB";
        Map<String, Object> response = createErrorResponse("FILE_SIZE_EXCEEDED", message, 413);
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(response);
    }
    
    /**
     * 处理404异常
     * 
     * @param e 404异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        logger.error("404异常 - URI: {}, Method: {}", request.getRequestURI(), request.getMethod(), e);
        
        String message = "请求的资源不存在: " + e.getRequestURL();
        Map<String, Object> response = createErrorResponse("NOT_FOUND", message, 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * 处理非法参数异常
     * 
     * @param e 非法参数异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.error("非法参数异常 - URI: {}, Method: {}, Message: {}", 
                request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        
        Map<String, Object> response = createErrorResponse("ILLEGAL_ARGUMENT", e.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * 处理空指针异常
     * 
     * @param e 空指针异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Map<String, Object>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常 - URI: {}, Method: {}", request.getRequestURI(), request.getMethod(), e);
        
        Map<String, Object> response = createErrorResponse("NULL_POINTER", "系统内部错误", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理运行时异常
     * 
     * @param e 运行时异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常 - URI: {}, Method: {}, Message: {}", 
                request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        
        Map<String, Object> response = createErrorResponse("RUNTIME_ERROR", "系统运行时错误", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 处理所有其他异常
     * 
     * @param e 异常
     * @param request HTTP请求
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e, HttpServletRequest request) {
        logger.error("未知异常 - URI: {}, Method: {}, Message: {}", 
                request.getRequestURI(), request.getMethod(), e.getMessage(), e);
        
        Map<String, Object> response = createErrorResponse("INTERNAL_ERROR", "系统内部错误", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 创建错误响应对象
     * 
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param statusCode HTTP状态码
     * @return 错误响应Map
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message, int statusCode) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("statusCode", statusCode);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }
}
