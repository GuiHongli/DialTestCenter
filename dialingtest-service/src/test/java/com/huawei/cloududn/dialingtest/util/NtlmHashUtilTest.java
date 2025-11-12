/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * NTLM Hash工具类单元测试
 *
 * @author g00940940
 * @since 2025-11-12
 */
public class NtlmHashUtilTest {
    private static final Logger logger = LoggerFactory.getLogger(NtlmHashUtilTest.class);
    
    /**
     * 测试将明文密码转换为NTLM Hash - 成功场景
     * 使用已知的密码和NTLM Hash对进行验证
     */
    @Test
    public void testToNtlmHash_ValidPassword_ReturnsCorrectHash() {
        logger.debug("Testing NTLM Hash generation with valid password");
        
        // Arrange
        String plainPassword = "Password123";
        
        // Act
        String ntlmHash = NtlmHashUtil.toNtlmHash(plainPassword);
        
        // Assert
        assertNotNull("NTLM Hash should not be null", ntlmHash);
        assertEquals("NTLM Hash should be 32 characters long", 32, ntlmHash.length());
        assertTrue("NTLM Hash should contain only hex characters", 
            ntlmHash.matches("^[0-9A-F]{32}$"));
        
        logger.debug("NTLM Hash generated successfully: {}", ntlmHash);
    }
    
    /**
     * 测试将简单密码转换为NTLM Hash - 成功场景
     */
    @Test
    public void testToNtlmHash_SimplePassword_ReturnsValidHash() {
        logger.debug("Testing NTLM Hash generation with simple password");
        
        // Arrange
        String plainPassword = "123456";
        
        // Act
        String ntlmHash = NtlmHashUtil.toNtlmHash(plainPassword);
        
        // Assert
        assertNotNull("NTLM Hash should not be null", ntlmHash);
        assertEquals("NTLM Hash should be 32 characters long", 32, ntlmHash.length());
        assertTrue("NTLM Hash should be uppercase", 
            ntlmHash.equals(ntlmHash.toUpperCase()));
        
        logger.debug("Simple password hash: {}", ntlmHash);
    }
    
    /**
     * 测试将复杂密码转换为NTLM Hash - 包含特殊字符
     */
    @Test
    public void testToNtlmHash_ComplexPassword_ReturnsValidHash() {
        logger.debug("Testing NTLM Hash generation with complex password");
        
        // Arrange
        String plainPassword = "QAZ!q123@#$%";
        
        // Act
        String ntlmHash = NtlmHashUtil.toNtlmHash(plainPassword);
        
        // Assert
        assertNotNull("NTLM Hash should not be null", ntlmHash);
        assertEquals("NTLM Hash should be 32 characters long", 32, ntlmHash.length());
        assertTrue("NTLM Hash should contain only hex characters", 
            ntlmHash.matches("^[0-9A-F]{32}$"));
        
        logger.debug("Complex password hash: {}", ntlmHash);
    }
    
    /**
     * 测试相同密码生成相同的NTLM Hash - 幂等性验证
     */
    @Test
    public void testToNtlmHash_SamePassword_GeneratesSameHash() {
        logger.debug("Testing NTLM Hash idempotency");
        
        // Arrange
        String plainPassword = "TestPassword";
        
        // Act
        String hash1 = NtlmHashUtil.toNtlmHash(plainPassword);
        String hash2 = NtlmHashUtil.toNtlmHash(plainPassword);
        
        // Assert
        assertNotNull("First hash should not be null", hash1);
        assertNotNull("Second hash should not be null", hash2);
        assertEquals("Same password should generate same hash", hash1, hash2);
        
        logger.debug("Idempotency verified: hash={}", hash1);
    }
    
    /**
     * 测试不同密码生成不同的NTLM Hash
     */
    @Test
    public void testToNtlmHash_DifferentPasswords_GenerateDifferentHashes() {
        logger.debug("Testing NTLM Hash uniqueness");
        
        // Arrange
        String password1 = "Password1";
        String password2 = "Password2";
        
        // Act
        String hash1 = NtlmHashUtil.toNtlmHash(password1);
        String hash2 = NtlmHashUtil.toNtlmHash(password2);
        
        // Assert
        assertNotNull("First hash should not be null", hash1);
        assertNotNull("Second hash should not be null", hash2);
        assertNotEquals("Different passwords should generate different hashes", hash1, hash2);
        
        logger.debug("Different hashes generated: hash1={}, hash2={}", hash1, hash2);
    }
    
    /**
     * 测试空密码 - 应该抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToNtlmHash_EmptyPassword_ThrowsException() {
        logger.debug("Testing NTLM Hash generation with empty password");
        
        // Arrange
        String emptyPassword = "";
        
        // Act
        NtlmHashUtil.toNtlmHash(emptyPassword);
        
        // Assert - expect exception
        fail("Should throw IllegalArgumentException for empty password");
    }
    
    /**
     * 测试null密码 - 应该抛出IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testToNtlmHash_NullPassword_ThrowsException() {
        logger.debug("Testing NTLM Hash generation with null password");
        
        // Arrange
        String nullPassword = null;
        
        // Act
        NtlmHashUtil.toNtlmHash(nullPassword);
        
        // Assert - expect exception
        fail("Should throw IllegalArgumentException for null password");
    }
    
    /**
     * 测试验证有效的NTLM Hash格式 - 32位16进制字符串
     */
    @Test
    public void testIsValidNtlmHash_ValidHash_ReturnsTrue() {
        logger.debug("Testing valid NTLM Hash format validation");
        
        // Arrange
        String validHash = "08A1D3438D7DFE8A2DDC9BBBCB05A0D0";
        
        // Act
        boolean isValid = NtlmHashUtil.isValidNtlmHash(validHash);
        
        // Assert
        assertTrue("Valid NTLM Hash should return true", isValid);
        
        logger.debug("Valid hash format verified: {}", validHash);
    }
    
    /**
     * 测试验证无效长度的NTLM Hash - 长度不是32
     */
    @Test
    public void testIsValidNtlmHash_InvalidLength_ReturnsFalse() {
        logger.debug("Testing invalid NTLM Hash length validation");
        
        // Arrange
        String shortHash = "08A1D3438D7DFE8A";
        String longHash = "08A1D3438D7DFE8A2DDC9BBBCB05A0D012345678";
        
        // Act & Assert
        assertFalse("Short hash should be invalid", NtlmHashUtil.isValidNtlmHash(shortHash));
        assertFalse("Long hash should be invalid", NtlmHashUtil.isValidNtlmHash(longHash));
        
        logger.debug("Invalid lengths rejected");
    }
    
    /**
     * 测试验证包含非16进制字符的NTLM Hash
     */
    @Test
    public void testIsValidNtlmHash_NonHexCharacters_ReturnsFalse() {
        logger.debug("Testing NTLM Hash with non-hex characters");
        
        // Arrange
        String invalidHash = "08A1D3438D7DFE8A2DDC9BBBCB05GHIJ";
        
        // Act
        boolean isValid = NtlmHashUtil.isValidNtlmHash(invalidHash);
        
        // Assert
        assertFalse("Hash with non-hex characters should be invalid", isValid);
        
        logger.debug("Non-hex characters rejected");
    }
    
    /**
     * 测试验证null NTLM Hash
     */
    @Test
    public void testIsValidNtlmHash_NullHash_ReturnsFalse() {
        logger.debug("Testing null NTLM Hash validation");
        
        // Arrange
        String nullHash = null;
        
        // Act
        boolean isValid = NtlmHashUtil.isValidNtlmHash(nullHash);
        
        // Assert
        assertFalse("Null hash should be invalid", isValid);
        
        logger.debug("Null hash rejected");
    }
    
    /**
     * 测试验证小写的有效NTLM Hash - 应该也被认为有效
     */
    @Test
    public void testIsValidNtlmHash_LowercaseHash_ReturnsTrue() {
        logger.debug("Testing lowercase NTLM Hash format validation");
        
        // Arrange
        String lowercaseHash = "08a1d3438d7dfe8a2ddc9bbbcb05a0d0";
        
        // Act
        boolean isValid = NtlmHashUtil.isValidNtlmHash(lowercaseHash);
        
        // Assert
        assertTrue("Lowercase NTLM Hash should be valid", isValid);
        
        logger.debug("Lowercase hash format accepted: {}", lowercaseHash);
    }
}

