/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.flow;

import java.nio.ByteBuffer;

/**
 * 队列消息包装类
 * 用于在SessionSendQueue中统一管理Text和Binary消息
 *
 * @author g00940940
 * @since 2025-11-14
 */
public class QueuedMessage {
    private final MessageTypeEnum type;
    private final String textMessage;
    private final ByteBuffer binaryMessage;
    private final long timestamp;

    private QueuedMessage(MessageTypeEnum type, String textMessage, ByteBuffer binaryMessage) {
        this.type = type;
        this.textMessage = textMessage;
        this.binaryMessage = binaryMessage;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 创建Text消息（JSON信令）
     *
     * @param textMessage JSON消息字符串
     * @return QueuedMessage实例
     */
    public static QueuedMessage text(String textMessage) {
        if (textMessage == null || textMessage.isEmpty()) {
            throw new IllegalArgumentException("Text message cannot be null or empty");
        }
        return new QueuedMessage(MessageTypeEnum.TEXT, textMessage, null);
    }

    /**
     * 创建Binary消息（文件分片）
     *
     * @param binaryMessage 二进制数据
     * @return QueuedMessage实例
     */
    public static QueuedMessage binary(ByteBuffer binaryMessage) {
        if (binaryMessage == null || binaryMessage.remaining() == 0) {
            throw new IllegalArgumentException("Binary message cannot be null or empty");
        }
        return new QueuedMessage(MessageTypeEnum.BINARY, null, binaryMessage);
    }

    public MessageTypeEnum getType() {
        return type;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public ByteBuffer getBinaryMessage() {
        return binaryMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isText() {
        return type == MessageTypeEnum.TEXT;
    }

    public boolean isBinary() {
        return type == MessageTypeEnum.BINARY;
    }

    public int getSize() {
        if (isText()) {
            return textMessage != null ? textMessage.length() : 0;
        } else {
            return binaryMessage != null ? binaryMessage.remaining() : 0;
        }
    }

    @Override
    public String toString() {
        return "QueuedMessage{" +
                "type=" + type +
                ", size=" + getSize() +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * 消息类型枚举
     */
    public enum MessageTypeEnum {
        TEXT,
        BINARY
    }
}

