package com.huawei.cloududn.dialingtest.exception;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.core.MethodParameter;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * 全局异常处理器测试类
 * 
 * @author DialTestCenter
 * @version 1.0.0
 * @since 2024-01-01
 */
@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerTest {
    
    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;
    
    @Mock
    private HttpServletRequest request;
    
    @Before
    public void setUp() {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("GET");
    }
    
    
    /**
     * 测试处理参数验证异常
     */
    @Test
    public void testHandleValidationException_Success_ReturnsBadRequest() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleValidationException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("VALIDATION_ERROR", body.get("errorCode"));
        assertEquals(400, body.get("statusCode"));
    }
    
    /**
     * 测试处理约束违反异常
     */
    @Test
    public void testHandleConstraintViolationException_Success_ReturnsBadRequest() {
        // Arrange
        String message = "约束违反";
        ConstraintViolationException exception = new ConstraintViolationException(message, null);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleConstraintViolationException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("CONSTRAINT_VIOLATION", body.get("errorCode"));
        assertEquals(message, body.get("message"));
        assertEquals(400, body.get("statusCode"));
    }
    
    /**
     * 测试处理缺少请求参数异常
     */
    @Test
    public void testHandleMissingParameterException_Success_ReturnsBadRequest() {
        // Arrange
        String parameterName = "id";
        MissingServletRequestParameterException exception = new MissingServletRequestParameterException(parameterName, "String");
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleMissingParameterException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("MISSING_PARAMETER", body.get("errorCode"));
        assertTrue(((String) body.get("message")).contains(parameterName));
        assertEquals(400, body.get("statusCode"));
    }
    
    /**
     * 测试处理方法参数类型不匹配异常
     */
    @Test
    public void testHandleTypeMismatchException_Success_ReturnsBadRequest() {
        // Arrange
        String parameterName = "id";
        Class<?> requiredType = Integer.class;
        MethodParameter methodParameter = mock(MethodParameter.class);
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException("invalid", requiredType, parameterName, methodParameter, new RuntimeException());
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleTypeMismatchException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("TYPE_MISMATCH", body.get("errorCode"));
        assertTrue(((String) body.get("message")).contains(parameterName));
        assertEquals(400, body.get("statusCode"));
    }
    
    /**
     * 测试处理文件上传大小超限异常
     */
    @Test
    public void testHandleMaxUploadSizeExceededException_Success_ReturnsPayloadTooLarge() {
        // Arrange
        long maxSize = 10485760L; // 10MB
        MaxUploadSizeExceededException exception = new MaxUploadSizeExceededException(maxSize);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleMaxUploadSizeExceededException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.PAYLOAD_TOO_LARGE, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("FILE_SIZE_EXCEEDED", body.get("errorCode"));
        assertEquals(413, body.get("statusCode"));
    }
    
    /**
     * 测试处理404异常
     */
    @Test
    public void testHandleNoHandlerFoundException_Success_ReturnsNotFound() {
        // Arrange
        NoHandlerFoundException exception = new NoHandlerFoundException("GET", "/api/notfound", org.springframework.http.HttpHeaders.EMPTY);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleNoHandlerFoundException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("NOT_FOUND", body.get("errorCode"));
        assertEquals(404, body.get("statusCode"));
    }
    
    /**
     * 测试处理非法参数异常
     */
    @Test
    public void testHandleIllegalArgumentException_Success_ReturnsBadRequest() {
        // Arrange
        String message = "非法参数";
        IllegalArgumentException exception = new IllegalArgumentException(message);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleIllegalArgumentException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("ILLEGAL_ARGUMENT", body.get("errorCode"));
        assertEquals(message, body.get("message"));
        assertEquals(400, body.get("statusCode"));
    }
    
    /**
     * 测试处理空指针异常
     */
    @Test
    public void testHandleNullPointerException_Success_ReturnsInternalServerError() {
        // Arrange
        NullPointerException exception = new NullPointerException("空指针异常");
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleNullPointerException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("NULL_POINTER", body.get("errorCode"));
        assertEquals("系统内部错误", body.get("message"));
        assertEquals(500, body.get("statusCode"));
    }
    
    /**
     * 测试处理运行时异常
     */
    @Test
    public void testHandleRuntimeException_Success_ReturnsInternalServerError() {
        // Arrange
        String message = "运行时异常";
        RuntimeException exception = new RuntimeException(message);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleRuntimeException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("RUNTIME_ERROR", body.get("errorCode"));
        assertEquals("系统运行时错误", body.get("message"));
        assertEquals(500, body.get("statusCode"));
    }
    
    /**
     * 测试处理通用异常
     */
    @Test
    public void testHandleGenericException_Success_ReturnsInternalServerError() {
        // Arrange
        String message = "通用异常";
        Exception exception = new Exception(message);
        
        // Act
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGenericException(exception, request);
        
        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(false, body.get("success"));
        assertEquals("INTERNAL_ERROR", body.get("errorCode"));
        assertEquals("系统内部错误", body.get("message"));
        assertEquals(500, body.get("statusCode"));
    }
}
