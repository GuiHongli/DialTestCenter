/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow;

import java.util.zip.CRC32;

/**
 * 入站文件接收状态
 *
 * @author g00940940
 * @since 2025-11-14
 */
public class InboundFileState {
    private String sessionId;
    private int expectedSize;
    private int receivedSize;
    private String expectedCrc;
    private CRC32 crc32 = new CRC32();
    private String tempFilePath;
    private String error;
    private Object businessContext;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getExpectedSize() {
        return expectedSize;
    }

    public void setExpectedSize(int expectedSize) {
        this.expectedSize = expectedSize;
    }

    public int getReceivedSize() {
        return receivedSize;
    }

    public void setReceivedSize(int receivedSize) {
        this.receivedSize = receivedSize;
    }

    public String getExpectedCrc() {
        return expectedCrc;
    }

    public void setExpectedCrc(String expectedCrc) {
        this.expectedCrc = expectedCrc;
    }

    public String getTempFilePath() {
        return tempFilePath;
    }

    public void setTempFilePath(String tempFilePath) {
        this.tempFilePath = tempFilePath;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getBusinessContext() {
        return businessContext;
    }

    public void setBusinessContext(Object businessContext) {
        this.businessContext = businessContext;
    }

    public void addReceivedSize(int size) {
        this.receivedSize += size;
    }

    public void updateCrc(byte[] data) {
        crc32.update(data);
    }

    public boolean verifyCrc() {
        if (expectedCrc == null || expectedCrc.isEmpty()) {
            return true;
        }
        String actualCrc = Long.toHexString(crc32.getValue());
        return actualCrc.equalsIgnoreCase(expectedCrc);
    }

    public boolean isComplete() {
        return receivedSize >= expectedSize;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}

