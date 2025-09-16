/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.
 */

package com.huawei.dialtest.center.dto;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

/**
 * PagedResponse测试类
 *
 * @author g00940940
 * @since 2025-01-27
 */
public class PagedResponseTest {

    @Test
    public void testDefaultConstructor() {
        PagedResponse<String> response = new PagedResponse<>();
        
        assertNull(response.getData());
        assertEquals(0, response.getTotal());
        assertEquals(0, response.getPage());
        assertEquals(0, response.getPageSize());
        assertEquals(0, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    public void testParameterizedConstructor() {
        List<String> testData = Arrays.asList("item1", "item2", "item3");
        long total = 25L;
        int page = 1;
        int pageSize = 10;
        
        PagedResponse<String> response = new PagedResponse<>(testData, total, page, pageSize);
        
        assertEquals(testData, response.getData());
        assertEquals(total, response.getTotal());
        assertEquals(page, response.getPage());
        assertEquals(pageSize, response.getPageSize());
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    public void testTotalPagesCalculation() {
        List<String> testData = Arrays.asList("item1", "item2");
        long total = 25L;
        int page = 0;
        int pageSize = 10;
        
        PagedResponse<String> response = new PagedResponse<>(testData, total, page, pageSize);
        
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    public void testLastPage() {
        List<String> testData = Arrays.asList("item1", "item2");
        long total = 25L;
        int page = 2;
        int pageSize = 10;
        
        PagedResponse<String> response = new PagedResponse<>(testData, total, page, pageSize);
        
        assertEquals(3, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    public void testMiddlePage() {
        List<String> testData = Arrays.asList("item1", "item2");
        long total = 25L;
        int page = 1;
        int pageSize = 10;
        
        PagedResponse<String> response = new PagedResponse<>(testData, total, page, pageSize);
        
        assertEquals(3, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }

    @Test
    public void testSettersAndGetters() {
        PagedResponse<String> response = new PagedResponse<>();
        
        List<String> testData = Arrays.asList("item1", "item2");
        response.setData(testData);
        response.setTotal(100L);
        response.setPage(2);
        response.setPageSize(20);
        response.setTotalPages(5);
        response.setHasNext(true);
        response.setHasPrevious(true);
        
        assertEquals(testData, response.getData());
        assertEquals(100L, response.getTotal());
        assertEquals(2, response.getPage());
        assertEquals(20, response.getPageSize());
        assertEquals(5, response.getTotalPages());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }
}
