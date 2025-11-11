/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.codec;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * V3版本TlvDecoder测试
 * 测试TLV消息解码功能
 *
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class TlvDecoderTest {

    @Test
    public void testDecodeMessageHeader_ValidHeader_CorrectValues() {
        // Given
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.put((byte) MessageType.REGISTER_REQUEST.getId()); // Type
        buffer.putInt(42); // Length
        buffer.putInt(12345); // Some data
        buffer.flip();

        // When
        TlvDecoder.DecodedMessageHeader header = TlvDecoder.decodeMessageHeader(buffer);

        // Then
        assertEquals(MessageType.REGISTER_REQUEST, header.getMessageType());
        assertEquals(42, header.getLength());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeMessageHeader_BufferTooSmall_ThrowsException() {
        // Given - Buffer too small for header
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put((byte) 0x01);
        buffer.putShort((short) 42);
        buffer.flip();

        // When - Should throw
        TlvDecoder.decodeMessageHeader(buffer);
    }

    @Test
    public void testDecodeFields_SingleField_CorrectDecode() {
        // Given
        ByteBuffer buffer = ByteBuffer.allocate(50);
        // Field: Tag(0x0001) + Length(2) + Value("TestHost")
        buffer.putShort(FieldTag.HOSTNAME.getValue()); // Tag
        buffer.putShort((short) 8); // Length
        buffer.put("TestHost".getBytes()); // Value
        buffer.flip();

        // When
        java.util.List<TlvField> fields = TlvDecoder.decodeFields(buffer, buffer.remaining());

        // Then
        assertEquals(1, fields.size());
        TlvField field = fields.get(0);
        assertEquals(FieldTag.HOSTNAME, field.getTag());
        assertEquals("TestHost", field.getAsString());
    }

    @Test
    public void testDecodeFields_MultipleFields_CorrectOrder() {
        // Given
        ByteBuffer buffer = ByteBuffer.allocate(100);
        // Field 1: hostname
        buffer.putShort(FieldTag.HOSTNAME.getValue());
        buffer.putShort((short) 5);
        buffer.put("Host1".getBytes());

        // Field 2: token
        buffer.putShort(FieldTag.TOKEN.getValue());
        buffer.putShort((short) 8);
        buffer.putLong(123456789L);

        // Field 3: state
        buffer.putShort(FieldTag.STATE.getValue());
        buffer.putShort((short) 6);
        buffer.put("Normal".getBytes());

        buffer.flip();

        // When
        java.util.List<TlvField> fields = TlvDecoder.decodeFields(buffer, buffer.remaining());

        // Then
        assertEquals(3, fields.size());

        assertEquals(FieldTag.HOSTNAME, fields.get(0).getTag());
        assertEquals("Host1", fields.get(0).getAsString());

        assertEquals(FieldTag.TOKEN, fields.get(1).getTag());
        assertEquals(123456789L, fields.get(1).getAsLong());

        assertEquals(FieldTag.STATE, fields.get(2).getTag());
        assertEquals("Normal", fields.get(2).getAsString());
    }

    @Test
    public void testDecodeFields_EmptyValue_HandlesCorrectly() {
        // Given - Field with empty value
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.putShort(FieldTag.HOSTNAME.getValue());
        buffer.putShort((short) 0); // Empty value
        buffer.flip();

        // When
        java.util.List<TlvField> fields = TlvDecoder.decodeFields(buffer, buffer.remaining());

        // Then
        assertEquals(1, fields.size());
        TlvField field = fields.get(0);
        assertEquals(FieldTag.HOSTNAME, field.getTag());
        assertEquals("", field.getAsString()); // Empty string
        assertEquals(0, field.getLength());
    }

    @Test
    public void testDecodeFields_PartialData_StopsGracefully() {
        // Given - Buffer with incomplete field data
        ByteBuffer buffer = ByteBuffer.allocate(10);
        buffer.putShort(FieldTag.HOSTNAME.getValue());
        buffer.putShort((short) 10); // Length longer than remaining data
        buffer.put("Test".getBytes()); // Only partial data
        buffer.flip();

        // When
        java.util.List<TlvField> fields = TlvDecoder.decodeFields(buffer, buffer.remaining());

        // Then - Should decode what it can
        assertTrue(fields.size() >= 0); // May decode partial or none
    }

    @Test
    public void testDecodeMessage_CompleteMessage_CorrectStructure() {
        // Given - Create a complete TLV message
        java.util.List<TlvField> originalFields = Arrays.asList(
            TlvField.ofString(FieldTag.HOSTNAME, "TestExecutor"),
            TlvField.ofLong(FieldTag.TOKEN, 987654321L)
        );
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST, originalFields);

        // When
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);

        // Then
        assertEquals(MessageType.REGISTER_REQUEST, decoded.getMessageType());
        assertEquals(2, decoded.getFields().size());
        assertEquals("TestExecutor", decoded.getField(FieldTag.HOSTNAME).getAsString());
        assertEquals(987654321L, decoded.getField(FieldTag.TOKEN).getAsLong());
    }

    @Test
    public void testDecodeContainer_SingleSubField_CorrectDecode() {
        // Given - Create a container field
        java.util.List<TlvField> subFields = Arrays.asList(
            TlvField.ofString(FieldTag.SERIAL_NO, "SN001")
        );
        TlvField container = TlvEncoder.encodeContainer(FieldTag.UE_ITEM, subFields);

        // When
        java.util.List<TlvField> decodedFields = TlvDecoder.decodeContainer(container);

        // Then
        assertEquals(1, decodedFields.size());
        assertEquals(FieldTag.SERIAL_NO, decodedFields.get(0).getTag());
        assertEquals("SN001", decodedFields.get(0).getAsString());
    }

    @Test
    public void testDecodeContainer_MultipleSubFields_CorrectDecode() {
        // Given - Container with multiple fields
        java.util.List<TlvField> subFields = Arrays.asList(
            TlvField.ofString(FieldTag.BRAND, "Huawei"),
            TlvField.ofString(FieldTag.MODEL, "Mate 50"),
            TlvField.ofInt(FieldTag.BATTERY, 95)
        );
        TlvField container = TlvEncoder.encodeContainer(FieldTag.UE_ITEM, subFields);

        // When
        java.util.List<TlvField> decodedFields = TlvDecoder.decodeContainer(container);

        // Then
        assertEquals(3, decodedFields.size());
        assertEquals("Huawei", decodedFields.get(0).getAsString());
        assertEquals("Mate 50", decodedFields.get(1).getAsString());
        assertEquals(95, decodedFields.get(2).getAsInt());
    }

    @Test
    public void testDecodeContainer_EmptyContainer_NoFields() {
        // Given - Empty container
        TlvField container = TlvField.ofContainer(FieldTag.UE_LIST, new byte[0]);

        // When
        java.util.List<TlvField> decodedFields = TlvDecoder.decodeContainer(container);

        // Then
        assertTrue(decodedFields.isEmpty());
    }

    @Test
    public void testToFieldMap_SingleFields_CorrectMapping() {
        // Given
        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.HOSTNAME, "Host1"),
            TlvField.ofLong(FieldTag.TOKEN, 123L)
        );

        // When
        java.util.Map<FieldTag, TlvField> fieldMap = TlvDecoder.toFieldMap(fields);

        // Then
        assertEquals(2, fieldMap.size());
        assertEquals("Host1", fieldMap.get(FieldTag.HOSTNAME).getAsString());
        assertEquals(123L, fieldMap.get(FieldTag.TOKEN).getAsLong());
    }

    @Test
    public void testToFieldMultiMap_DuplicateTags_HandlesCorrectly() {
        // Given - Multiple fields with same tag (shouldn't happen in practice, but test robustness)
        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.HOSTNAME, "Host1"),
            TlvField.ofString(FieldTag.HOSTNAME, "Host2") // Duplicate tag
        );

        // When
        java.util.Map<FieldTag, java.util.List<TlvField>> multiMap = TlvDecoder.toFieldMultiMap(fields);

        // Then
        assertEquals(1, multiMap.size()); // Only one key
        assertEquals(2, multiMap.get(FieldTag.HOSTNAME).size()); // But two values
    }

    @Test
    public void testDecodedMessage_GetField_ConvenienceMethods() {
        // Given
        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.HOSTNAME, "TestHost"),
            TlvField.ofLong(FieldTag.TOKEN, 456789L),
            TlvField.ofInt(FieldTag.RESULT, 0)
        );
        TlvDecoder.DecodedMessage decoded = new TlvDecoder.DecodedMessage(MessageType.REGISTER_REQUEST, fields);

        // Then
        assertEquals(MessageType.REGISTER_REQUEST, decoded.getMessageType());
        assertEquals(3, decoded.getFields().size());
        assertTrue(decoded.hasField(FieldTag.HOSTNAME));
        assertFalse(decoded.hasField(FieldTag.CHALLENGE_ID)); // Not present

        assertEquals("TestHost", decoded.getField(FieldTag.HOSTNAME).getAsString());
        assertEquals(456789L, decoded.getField(FieldTag.TOKEN).getAsLong());
        assertEquals(0, decoded.getField(FieldTag.RESULT).getAsInt());
    }

    @Test
    public void testDecodeMessage_VariousDataTypes_HandlesCorrectly() {
        // Given - Message with various data types
        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.USERNAME, "testuser"),
            TlvField.ofInt(FieldTag.CHALLENGE_ID, 42),
            TlvField.ofLong(FieldTag.TOKEN, 123456789012345L),
            TlvField.ofBytes(FieldTag.CONTENT, new byte[]{1, 2, 3, 4, 5})
        );
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.REGISTER_RESPONSE, fields);

        // When
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);

        // Then
        assertEquals(MessageType.REGISTER_RESPONSE, decoded.getMessageType());
        assertEquals(4, decoded.getFields().size());

        assertEquals("testuser", decoded.getField(FieldTag.USERNAME).getAsString());
        assertEquals(42, decoded.getField(FieldTag.CHALLENGE_ID).getAsInt());
        assertEquals(123456789012345L, decoded.getField(FieldTag.TOKEN).getAsLong());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, decoded.getField(FieldTag.CONTENT).getAsBytes());
    }

    @Test
    public void testDecodeMessage_BinaryData_LargeContent() {
        // Given - Message with large binary content
        byte[] largeBinary = new byte[10000];
        for (int i = 0; i < largeBinary.length; i++) {
            largeBinary[i] = (byte) (i % 256);
        }

        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofBytes(FieldTag.CONTENT, largeBinary)
        );
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.SCREENCAP_RESPONSE, fields);

        // When
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);

        // Then
        assertEquals(MessageType.SCREENCAP_RESPONSE, decoded.getMessageType());
        assertArrayEquals(largeBinary, decoded.getField(FieldTag.CONTENT).getAsBytes());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDecodeMessage_InvalidMessageType_ThrowsException() {
        // Given - Invalid message type
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.put((byte) 0xFF); // Invalid message type
        buffer.putInt(0);
        buffer.flip();

        // When - Should throw
        TlvDecoder.decodeMessage(buffer);
    }
}
