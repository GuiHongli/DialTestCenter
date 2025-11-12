/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

import com.huawei.cloududn.dialingtest.controller.executormanagement.websocket.dto.*;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.RegisterRequestDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.ReportMsgDto;
import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.UeItemDto;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * V3版本DtoTlvConverter测试
 * 测试DTO与TLV消息之间的转换功能
 *
 * @author DialTestCenter
 * @since 2025-11-11
 */
public class DtoTlvConverterTest {

    @Test
    public void testRegisterRequest_EncodeDecode_RoundTrip() {
        // Given
        RegisterRequestDto originalDto = new RegisterRequestDto("Executor_PC_001");

        // When - Encode
        ByteBuffer buffer = DtoTlvConverter.encodeRegisterRequest(originalDto);

        // Decode back
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        RegisterRequestDto decodedDto = DtoTlvConverter.decodeRegisterRequest(decoded);

        // Then
        assertEquals(MessageType.REGISTER_REQUEST, decoded.getMessageType());
        assertEquals(originalDto.getHostname(), decodedDto.getHostname());
    }

    @Test
    public void testReportMsg_EncodeDecode_RoundTrip() {
        // Given
        ReportMsgDto originalDto = new ReportMsgDto();
        originalDto.setToken(123456789L);
        originalDto.setState("Normal");

        UeItemDto ueItem = new UeItemDto();
        ueItem.setSerialNo("SN001");
        ueItem.setBrand("Huawei");
        ueItem.setModel("NOH-AN01");
        ueItem.setOs("Android");
        ueItem.setVersion("12.0");
        ueItem.setWmsize("2772x1344");
        ueItem.setIpv4("192.168.1.100");
        ueItem.setIpv6("2001:db8::1");
        ueItem.setBattery(85);
        originalDto.setUeList(Arrays.asList(ueItem));

        // When - Encode
        ByteBuffer buffer = DtoTlvConverter.encodeReportMsg(originalDto);

        // Decode back
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        ReportMsgDto decodedDto = DtoTlvConverter.decodeReportMsg(decoded);

        // Then
        assertEquals(MessageType.REPORT_MSG, decoded.getMessageType());
        assertEquals(originalDto.getToken(), decodedDto.getToken());
        assertEquals(originalDto.getState(), decodedDto.getState());
        assertNotNull(decodedDto.getUeList());
        assertEquals(1, decodedDto.getUeList().size());

        UeItemDto decodedUe = decodedDto.getUeList().get(0);
        assertEquals(ueItem.getSerialNo(), decodedUe.getSerialNo());
        assertEquals(ueItem.getBrand(), decodedUe.getBrand());
        assertEquals(ueItem.getModel(), decodedUe.getModel());
        assertEquals(ueItem.getOs(), decodedUe.getOs());
        assertEquals(ueItem.getVersion(), decodedUe.getVersion());
        assertEquals(ueItem.getBattery(), decodedUe.getBattery());
    }

    @Test
    public void testReportMsg_EmptyUeList_EncodeDecode() {
        // Given
        ReportMsgDto originalDto = new ReportMsgDto();
        originalDto.setToken(987654321L);
        originalDto.setState("Offline");
        originalDto.setUeList(Arrays.asList()); // Empty list

        // When - Encode
        ByteBuffer buffer = DtoTlvConverter.encodeReportMsg(originalDto);

        // Decode back
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        ReportMsgDto decodedDto = DtoTlvConverter.decodeReportMsg(decoded);

        // Then
        assertEquals(MessageType.REPORT_MSG, decoded.getMessageType());
        assertEquals(originalDto.getToken(), decodedDto.getToken());
        assertEquals(originalDto.getState(), decodedDto.getState());
        assertNotNull(decodedDto.getUeList());
        assertTrue(decodedDto.getUeList().isEmpty());
    }

    @Test
    public void testUeItem_FullData_EncodeDecode() {
        // Given
        UeItemDto ueItem = new UeItemDto();
        ueItem.setSerialNo("SN123456789");
        ueItem.setBrand("Xiaomi");
        ueItem.setModel("Mi 12 Pro");
        ueItem.setOs("Android");
        ueItem.setVersion("13.0.1");
        ueItem.setWmsize("3200x1440");
        ueItem.setIpv4("10.0.0.1");
        ueItem.setIpv6("fe80::1");
        ueItem.setBattery(92);

        // When - Create UE container
        TlvField ueField = createUeItemField(ueItem);
        java.util.List<TlvField> ueFields = TlvDecoder.decodeContainer(ueField);

        // Decode back
        UeItemDto decodedUe = decodeUeItem(ueFields);

        // Then
        assertEquals(ueItem.getSerialNo(), decodedUe.getSerialNo());
        assertEquals(ueItem.getBrand(), decodedUe.getBrand());
        assertEquals(ueItem.getModel(), decodedUe.getModel());
        assertEquals(ueItem.getOs(), decodedUe.getOs());
        assertEquals(ueItem.getVersion(), decodedUe.getVersion());
        assertEquals(ueItem.getWmsize(), decodedUe.getWmsize());
        assertEquals(ueItem.getIpv4(), decodedUe.getIpv4());
        assertEquals(ueItem.getIpv6(), decodedUe.getIpv6());
        assertEquals(ueItem.getBattery(), decodedUe.getBattery());
    }

    @Test
    public void testUeItem_PartialData_EncodeDecode() {
        // Given - UE with minimal data
        UeItemDto ueItem = new UeItemDto();
        ueItem.setSerialNo("SN999");
        ueItem.setBrand("Apple");
        ueItem.setModel("iPhone 14");
        // Other fields null/empty

        // When - Create UE container
        TlvField ueField = createUeItemField(ueItem);
        java.util.List<TlvField> ueFields = TlvDecoder.decodeContainer(ueField);

        // Decode back
        UeItemDto decodedUe = decodeUeItem(ueFields);

        // Then
        assertEquals(ueItem.getSerialNo(), decodedUe.getSerialNo());
        assertEquals(ueItem.getBrand(), decodedUe.getBrand());
        assertEquals(ueItem.getModel(), decodedUe.getModel());
        assertNull(decodedUe.getOs()); // Should be null for missing fields
        assertNull(decodedUe.getVersion());
        assertNull(decodedUe.getBattery());
    }

    @Test
    public void testEncodedMessage_Structure_ValidTlvFormat() {
        // Given
        RegisterRequestDto dto = new RegisterRequestDto("TestExecutor");

        // When
        ByteBuffer buffer = DtoTlvConverter.encodeRegisterRequest(dto);

        // Then - Verify TLV structure
        assertTrue(buffer.remaining() >= 5); // At least Type(1) + Length(4) + data

        // Decode and verify
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        assertEquals(MessageType.REGISTER_REQUEST, decoded.getMessageType());
        assertTrue(decoded.getFields().size() >= 1); // Should have at least hostname field
    }

    @Test
    public void testEncodedMessage_FieldValues_CorrectEncoding() {
        // Given
        ReportMsgDto dto = new ReportMsgDto();
        dto.setToken(0x123456789ABCDEF0L);
        dto.setState("Testing");

        // When
        ByteBuffer buffer = DtoTlvConverter.encodeReportMsg(dto);
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);

        // Then
        assertEquals(MessageType.REPORT_MSG, decoded.getMessageType());

        // Check token field
        TlvField tokenField = decoded.getField(FieldTag.TOKEN);
        assertNotNull(tokenField);
        assertEquals(0x123456789ABCDEF0L, tokenField.getAsLong());

        // Check state field
        TlvField stateField = decoded.getField(FieldTag.STATE);
        assertNotNull(stateField);
        assertEquals("Testing", stateField.getAsString());
    }

    @Test
    public void testContainerField_Encoding_CorrectStructure() {
        // Given
        UeItemDto ueItem = new UeItemDto();
        ueItem.setSerialNo("SN001");
        ueItem.setBrand("TestBrand");

        // When - Create container field
        TlvField containerField = createUeItemField(ueItem);

        // Then
        assertEquals(FieldTag.UE_ITEM, containerField.getTag());
        assertTrue(containerField.getLength() > 0);

        // Decode container and verify
        java.util.List<TlvField> subFields = TlvDecoder.decodeContainer(containerField);
        assertTrue(subFields.size() >= 2); // At least serialNo and brand

        // Check for expected fields
        boolean hasSerialNo = false;
        boolean hasBrand = false;
        for (TlvField field : subFields) {
            if (field.getTag() == FieldTag.SERIAL_NO) {
                assertEquals("SN001", field.getAsString());
                hasSerialNo = true;
            } else if (field.getTag() == FieldTag.BRAND) {
                assertEquals("TestBrand", field.getAsString());
                hasBrand = true;
            }
        }
        assertTrue("Should contain serialNo field", hasSerialNo);
        assertTrue("Should contain brand field", hasBrand);
    }

    // Helper methods
    private TlvField createUeItemField(UeItemDto ueItem) {
        java.util.List<TlvField> fields = new java.util.ArrayList<>();

        if (ueItem.getSerialNo() != null) {
            fields.add(TlvField.ofString(FieldTag.SERIAL_NO, ueItem.getSerialNo()));
        }
        if (ueItem.getBrand() != null) {
            fields.add(TlvField.ofString(FieldTag.BRAND, ueItem.getBrand()));
        }
        if (ueItem.getModel() != null) {
            fields.add(TlvField.ofString(FieldTag.MODEL, ueItem.getModel()));
        }
        if (ueItem.getOs() != null) {
            fields.add(TlvField.ofString(FieldTag.OS, ueItem.getOs()));
        }
        if (ueItem.getVersion() != null) {
            fields.add(TlvField.ofString(FieldTag.VERSION, ueItem.getVersion()));
        }
        if (ueItem.getWmsize() != null) {
            fields.add(TlvField.ofString(FieldTag.WMSIZE, ueItem.getWmsize()));
        }
        if (ueItem.getIpv4() != null) {
            fields.add(TlvField.ofString(FieldTag.IPV4, ueItem.getIpv4()));
        }
        if (ueItem.getIpv6() != null) {
            fields.add(TlvField.ofString(FieldTag.IPV6, ueItem.getIpv6()));
        }
        if (ueItem.getBattery() != null) {
            fields.add(TlvField.ofInt(FieldTag.BATTERY, ueItem.getBattery()));
        }

        return TlvEncoder.encodeContainer(FieldTag.UE_ITEM, fields);
    }

    private UeItemDto decodeUeItem(java.util.List<TlvField> fields) {
        UeItemDto ueItem = new UeItemDto();

        for (TlvField field : fields) {
            switch (field.getTag()) {
                case SERIAL_NO:
                    ueItem.setSerialNo(field.getAsString());
                    break;
                case BRAND:
                    ueItem.setBrand(field.getAsString());
                    break;
                case MODEL:
                    ueItem.setModel(field.getAsString());
                    break;
                case OS:
                    ueItem.setOs(field.getAsString());
                    break;
                case VERSION:
                    ueItem.setVersion(field.getAsString());
                    break;
                case WMSIZE:
                    ueItem.setWmsize(field.getAsString());
                    break;
                case IPV4:
                    ueItem.setIpv4(field.getAsString());
                    break;
                case IPV6:
                    ueItem.setIpv6(field.getAsString());
                    break;
                case BATTERY:
                    ueItem.setBattery(field.getAsInt());
                    break;
            }
        }

        return ueItem;
    }
}
