/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2025. All rights reserved.
 */

package com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.codec;

import com.huawei.cloududn.dialingtestapp.controller.executormanagement.websocket.dto.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DTO与TLV之间的转换器
 * V3版本核心组件：负责将业务DTO对象转换为TLV消息，以及将TLV消息转换为DTO对象
 *
 * @author DialTestCenter
 * @version V3
 * @since 2025-11-11
 */
public class DtoTlvConverter {
    
    /**
     * 将RegisterRequest DTO编码为TLV消息
     *
     * @param dto RegisterRequest DTO
     * @return TLV二进制缓冲区
     */
    public static ByteBuffer encodeRegisterRequest(RegisterRequestDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofString(FieldTag.HOSTNAME, dto.getHostname()));
        return TlvEncoder.encodeMessage(MessageType.REGISTER_REQUEST, fields);
    }
    
    /**
     * 从TLV消息解码为RegisterRequest DTO
     *
     * @param decoded 解码后的消息
     * @return RegisterRequest DTO
     */
    public static RegisterRequestDto decodeRegisterRequest(TlvDecoder.DecodedMessage decoded) {
        RegisterRequestDto dto = new RegisterRequestDto();
        dto.setHostname(decoded.getField(FieldTag.HOSTNAME).getAsString());
        return dto;
    }
    
    /**
     * 编码ReportMsg DTO为TLV消息
     *
     * @param dto ReportMsg DTO
     * @return TLV二进制缓冲区
     */
    public static ByteBuffer encodeReportMsg(ReportMsgDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofString(FieldTag.STATE, dto.getState()));
        
        // Encode UE list
        if (dto.getUeList() != null && !dto.getUeList().isEmpty()) {
            List<TlvField> ueFields = new ArrayList<>();
            for (UeItemDto ueItem : dto.getUeList()) {
                ueFields.add(encodeUeItem(ueItem));
            }
            fields.add(TlvEncoder.encodeContainer(FieldTag.UE_LIST, ueFields));
        } else {
            // Empty UE list container
            fields.add(TlvField.ofContainer(FieldTag.UE_LIST, new byte[0]));
        }
        
        return TlvEncoder.encodeMessage(MessageType.REPORT_MSG, fields);
    }
    
    /**
     * 解码ReportMsg TLV消息为DTO
     *
     * @param decoded 解码后的消息
     * @return ReportMsg DTO
     */
    public static ReportMsgDto decodeReportMsg(TlvDecoder.DecodedMessage decoded) {
        ReportMsgDto dto = new ReportMsgDto();
        dto.setToken(decoded.getField(FieldTag.TOKEN).getAsLong());
        dto.setState(decoded.getField(FieldTag.STATE).getAsString());
        
        // Decode UE list
        TlvField ueListField = decoded.getField(FieldTag.UE_LIST);
        if (ueListField != null) {
            List<TlvField> ueFields = TlvDecoder.decodeContainer(ueListField);
            List<UeItemDto> ueList = new ArrayList<>();
            for (TlvField ueField : ueFields) {
                if (ueField.getTag() == FieldTag.UE_ITEM) {
                    ueList.add(decodeUeItem(ueField));
                }
            }
            dto.setUeList(ueList);
        }
        
        return dto;
    }
    
    /**
     * 编码单个UE项为TLV字段
     *
     * @param ueItem UE项DTO
     * @return UE项TLV字段
     */
    private static TlvField encodeUeItem(UeItemDto ueItem) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofString(FieldTag.SERIAL_NO, ueItem.getSerialNo()));
        fields.add(TlvField.ofString(FieldTag.BRAND, ueItem.getBrand()));
        fields.add(TlvField.ofString(FieldTag.MODEL, ueItem.getModel()));
        fields.add(TlvField.ofString(FieldTag.OS, ueItem.getOs()));
        fields.add(TlvField.ofString(FieldTag.VERSION, ueItem.getVersion()));
        fields.add(TlvField.ofString(FieldTag.WMSIZE, ueItem.getWmsize()));
        
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
    
    /**
     * 解码UE项TLV字段为DTO
     *
     * @param ueField UE项TLV字段
     * @return UE项DTO
     */
    private static UeItemDto decodeUeItem(TlvField ueField) {
        List<TlvField> fields = TlvDecoder.decodeContainer(ueField);
        Map<FieldTag, TlvField> fieldMap = TlvDecoder.toFieldMap(fields);
        
        UeItemDto dto = new UeItemDto();
        
        // 必需字段 - 如果不存在则使用默认值
        if (fieldMap.containsKey(FieldTag.SERIAL_NO)) {
            dto.setSerialNo(fieldMap.get(FieldTag.SERIAL_NO).getAsString());
        } else {
            dto.setSerialNo("");
        }
        
        if (fieldMap.containsKey(FieldTag.BRAND)) {
            dto.setBrand(fieldMap.get(FieldTag.BRAND).getAsString());
        } else {
            dto.setBrand("");
        }
        
        if (fieldMap.containsKey(FieldTag.MODEL)) {
            dto.setModel(fieldMap.get(FieldTag.MODEL).getAsString());
        } else {
            dto.setModel("");
        }
        
        if (fieldMap.containsKey(FieldTag.OS)) {
            dto.setOs(fieldMap.get(FieldTag.OS).getAsString());
        } else {
            dto.setOs("");
        }
        
        if (fieldMap.containsKey(FieldTag.VERSION)) {
            dto.setVersion(fieldMap.get(FieldTag.VERSION).getAsString());
        } else {
            dto.setVersion("");
        }
        
        if (fieldMap.containsKey(FieldTag.WMSIZE)) {
            dto.setWmsize(fieldMap.get(FieldTag.WMSIZE).getAsString());
        } else {
            dto.setWmsize("");
        }
        
        // 可选字段
        if (fieldMap.containsKey(FieldTag.IPV4)) {
            dto.setIpv4(fieldMap.get(FieldTag.IPV4).getAsString());
        }
        if (fieldMap.containsKey(FieldTag.IPV6)) {
            dto.setIpv6(fieldMap.get(FieldTag.IPV6).getAsString());
        }
        if (fieldMap.containsKey(FieldTag.BATTERY)) {
            dto.setBattery(fieldMap.get(FieldTag.BATTERY).getAsInt());
        }
        
        return dto;
    }
    
    /**
     * 编码RegisterChallenge DTO为TLV消息
     *
     * @param dto RegisterChallenge DTO
     * @return TLV二进制缓冲区
     */
    public static ByteBuffer encodeRegisterChallenge(RegisterChallengeDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofInt(FieldTag.CHALLENGE_ID, dto.getChallengeId()));
        fields.add(TlvField.ofBytes(FieldTag.CHALLENGE, dto.getChallenge()));
        return TlvEncoder.encodeMessage(MessageType.REGISTER_CHALLENGE, fields);
    }
    
    /**
     * 编码RegisterResult DTO为TLV消息
     *
     * @param dto RegisterResult DTO
     * @return TLV二进制缓冲区
     */
    public static ByteBuffer encodeRegisterResult(RegisterResultDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofInt(FieldTag.RESULT, dto.getResult()));
        fields.add(TlvField.ofString(FieldTag.DESCRIPTION, dto.getDescription()));
        if (dto.getToken() != null) {
            fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        }
        return TlvEncoder.encodeMessage(MessageType.REGISTER_RESULT, fields);
    }
    
    /**
     * 编码ReportAck DTO为TLV消息
     *
     * @param dto ReportAck DTO
     * @return TLV二进制缓冲区
     */
    public static ByteBuffer encodeReportAck(ReportAckDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofInt(FieldTag.STATE, dto.getState()));
        return TlvEncoder.encodeMessage(MessageType.REPORT_ACK, fields);
    }
    
    /**
     * 编码DeRegisterAck DTO为TLV消息
     * V3新增：注销应答(0x06)
     *
     * @param dto DeRegisterAck DTO
     * @return TLV二进制缓冲区
     */
    public static ByteBuffer encodeDeRegisterAck(DeRegisterAckDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofInt(FieldTag.RESULT, dto.getResultCode()));
        fields.add(TlvField.ofString(FieldTag.DESCRIPTION, dto.getDescription()));
        return TlvEncoder.encodeMessage(MessageType.DEREGISTER_ACK, fields);
    }
    
    /**
     * 解码DeRegisterRequest TLV消息为DTO
     * V3新增：注销请求(0x05)
     *
     * @param decoded 解码后的消息
     * @return DeRegisterRequest DTO
     */
    public static DeRegisterRequestDto decodeDeRegisterRequest(TlvDecoder.DecodedMessage decoded) {
        DeRegisterRequestDto dto = new DeRegisterRequestDto();
        dto.setToken(decoded.getField(FieldTag.TOKEN).getAsLong());
        dto.setHostname(decoded.getField(FieldTag.HOSTNAME).getAsString());
        return dto;
    }
    
    /**
     * 编码TaskStartRequest TLV消息
     * V3版本：任务启动请求(0x33)
     *
     * @param dto TaskStartRequestDto
     * @return 编码后的ByteBuffer
     */
    public static ByteBuffer encodeTaskStart(TaskStartRequestDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofInt(FieldTag.TASKID, dto.getTaskId()));
        fields.add(TlvField.ofString(FieldTag.SCRIPT_NAME, dto.getScriptName()));
        fields.add(TlvField.ofString(FieldTag.VERSION, dto.getVersion()));

        // serial-no-list容器
        if (dto.getSerialNoList() != null && !dto.getSerialNoList().isEmpty()) {
            List<TlvField> serialList = new ArrayList<>();
            for (String serialNo : dto.getSerialNoList()) {
                serialList.add(TlvField.ofString(FieldTag.SERIAL_NO, serialNo));
            }
            fields.add(TlvEncoder.encodeContainer(FieldTag.SERIAL_NO_LIST, serialList));
        }

        fields.add(TlvField.ofInt(FieldTag.PROCTYPE, dto.getProcType()));
        fields.add(TlvField.ofString(FieldTag.PARAMETERS, dto.getParameters()));

        return TlvEncoder.encodeMessage(MessageType.TASK_START_REQUEST, fields);
    }

    /**
     * 编码TaskStopRequest TLV消息
     * V3版本：任务停止请求(0x35)
     *
     * @param dto TaskStopRequestDto
     * @return 编码后的ByteBuffer
     */
    public static ByteBuffer encodeTaskStop(TaskStopRequestDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofInt(FieldTag.TASKID, dto.getTaskId()));
        return TlvEncoder.encodeMessage(MessageType.TASK_STOP_REQUEST, fields);
    }

    /**
     * 编码AppListQuery TLV消息
     * V3版本：App列表查询请求(0x21)
     *
     * @param dto AppListQueryDto
     * @return 编码后的ByteBuffer
     */
    public static ByteBuffer encodeAppListQuery(AppListQueryDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofString(FieldTag.SERIAL_NO, dto.getSerialNo()));
        return TlvEncoder.encodeMessage(MessageType.APP_LIST_QUERY, fields);
    }

    /**
     * 编码AppInstallRequest TLV消息
     * V3版本：App安装请求(0x23)
     *
     * @param dto AppInstallRequestDto
     * @return 编码后的ByteBuffer
     */
    public static ByteBuffer encodeAppInstallRequest(AppInstallRequestDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofString(FieldTag.SERIAL_NO, dto.getSerialNo()));
        fields.add(TlvField.ofInt(FieldTag.TASKID, dto.getTaskId()));
        fields.add(TlvField.ofString(FieldTag.APPNAME, dto.getAppName()));

        // script或package字段（二选一）
        if (dto.getScript() != null && dto.getScript().length > 0) {
            fields.add(TlvField.ofBytes(FieldTag.SCRIPT, dto.getScript()));
        }
        if (dto.getPackageFile() != null && dto.getPackageFile().length > 0) {
            fields.add(TlvField.ofBytes(FieldTag.PACKAGE, dto.getPackageFile()));
        }

        if (dto.getCrc() != null) {
            fields.add(TlvField.ofBytes(FieldTag.CRC, dto.getCrc()));
        }

        return TlvEncoder.encodeMessage(MessageType.APP_INSTALL_REQUEST, fields);
    }

    /**
     * 编码ScriptUpdateNotify TLV消息
     * V3版本：脚本更新通知(0x31)
     *
     * @param dto ScriptUpdateNotifyDto
     * @return 编码后的ByteBuffer
     */
    public static ByteBuffer encodeScriptUpdateNotify(ScriptUpdateNotifyDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofString(FieldTag.SCRIPT_NAME, dto.getScriptName()));
        fields.add(TlvField.ofString(FieldTag.VERSION, dto.getVersion()));
        fields.add(TlvField.ofInt(FieldTag.FILELEN, dto.getFileLen()));
        fields.add(TlvField.ofBytes(FieldTag.SCRIPTFILE, dto.getScriptFile()));
        fields.add(TlvField.ofBytes(FieldTag.CRC, dto.getCrc()));
        return TlvEncoder.encodeMessage(MessageType.SCRIPT_UPDATE_NOTIFY, fields);
    }

    /**
     * 编码ScreencapQuery TLV消息
     * V3版本：截屏查询请求(0x25)
     *
     * @param dto ScreencapQueryDto
     * @return 编码后的ByteBuffer
     */
    public static ByteBuffer encodeScreencapQuery(ScreencapQueryDto dto) {
        List<TlvField> fields = new ArrayList<>();
        fields.add(TlvField.ofLong(FieldTag.TOKEN, dto.getToken()));
        fields.add(TlvField.ofString(FieldTag.SERIAL_NO, dto.getSerialNo()));
        return TlvEncoder.encodeMessage(MessageType.SCREENCAP_QUERY, fields);
    }

    /**
     * 通用解码方法：根据消息类型解码为对应的DTO
     *
     * @param buffer 二进制缓冲区
     * @return 解码后的DTO对象
     * @throws IllegalArgumentException 如果消息类型不支持
     */
    public static Object decode(ByteBuffer buffer) {
        TlvDecoder.DecodedMessage decoded = TlvDecoder.decodeMessage(buffer);
        MessageType messageType = decoded.getMessageType();

        if (messageType == MessageType.REGISTER_REQUEST) {
            return decodeRegisterRequest(decoded);
        } else if (messageType == MessageType.REPORT_MSG) {
            return decodeReportMsg(decoded);
        } else {
            throw new IllegalArgumentException("Unsupported message type: " + messageType);
        }
    }
}

