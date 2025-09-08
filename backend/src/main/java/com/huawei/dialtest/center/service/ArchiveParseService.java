/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.service;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 压缩包解析服务类，用于解析ZIP和TAR.GZ格式的压缩包
 * 提取cases.xlsx文件和scripts目录下的Python脚本文件
 * 支持文件内容提取和文件名列表获取
 *
 * @author g00940940
 * @since 2025-09-08
 */
@Service
public class ArchiveParseService {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveParseService.class);

    private static final String CASES_EXCEL_FILE = "cases.xlsx";
    private static final String SCRIPTS_DIR = "scripts/";
    private static final String PYTHON_EXTENSION = ".py";

    /**
     * 解析压缩包并提取cases.xlsx文件内容
     *
     * @param archiveData 压缩包字节数据
     * @param fileFormat 文件格式（zip或tar.gz）
     * @return cases.xlsx文件的字节内容，如果未找到则返回null
     * @throws IOException 解析过程中发生IO异常时抛出
     */
    public byte[] extractCasesExcel(byte[] archiveData, String fileFormat) throws IOException {
        logger.debug("Extracting cases.xlsx from {} archive", fileFormat);
        
        try (InputStream inputStream = new ByteArrayInputStream(archiveData);
             ArchiveInputStream archiveInputStream = createArchiveInputStream(inputStream, fileFormat)) {
            
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && CASES_EXCEL_FILE.equals(entry.getName())) {
                    logger.info("Found cases.xlsx file in archive");
                    return readEntryContent(archiveInputStream, entry.getSize());
                }
            }
        }
        
        logger.warn("cases.xlsx file not found in archive");
        return null;
    }

    /**
     * 解析压缩包并获取scripts目录下的Python脚本文件名列表
     *
     * @param archiveData 压缩包字节数据
     * @param fileFormat 文件格式（zip或tar.gz）
     * @return Python脚本文件名列表（不包含路径）
     * @throws IOException 解析过程中发生IO异常时抛出
     */
    public List<String> extractScriptFileNames(byte[] archiveData, String fileFormat) throws IOException {
        logger.debug("Extracting script file names from {} archive", fileFormat);
        
        List<String> scriptFiles = new ArrayList<>();
        
        try (InputStream inputStream = new ByteArrayInputStream(archiveData);
             ArchiveInputStream archiveInputStream = createArchiveInputStream(inputStream, fileFormat)) {
            
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && 
                    entry.getName().startsWith(SCRIPTS_DIR) && 
                    entry.getName().toLowerCase().endsWith(PYTHON_EXTENSION)) {
                    
                    String fileName = entry.getName().substring(SCRIPTS_DIR.length());
                    scriptFiles.add(fileName);
                    logger.debug("Found script file: {}", fileName);
                }
            }
        }
        
        logger.info("Found {} script files in archive", scriptFiles.size());
        return scriptFiles;
    }

    /**
     * 验证压缩包是否包含必要的文件结构
     *
     * @param archiveData 压缩包字节数据
     * @param fileFormat 文件格式（zip或tar.gz）
     * @return 验证结果，包含是否包含cases.xlsx和scripts目录
     * @throws IOException 解析过程中发生IO异常时抛出
     */
    public ArchiveValidationResult validateArchive(byte[] archiveData, String fileFormat) throws IOException {
        logger.debug("Validating {} archive structure", fileFormat);
        
        boolean hasCasesExcel = false;
        boolean hasScriptsDir = false;
        int scriptCount = 0;
        
        try (InputStream inputStream = new ByteArrayInputStream(archiveData);
             ArchiveInputStream archiveInputStream = createArchiveInputStream(inputStream, fileFormat)) {
            
            ArchiveEntry entry;
            while ((entry = archiveInputStream.getNextEntry()) != null) {
                String entryName = entry.getName();
                
                if (CASES_EXCEL_FILE.equals(entryName)) {
                    hasCasesExcel = true;
                    logger.debug("Found cases.xlsx file");
                } else if (entryName.startsWith(SCRIPTS_DIR)) {
                    hasScriptsDir = true;
                    if (!entry.isDirectory() && entryName.toLowerCase().endsWith(PYTHON_EXTENSION)) {
                        scriptCount++;
                    }
                }
            }
        }
        
        ArchiveValidationResult result = new ArchiveValidationResult(hasCasesExcel, hasScriptsDir, scriptCount);
        logger.info("Archive validation result: {}", result);
        return result;
    }

    /**
     * 创建压缩包输入流
     *
     * @param inputStream 原始输入流
     * @param fileFormat 文件格式
     * @return 压缩包输入流
     * @throws IOException 创建流时发生异常
     */
    private ArchiveInputStream createArchiveInputStream(InputStream inputStream, String fileFormat) throws IOException {
        if ("zip".equalsIgnoreCase(fileFormat)) {
            return new ZipArchiveInputStream(inputStream);
        } else if ("tar.gz".equalsIgnoreCase(fileFormat)) {
            GzipCompressorInputStream gzipInputStream = new GzipCompressorInputStream(inputStream);
            return new TarArchiveInputStream(gzipInputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + fileFormat);
        }
    }

    /**
     * 读取压缩包条目内容
     *
     * @param archiveInputStream 压缩包输入流
     * @param size 条目大小
     * @return 条目内容字节数组
     * @throws IOException 读取过程中发生异常
     */
    private byte[] readEntryContent(ArchiveInputStream archiveInputStream, long size) throws IOException {
        // 如果大小为-1（未知大小），使用ByteArrayOutputStream动态读取
        if (size == -1) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = archiveInputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        }
        
        if (size > Integer.MAX_VALUE) {
            throw new IOException("Entry size too large: " + size);
        }
        
        byte[] content = new byte[(int) size];
        int totalRead = 0;
        int bytesRead;
        
        while (totalRead < content.length) {
            bytesRead = archiveInputStream.read(content, totalRead, content.length - totalRead);
            if (bytesRead == -1) {
                break;
            }
            totalRead += bytesRead;
        }
        
        return content;
    }

    /**
     * 压缩包验证结果类
     */
    public static class ArchiveValidationResult {
        private final boolean hasCasesExcel;
        private final boolean hasScriptsDir;
        private final int scriptCount;

        public ArchiveValidationResult(boolean hasCasesExcel, boolean hasScriptsDir, int scriptCount) {
            this.hasCasesExcel = hasCasesExcel;
            this.hasScriptsDir = hasScriptsDir;
            this.scriptCount = scriptCount;
        }

        public boolean isHasCasesExcel() {
            return hasCasesExcel;
        }

        public boolean isHasScriptsDir() {
            return hasScriptsDir;
        }

        public int getScriptCount() {
            return scriptCount;
        }

        public boolean isValid() {
            return hasCasesExcel && hasScriptsDir;
        }

        @Override
        public String toString() {
            return "ArchiveValidationResult{" +
                   "hasCasesExcel=" + hasCasesExcel +
                   ", hasScriptsDir=" + hasScriptsDir +
                   ", scriptCount=" + scriptCount +
                   '}';
        }
    }
}
