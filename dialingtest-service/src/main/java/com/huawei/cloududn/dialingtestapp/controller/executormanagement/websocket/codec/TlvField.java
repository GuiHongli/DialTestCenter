package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

import java.nio.charset.StandardCharsets;

/**
 * TLV字段基础类
 * 表示单个TLV字段 (Tag-Length-Value)
 * 
 * 字段结构:
 * +--------+----------------+------------------+
 * | Tag    | Length         | Value            |
 * | 2 bytes| 2 bytes (BE)   | N bytes          |
 * +--------+----------------+------------------+
 * 
 * @author DialTestCenter
 * @version V3
 */
public class TlvField {
    private final FieldTag tag;
    private final byte[] value;
    
    public TlvField(FieldTag tag, byte[] value) {
        this.tag = tag;
        this.value = value != null ? value : new byte[0];
    }
    
    public FieldTag getTag() {
        return tag;
    }
    
    public byte[] getValue() {
        return value;
    }
    
    public int getLength() {
        return value.length;
    }
    
    /**
     * 创建字符串类型TLV字段
     */
    public static TlvField ofString(FieldTag tag, String value) {
        if (value == null) {
            return new TlvField(tag, new byte[0]);
        }
        return new TlvField(tag, value.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 创建整型类型TLV字段 (4字节大端序)
     */
    public static TlvField ofInt(FieldTag tag, int value) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (value >>> 24);
        bytes[1] = (byte) (value >>> 16);
        bytes[2] = (byte) (value >>> 8);
        bytes[3] = (byte) value;
        return new TlvField(tag, bytes);
    }
    
    /**
     * 创建长整型类型TLV字段 (8字节大端序)
     */
    public static TlvField ofLong(FieldTag tag, long value) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (value >>> 56);
        bytes[1] = (byte) (value >>> 48);
        bytes[2] = (byte) (value >>> 40);
        bytes[3] = (byte) (value >>> 32);
        bytes[4] = (byte) (value >>> 24);
        bytes[5] = (byte) (value >>> 16);
        bytes[6] = (byte) (value >>> 8);
        bytes[7] = (byte) value;
        return new TlvField(tag, bytes);
    }
    
    /**
     * 创建二进制类型TLV字段
     */
    public static TlvField ofBytes(FieldTag tag, byte[] value) {
        return new TlvField(tag, value);
    }
    
    /**
     * 创建容器类型TLV字段（包含多个子字段）
     */
    public static TlvField ofContainer(FieldTag tag, byte[] containerBytes) {
        return new TlvField(tag, containerBytes);
    }
    
    /**
     * 获取字符串值
     */
    public String getAsString() {
        if (value == null || value.length == 0) {
            return "";
        }
        return new String(value, StandardCharsets.UTF_8);
    }
    
    /**
     * 获取整型值 (4字节大端序)
     */
    public int getAsInt() {
        if (value == null || value.length < 4) {
            throw new IllegalStateException("Value is not a valid int");
        }
        return ((value[0] & 0xFF) << 24) |
               ((value[1] & 0xFF) << 16) |
               ((value[2] & 0xFF) << 8) |
               (value[3] & 0xFF);
    }
    
    /**
     * 获取长整型值 (8字节大端序)
     */
    public long getAsLong() {
        if (value == null || value.length < 8) {
            throw new IllegalStateException("Value is not a valid long");
        }
        return ((long) (value[0] & 0xFF) << 56) |
               ((long) (value[1] & 0xFF) << 48) |
               ((long) (value[2] & 0xFF) << 40) |
               ((long) (value[3] & 0xFF) << 32) |
               ((long) (value[4] & 0xFF) << 24) |
               ((long) (value[5] & 0xFF) << 16) |
               ((long) (value[6] & 0xFF) << 8) |
               (long) (value[7] & 0xFF);
    }
    
    /**
     * 获取二进制值
     */
    public byte[] getAsBytes() {
        return value;
    }
    
    /**
     * 序列化为字节数组
     * 格式: Tag(2字节) + Length(2字节) + Value(N字节)
     */
    public byte[] toBytes() {
        int totalLength = 4 + value.length; // 2字节Tag + 2字节Length + Value
        byte[] result = new byte[totalLength];
        
        // Tag (2字节大端序)
        int tagValue = tag.getValue();
        result[0] = (byte) (tagValue >>> 8);
        result[1] = (byte) tagValue;
        
        // Length (2字节大端序)
        int length = value.length;
        result[2] = (byte) (length >>> 8);
        result[3] = (byte) length;
        
        // Value
        if (value.length > 0) {
            System.arraycopy(value, 0, result, 4, value.length);
        }
        
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("TlvField{tag=%s(0x%04X), length=%d}", 
            tag.getName(), tag.getValue(), value.length);
    }
}

