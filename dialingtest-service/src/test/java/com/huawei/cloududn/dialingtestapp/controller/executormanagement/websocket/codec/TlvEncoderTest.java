/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * V3版本TlvEncoder测试
 * 测试TLV消息编码功能
 *
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class TlvEncoderTest {

    @Test
    public void testEncodeMessage_BasicStructure_CorrectFormat() {
        // Given
        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.HOSTNAME, "TestExecutor")
        );

        // When
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST, fields);

        // Then
        assertTrue(buffer.remaining() >= 5); // Type(1) + Length(4) + body

        // Verify message type
        buffer.mark();
        assertEquals((byte) MessageType.REGISTER_REQUEST.getId(), buffer.get());
        buffer.reset();
    }

    @Test
    public void testEncodeMessage_SingleField_CorrectLength() {
        // Given
        String testValue = "HelloWorld";
        TlvField field = TlvField.ofString(FieldTag.HOSTNAME, testValue);
        java.util.List<TlvField> fields = Arrays.asList(field);

        // When
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST, fields);

        // Then
        // Expected: Type(1) + Length(4) + Tag(2) + FieldLength(2) + Value
        int expectedMinLength = 1 + 4 + 2 + 2 + testValue.getBytes().length;
        assertTrue(buffer.remaining() >= expectedMinLength);
    }

    @Test
    public void testEncodeMessage_MultipleFields_CorrectStructure() {
        // Given
        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.HOSTNAME, "Executor1"),
            TlvField.ofInt(FieldTag.CHALLENGE_ID, 42),
            TlvField.ofLong(FieldTag.TOKEN, 123456789L)
        );

        // When
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST, fields);

        // Then
        assertTrue(buffer.remaining() > 0);

        // Decode and verify
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        assertEquals(MessageType.REGISTER_REQUEST, decoded.getMessageType());
        assertEquals(3, decoded.getFields().size());
    }

    @Test
    public void testEncodeMessage_EmptyFields_ZeroLength() {
        // Given
        java.util.List<TlvField> fields = Arrays.asList();

        // When
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST, fields);

        // Then
        assertEquals(5, buffer.remaining()); // Type(1) + Length(4) + empty body

        // Verify length is 0
        buffer.mark();
        buffer.get(); // Skip type
        assertEquals(0, buffer.getInt()); // Length should be 0
        buffer.reset();
    }

    @Test
    public void testEncodeContainer_SingleField_CorrectStructure() {
        // Given
        java.util.List<TlvField> subFields = Arrays.asList(
            TlvField.ofString(FieldTag.SERIAL_NO, "SN001")
        );

        // When
        TlvField container = TlvEncoder.encodeContainer(FieldTag.UE_ITEM, subFields);

        // Then
        assertEquals(FieldTag.UE_ITEM, container.getTag());
        assertTrue(container.getLength() > 0);

        // Decode container and verify
        java.util.List<TlvField> decodedFields = TlvDecoder.decodeContainer(container);
        assertEquals(1, decodedFields.size());
        assertEquals(FieldTag.SERIAL_NO, decodedFields.get(0).getTag());
        assertEquals("SN001", decodedFields.get(0).getAsString());
    }

    @Test
    public void testEncodeContainer_MultipleFields_CorrectOrder() {
        // Given
        java.util.List<TlvField> subFields = Arrays.asList(
            TlvField.ofString(FieldTag.BRAND, "Huawei"),
            TlvField.ofString(FieldTag.MODEL, "Mate 50"),
            TlvField.ofString(FieldTag.OS, "HarmonyOS")
        );

        // When
        TlvField container = TlvEncoder.encodeContainer(FieldTag.UE_ITEM, subFields);

        // Then
        java.util.List<TlvField> decodedFields = TlvDecoder.decodeContainer(container);
        assertEquals(3, decodedFields.size());

        // Verify fields are in encoding order
        assertEquals(FieldTag.BRAND, decodedFields.get(0).getTag());
        assertEquals("Huawei", decodedFields.get(0).getAsString());
        assertEquals(FieldTag.MODEL, decodedFields.get(1).getTag());
        assertEquals("Mate 50", decodedFields.get(1).getAsString());
        assertEquals(FieldTag.OS, decodedFields.get(2).getTag());
        assertEquals("HarmonyOS", decodedFields.get(2).getAsString());
    }

    @Test
    public void testEncodeContainer_EmptyFields_EmptyContainer() {
        // Given
        java.util.List<TlvField> subFields = Arrays.asList();

        // When
        TlvField container = TlvEncoder.encodeContainer(FieldTag.UE_LIST, subFields);

        // Then
        assertEquals(FieldTag.UE_LIST, container.getTag());
        assertEquals(0, container.getLength());

        // Decode and verify empty
        java.util.List<TlvField> decodedFields = TlvDecoder.decodeContainer(container);
        assertTrue(decodedFields.isEmpty());
    }

    @Test
    public void testToByteArray_BufferConversion_Correct() {
        // Given
        ByteBuffer buffer = ByteBuffer.allocate(13);
        buffer.put((byte) 0x01);
        buffer.putInt(42);
        buffer.putLong(123456789L);
        buffer.flip();

        // When
        byte[] bytes = TlvEncoder.toByteArray(buffer);

        // Then
        assertEquals(13, bytes.length);
        assertEquals((byte) 0x01, bytes[0]);
        assertEquals(42, java.nio.ByteBuffer.wrap(bytes, 1, 4).getInt());
        assertEquals(123456789L, java.nio.ByteBuffer.wrap(bytes, 5, 8).getLong());
    }

    @Test
    public void testToHexString_VariousInputs_CorrectFormat() {
        // Given
        byte[] data = new byte[] { 0x01, 0x02, (byte) 0xFF, 0x10 };

        // When
        String hex = TlvEncoder.toHexString(data);

        // Then
        assertEquals("01 02 FF 10", hex);
    }

    @Test
    public void testToHexString_EmptyArray_EmptyString() {
        // Given
        byte[] data = new byte[0];

        // When
        String hex = TlvEncoder.toHexString(data);

        // Then
        assertEquals("", hex);
    }

    @Test
    public void testToHexString_NullArray_EmptyString() {
        // When
        String hex = TlvEncoder.toHexString(null);

        // Then
        assertEquals("", hex);
    }

    @Test
    public void testEncodeMessage_LargeData_NoErrors() {
        // Given - Large string to test buffer handling
        StringBuilder largeString = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeString.append("TestData");
        }

        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofString(FieldTag.CONTENT, largeString.toString())
        );

        // When
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.SCREENCAP_RESPONSE, fields);

        // Then - Should not throw exception and have reasonable size
        assertTrue(buffer.remaining() > largeString.length());
    }

    @Test
    public void testEncodeMessage_BinaryData_HandlesCorrectly() {
        // Given - Binary data (simulating file content)
        byte[] binaryData = new byte[256];
        for (int i = 0; i < binaryData.length; i++) {
            binaryData[i] = (byte) (i % 256);
        }

        java.util.List<TlvField> fields = Arrays.asList(
            TlvField.ofBytes(FieldTag.CONTENT, binaryData)
        );

        // When
        ByteBuffer buffer = TlvEncoder.encodeMessage(MessageType.SCRIPT_UPDATE_NOTIFY, fields);

        // Then
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        assertEquals(MessageType.SCRIPT_UPDATE_NOTIFY, decoded.getMessageType());

        TlvField contentField = decoded.getField(FieldTag.CONTENT);
        assertNotNull(contentField);
        assertArrayEquals(binaryData, contentField.getAsBytes());
    }

    @Test
    public void testEncodeMessage_DifferentMessageTypes_CorrectTypeEncoding() {
        // Test various message types
        MessageType[] testTypes = {
            MessageType.REGISTER_REQUEST,
            MessageType.REPORT_MSG,
            MessageType.APP_LIST_QUERY,
            MessageType.TASK_START_REQUEST
        };

        for (MessageType type : testTypes) {
            // Given
            java.util.List<TlvField> fields = Arrays.asList(
                TlvField.ofString(FieldTag.HOSTNAME, "Test")
            );

            // When
            ByteBuffer buffer = TlvEncoder.encodeMessage(type, fields);

            // Then
            TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
            assertEquals(type, decoded.getMessageType());
        }
    }
}
