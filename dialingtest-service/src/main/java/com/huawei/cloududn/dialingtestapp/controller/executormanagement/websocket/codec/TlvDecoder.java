package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TLV消息解码器
 * 将TLV二进制数据解码为Java对象
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
public class TlvDecoder {
    
    /**
     * 解码消息头（Type和Length）
     * @param buffer 二进制缓冲区
     * @return DecodedMessageHeader对象
     */
    public static DecodedMessageHeader decodeMessageHeader(ByteBuffer buffer) {
        if (buffer.remaining() < 5) {
            throw new IllegalArgumentException("Buffer too small for TLV message header");
        }
        
        // Type (1字节)
        int typeId = buffer.get() & 0xFF;
        MessageType messageType = MessageType.fromId(typeId);
        
        // Length (4字节大端序)
        int length = buffer.getInt();
        
        return new DecodedMessageHeader(messageType, length);
    }
    
    /**
     * 解码消息体字段
     * @param buffer 二进制缓冲区（已读取消息头）
     * @param bodyLength 消息体长度
     * @return 字段列表
     */
    public static List<TlvField> decodeFields(ByteBuffer buffer, int bodyLength) {
        List<TlvField> fields = new ArrayList<>();
        int startPosition = buffer.position();
        int endPosition = startPosition + bodyLength;
        
        while (buffer.position() < endPosition) {
            if (buffer.remaining() < 4) {
                break; // 不足以读取Tag+Length
            }
            
            // Tag (2字节大端序)
            int tagValue = ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
            FieldTag tag = FieldTag.fromValue(tagValue);
            
            // Length (2字节大端序)
            int length = ((buffer.get() & 0xFF) << 8) | (buffer.get() & 0xFF);
            
            // Value
            byte[] value = new byte[length];
            if (length > 0) {
                if (buffer.remaining() < length) {
                    break; // 数据不足，停止解码
                }
                buffer.get(value);
            }
            
            fields.add(new TlvField(tag, value));
        }
        
        return fields;
    }
    
    /**
     * 解码完整消息
     * @param buffer 二进制缓冲区
     * @return DecodedMessage对象
     */
    public static DecodedMessage decodeMessage(ByteBuffer buffer) {
        DecodedMessageHeader header = decodeMessageHeader(buffer);
        List<TlvField> fields = decodeFields(buffer, header.getLength());
        return new DecodedMessage(header.getMessageType(), fields);
    }
    
    /**
     * 解码容器字段
     * @param containerField 容器TLV字段
     * @return 子字段列表
     */
    public static List<TlvField> decodeContainer(TlvField containerField) {
        ByteBuffer buffer = ByteBuffer.wrap(containerField.getValue());
        return decodeFields(buffer, containerField.getLength());
    }
    
    /**
     * 将字段列表转为Map（Tag -> Field）
     */
    public static Map<FieldTag, TlvField> toFieldMap(List<TlvField> fields) {
        Map<FieldTag, TlvField> map = new HashMap<>();
        for (TlvField field : fields) {
            map.put(field.getTag(), field);
        }
        return map;
    }
    
    /**
     * 将字段列表转为MultiMap（Tag -> List<Field>），支持同Tag多值
     */
    public static Map<FieldTag, List<TlvField>> toFieldMultiMap(List<TlvField> fields) {
        Map<FieldTag, List<TlvField>> map = new HashMap<>();
        for (TlvField field : fields) {
            map.computeIfAbsent(field.getTag(), k -> new ArrayList<>()).add(field);
        }
        return map;
    }
    
    /**
     * 解码后的消息头
     */
    public static class DecodedMessageHeader {
        private final MessageType messageType;
        private final int length;
        
        public DecodedMessageHeader(MessageType messageType, int length) {
            this.messageType = messageType;
            this.length = length;
        }
        
        public MessageType getMessageType() {
            return messageType;
        }
        
        public int getLength() {
            return length;
        }
    }
    
    /**
     * 解码后的完整消息
     */
    public static class DecodedMessage {
        private final MessageType messageType;
        private final List<TlvField> fields;
        private final Map<FieldTag, TlvField> fieldMap;
        
        public DecodedMessage(MessageType messageType, List<TlvField> fields) {
            this.messageType = messageType;
            this.fields = fields;
            this.fieldMap = toFieldMap(fields);
        }
        
        public MessageType getMessageType() {
            return messageType;
        }
        
        public List<TlvField> getFields() {
            return fields;
        }
        
        public Map<FieldTag, TlvField> getFieldMap() {
            return fieldMap;
        }
        
        public TlvField getField(FieldTag tag) {
            return fieldMap.get(tag);
        }
        
        public boolean hasField(FieldTag tag) {
            return fieldMap.containsKey(tag);
        }
    }
}

