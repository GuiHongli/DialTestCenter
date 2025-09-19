/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.util;

import com.huawei.cloududn.dialingtest.model.OperationLog;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Excel文件生成工具类
 * 用于生成操作记录的Excel导出文件
 *
 * @author g00940940
 * @since 2025-01-15
 */
public class ExcelUtil {
    
    /**
     * 生成操作记录Excel文件
     *
     * @param logs 操作记录列表
     * @return Excel文件资源
     */
    public static Resource generateOperationLogsExcel(List<OperationLog> logs) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("操作记录");
            
            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = getHeaders();
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (OperationLog log : logs) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(log.getId() != null ? log.getId() : 0);
                row.createCell(1).setCellValue(log.getUsername() != null ? log.getUsername() : "");
                row.createCell(2).setCellValue(log.getOperationType() != null ? log.getOperationType() : "");
                row.createCell(3).setCellValue(log.getOperationTarget() != null ? log.getOperationTarget() : "");
                
                // 设置中文描述
                row.createCell(4).setCellValue(log.getOperationDescriptionZh() != null ? log.getOperationDescriptionZh() : "");
                // 设置英文描述
                row.createCell(5).setCellValue(log.getOperationDescriptionEn() != null ? log.getOperationDescriptionEn() : "");
                row.createCell(6).setCellValue(log.getOperationTime() != null ? log.getOperationTime() : "");
            }
            
            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 生成文件流
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            byte[] excelBytes = outputStream.toByteArray();
            return new ByteArrayResource(excelBytes);
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }
    
    /**
     * 获取表头
     *
     * @return 表头数组
     */
    private static String[] getHeaders() {
        return new String[]{"ID", "用户名", "操作类型", "操作目标", 
                              "中文描述", "英文描述", "操作时间"};
    }
    
    /**
     * 创建表头样式
     *
     * @param workbook 工作簿
     * @return 表头样式
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }
}
