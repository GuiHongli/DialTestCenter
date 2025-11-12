package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * TLV消息编码器
 * 将Java对象编码为TLV二进制格式
 * 
 * 消息结构:
 * +--------+----------------+------------------+
 * | Type   | Length         | Value            |
 * | 1 byte | 4 bytes (BE)   | N bytes          |
 * +--------+----------------+------------------+
 * 
 * @author DialTestCenter
 * @version V3
 */
public class TlvEncoder {
    
    /**
     * 编码完整消息
     * @param messageType 消息类型
     * @param fields 消息体字段列表
     * @return TLV二进制数据
     */
    public static ByteBuffer encodeMessage(MessageType messageType, List<TlvField> fields) {
        try {
            // 先计算消息体长度
            ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
            for (TlvField field : fields) {
                bodyStream.write(field.toBytes());
            }
            byte[] body = bodyStream.toByteArray();
            
            // 构造完整消息: Type(1) + Length(4) + Body
            ByteBuffer buffer = ByteBuffer.allocate(5 + body.length);
            
            // Type (1字节)
            buffer.put((byte) messageType.getId());
            
            // Length (4字节大端序)
            buffer.putInt(body.length);
            
            // Value (消息体)
            buffer.put(body);
            
            buffer.flip();
            return buffer;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode TLV message", e);
        }
    }
    
    /**
     * 编码容器字段（包含多个子字段）
     * @param containerTag 容器Tag
     * @param fields 子字段列表
     * @return 容器TLV字段
     */
    public static TlvField encodeContainer(FieldTag containerTag, List<TlvField> fields) {
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            for (TlvField field : fields) {
                stream.write(field.toBytes());
            }
            return TlvField.ofContainer(containerTag, stream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode container field", e);
        }
    }
    
    /**
     * 将ByteBuffer转为字节数组（用于日志和调试）
     */
    public static byte[] toByteArray(ByteBuffer buffer) {
        buffer.mark();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.reset();
        return bytes;
    }
    
    /**
     * 格式化字节数组为十六进制字符串（用于日志）
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(bytes.length * 3);
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02X", bytes[i] & 0xFF));
        }
        return sb.toString();
    }
}

