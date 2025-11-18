/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * UeItemDto UT test
 *
 * @author g00940940
 * @since 2025-11-18
 */
public class UeItemDtoTest {
    @Test
    public void testUeItemDto_AllMethods_Success() {
        UeItemDto dto1 = new UeItemDto();
        assertNotNull(dto1);
        
        dto1.setSerialNo("12345");
        dto1.setBrand("huawei");
        dto1.setModel("NOH-AN01");
        dto1.setOs("HarmonyOS");
        dto1.setVersion("4.0");
        dto1.setWmsize("2772 x 1344");
        dto1.setIpv4("192.168.1.1");
        dto1.setIpv6("fe80::1");
        dto1.setBattery(85);
        
        assertEquals("12345", dto1.getSerialNo());
        assertEquals("huawei", dto1.getBrand());
        assertEquals("NOH-AN01", dto1.getModel());
        assertEquals("HarmonyOS", dto1.getOs());
        assertEquals("4.0", dto1.getVersion());
        assertEquals("2772 x 1344", dto1.getWmsize());
        assertEquals("192.168.1.1", dto1.getIpv4());
        assertEquals("fe80::1", dto1.getIpv6());
        assertEquals(Integer.valueOf(85), dto1.getBattery());
        assertNotNull(dto1.toString());
    }
}

