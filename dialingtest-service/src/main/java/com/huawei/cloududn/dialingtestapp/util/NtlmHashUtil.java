/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * NTLM Hash工具类
 * 用于将明文密码转换为NTLM Hash格式，支持执行机CHAP认证
 *
 * @author g00940940
 * @since 2025-11-12
 */
public class NtlmHashUtil {
    private static final Logger logger = LoggerFactory.getLogger(NtlmHashUtil.class);
    
    /**
     * 将明文密码转换为NTLM Hash
     * 算法流程：
     * 1. 将明文密码转换为UTF-16LE编码的字节数组
     * 2. 使用MD4算法计算哈希值
     * 3. 将哈希值转换为32位16进制字符串（大写）
     *
     * @param plainPassword 明文密码，如 "QAZ!q123"
     * @return NTLM Hash，如 "08A1D3438D7DFE8A2DDC9BBBCB05A0D0"
     * @throws IllegalArgumentException 如果密码为空
     * @throws IllegalStateException 如果MD4算法不可用
     */
    public static String toNtlmHash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            logger.error("Plain password cannot be null or empty");
            throw new IllegalArgumentException("Plain password cannot be null or empty");
        }
        
        try {
            // 1. Unicode编码 (UTF-16LE)
            byte[] unicodeBytes = plainPassword.getBytes(StandardCharsets.UTF_16LE);
            logger.debug("Converting password to NTLM Hash, unicodeBytes.length={}", unicodeBytes.length);
            
            // 2. MD4加密
            MessageDigest md4 = getMd4MessageDigest();
            byte[] hashBytes = md4.digest(unicodeBytes);
            
            // 3. 转16进制字符串（大写）
            String ntlmHash = bytesToHexString(hashBytes);
            logger.debug("NTLM Hash generated successfully, length={}", ntlmHash.length());
            
            return ntlmHash;
        } catch (NoSuchAlgorithmException e) {
            logger.error("MD4 algorithm not available", e);
            throw new IllegalStateException("NTLM Hash generation failed: MD4 algorithm not available", e);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid password format", e);
            throw new IllegalStateException("NTLM Hash generation failed: Invalid password format", e);
        }
    }
    
    /**
     * 获取MD4 MessageDigest实例
     * 优先尝试使用标准名称"MD4"，如果不支持则尝试使用BouncyCastle
     *
     * @return MD4 MessageDigest实例
     * @throws NoSuchAlgorithmException 如果MD4算法不可用
     */
    private static MessageDigest getMd4MessageDigest() throws NoSuchAlgorithmException {
        try {
            // 尝试使用标准MD4算法
            return MessageDigest.getInstance("MD4");
        } catch (NoSuchAlgorithmException e) {
            logger.warn("MD4 algorithm not available via standard API, attempting BouncyCastle");
            
            // 尝试使用BouncyCastle提供商
            try {
                // 动态加载BouncyCastle提供商（如果存在）
                Class<?> providerClass = Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
                java.security.Provider provider = (java.security.Provider) providerClass.getDeclaredConstructor().newInstance();
                return MessageDigest.getInstance("MD4", provider);
            } catch (ClassNotFoundException ex) {
                logger.error("BouncyCastle provider not found in classpath");
                throw new NoSuchAlgorithmException("MD4 algorithm not available. Please add BouncyCastle dependency.", e);
            } catch (NoSuchAlgorithmException ex) {
                throw e;
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Failed to initialize BouncyCastle provider", ex);
            } catch (IllegalAccessException ex) {
                throw new IllegalArgumentException("Failed to access BouncyCastle provider", ex);
            } catch (SecurityException ex) {
                throw new IllegalArgumentException("Security restriction accessing BouncyCastle provider", ex);
            } catch (InstantiationException ex) {
                throw new IllegalArgumentException("Failed to instantiate BouncyCastle provider", ex);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("BouncyCastle provider constructor not found", ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new IllegalArgumentException("Failed to invoke BouncyCastle provider constructor", ex);
            }
        }
    }
    
    /**
     * 将字节数组转换为16进制字符串（大写）
     *
     * @param bytes 字节数组
     * @return 16进制字符串（大写）
     */
    private static String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
    
    /**
     * 验证字符串是否为有效的NTLM Hash格式
     * 有效格式：32位16进制字符串
     *
     * @param ntlmHash 待验证的字符串
     * @return true表示格式有效，false表示格式无效
     */
    public static boolean isValidNtlmHash(String ntlmHash) {
        if (ntlmHash == null) {
            return false;
        }
        
        // NTLM Hash必须是32位16进制字符串
        if (ntlmHash.length() != 32) {
            return false;
        }
        
        // 检查是否全部为16进制字符
        return ntlmHash.matches("^[0-9A-Fa-f]{32}$");
    }
}

