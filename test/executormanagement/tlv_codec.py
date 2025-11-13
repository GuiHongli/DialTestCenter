"""
TLV编解码器实现 - 基于执行机管理后端组件测试设计文档

支持TLV二进制通信协议，包含：
- MessageType枚举（0x01-0x36消息ID）
- FieldTag枚举（0x0001-0x0105字段标签）
- TlvEncoder类：消息编码功能
- TlvDecoder类：消息解码功能
"""

import struct
import enum
from typing import Dict, Any, List, Optional, Tuple
import binascii


class MessageType(enum.IntEnum):
    """消息类型枚举 - 0x01-0x36"""
    REGISTER_REQUEST = 0x01
    REGISTER_CHALLENGE = 0x02
    REGISTER_RESPONSE = 0x03
    REGISTER_RESULT = 0x04
    DEREGISTER_REQUEST = 0x05
    DEREGISTER_ACK = 0x06

    REPORT_MSG = 0x11
    REPORT_ACK = 0x12

    APP_LIST_QUERY = 0x21
    APP_LIST_RESPONSE = 0x22
    APP_INSTALL_REQUEST = 0x23
    APP_INSTALL_RESPONSE = 0x24
    SCREEN_CAP_QUERY = 0x25
    SCREEN_CAP_RESPONSE = 0x26

    SCRIPT_UPDATE_NOTIFY = 0x31
    SCRIPT_UPDATE_ACK = 0x32

    TASK_START_REQUEST = 0x33
    TASK_START_RESPONSE = 0x34
    TASK_STOP_REQUEST = 0x35
    TASK_STOP_RESPONSE = 0x36


class FieldTag(enum.IntEnum):
    """字段标签枚举 - 匹配服务端FieldTag定义"""
    # 基础字段 (0x0001-0x0012)
    HOSTNAME = 0x0001        # 执行机名称
    CHALLENGE_ID = 0x0002    # 挑战序号
    CHALLENGE = 0x0003       # 挑战随机数（16字节）
    USERNAME = 0x0004        # 认证用户名
    RESPONSE = 0x0005        # CHAP响应（16字节MD5）
    RESULT = 0x0006          # 结果码（0-成功，非0-失败）
    DESCRIPTION = 0x0007     # 描述信息
    TOKEN = 0x0008           # 会话Token（8字节）
    STATE = 0x0009           # 状态信息
    SERIAL_NO = 0x000A       # 手机序列号
    BRAND = 0x000B           # 手机品牌
    MODEL = 0x000C           # 手机型号
    OS = 0x000D              # 操作系统类型
    VERSION = 0x000E         # 版本号
    WMSIZE = 0x000F          # 屏幕分辨率
    IPV4 = 0x0010            # IPv4地址
    IPV6 = 0x0011            # IPv6地址
    BATTERY = 0x0012         # 电量百分比

    # 任务字段 (0x0020-0x002C)
    TASKID = 0x0020          # 任务ID
    SCRIPT_NAME = 0x0021     # 脚本名称
    PACKAGE = 0x0022         # 软件包名/安装包内容
    APPNAME = 0x0023         # App名称
    SCRIPT = 0x0024          # 安装脚本内容
    CRC = 0x0025             # CRC校验值
    FILENAME = 0x0026        # 文件名
    CONTENT = 0x0027         # 文件内容
    FILELEN = 0x0028         # 文件长度
    SCRIPTFILE = 0x0029      # 脚本文件内容
    PARAMETERS = 0x002A      # 参数列表
    PROCTYPE = 0x002B        # 多UE执行关系
    BLOCK = 0x002C           # VPN阻塞结果

    # 容器字段 (0x0100-0x0105)
    UE_LIST = 0x0100         # UE列表容器
    UE_ITEM = 0x0101         # 单个UE信息容器
    APP_LIST = 0x0102        # App列表容器
    APP_ITEM = 0x0103        # 单个App信息容器


class TlvEncoder:
    """TLV消息编码器"""

    @staticmethod
    def _encode_field(tag: int, value: Any) -> bytes:
        """编码单个字段"""
        if isinstance(value, str):
            # 特殊处理token字段：16字符十六进制字符串 -> 8字节二进制数据
            if tag == FieldTag.TOKEN and len(value) == 16:
                value_bytes = bytes.fromhex(value)
            else:
                value_bytes = value.encode('utf-8')
        elif isinstance(value, int):
            # 对于整数，使用4字节大端序编码（与服务端保持一致）
            value_bytes = struct.pack('>I', value)
        elif isinstance(value, bytes):
            value_bytes = value
        elif isinstance(value, list):
            # 对于数组/列表，使用TLV容器格式
            container_data = b''
            for item in value:
                if isinstance(item, dict):
                    # UE设备信息等复杂对象 - 需要包装在UE_ITEM容器中
                    if tag == FieldTag.UE_LIST:
                        # 为UE列表中的每个UE创建UE_ITEM容器
                        ue_item_data = b''
                        for field_name, field_value in item.items():
                            field_tag = TlvEncoder._get_field_tag(field_name)
                            if field_tag is not None:
                                field_bytes = TlvEncoder._encode_field(field_tag, field_value)
                                ue_item_data += field_bytes
                        # 包装在UE_ITEM容器中
                        ue_item_length = len(ue_item_data)
                        container_data += struct.pack('>HH', FieldTag.UE_ITEM, ue_item_length) + ue_item_data
                    else:
                        # 其他复杂对象 - 直接编码字段
                        for field_name, field_value in item.items():
                            field_tag = TlvEncoder._get_field_tag(field_name)
                            if field_tag is not None:
                                field_bytes = TlvEncoder._encode_field(field_tag, field_value)
                                container_data += field_bytes
                else:
                    # 简单类型数组
                    item_bytes = TlvEncoder._encode_field(0x0001, item)  # 使用通用标签
                    container_data += item_bytes
            value_bytes = container_data
        elif isinstance(value, dict):
            # 对于字典，使用TLV容器格式
            value_bytes = TlvEncoder.encode_message(value, 0x00)
        else:
            raise ValueError(f"Unsupported value type: {type(value)}")

        # TLV格式：Tag(2字节) + Length(2字节) + Value
        length = len(value_bytes)
        return struct.pack('>HH', tag, length) + value_bytes

    @staticmethod
    def encode_message(fields: Dict[str, Any], message_type: int) -> bytes:
        """编码完整消息"""
        # 消息头：Type(1字节) + Length(4字节大端序)
        message_data = b''

        # 编码各个字段
        for field_name, value in fields.items():
            # 根据字段名映射到标签
            tag = TlvEncoder._get_field_tag(field_name)
            if tag is not None:
                field_bytes = TlvEncoder._encode_field(tag, value)
                message_data += field_bytes

        # 计算总长度
        total_length = len(message_data)

        # 消息头：Type(1) + Length(4字节大端序) + Data
        header = bytes([message_type]) + struct.pack('>I', total_length)
        return header + message_data

    @staticmethod
    def _get_field_tag(field_name: str) -> Optional[int]:
        """根据字段名获取标签值"""
        field_mapping = {
            'hostname': FieldTag.HOSTNAME,          # 执行机名称
            'challenge_id': FieldTag.CHALLENGE_ID,  # 挑战序号
            'challenge': FieldTag.CHALLENGE,        # 挑战随机数
            'username': FieldTag.USERNAME,          # 认证用户名
            'response': FieldTag.RESPONSE,          # CHAP响应
            'result': FieldTag.RESULT,              # 结果码
            'description': FieldTag.DESCRIPTION,    # 描述信息
            'token': FieldTag.TOKEN,                # 会话Token
            'state': FieldTag.STATE,                # 状态信息
            'serial_no': FieldTag.SERIAL_NO,        # 手机序列号
            'brand': FieldTag.BRAND,                # 手机品牌
            'model': FieldTag.MODEL,                # 手机型号
            'os': FieldTag.OS,                      # 操作系统类型
            'version': FieldTag.VERSION,            # 版本号
            'wmsize': FieldTag.WMSIZE,              # 屏幕分辨率
            'ipv4': FieldTag.IPV4,                  # IPv4地址
            'ipv6': FieldTag.IPV6,                  # IPv6地址
            'battery': FieldTag.BATTERY,            # 电量百分比

            # 任务字段
            'taskid': FieldTag.TASKID,              # 任务ID
            'script_name': FieldTag.SCRIPT_NAME,    # 脚本名称
            'package': FieldTag.PACKAGE,            # 软件包名/安装包内容
            'appname': FieldTag.APPNAME,            # App名称
            'script': FieldTag.SCRIPT,              # 安装脚本内容
            'crc': FieldTag.CRC,                    # CRC校验值
            'filename': FieldTag.FILENAME,          # 文件名
            'content': FieldTag.CONTENT,            # 文件内容
            'filelen': FieldTag.FILELEN,            # 文件长度
            'scriptfile': FieldTag.SCRIPTFILE,      # 脚本文件内容
            'parameters': FieldTag.PARAMETERS,      # 参数列表
            'proctype': FieldTag.PROCTYPE,          # 多UE执行关系
            'block': FieldTag.BLOCK,                # VPN阻塞结果

            # 容器字段
            'ue_list': FieldTag.UE_LIST,            # UE列表容器
            'ue_item': FieldTag.UE_ITEM,            # 单个UE信息容器
            'app_list': FieldTag.APP_LIST,          # App列表容器
            'app_item': FieldTag.APP_ITEM,          # 单个App信息容器

            # 兼容性字段名
            'task_id': FieldTag.TASKID,
            'error_message': FieldTag.DESCRIPTION,
            
            # UE字段的兼容性别名
            'msisdn': FieldTag.SERIAL_NO,           # MSISDN -> serial_no
            'serial': FieldTag.SERIAL_NO,           # serial -> serial_no
            'vendor': FieldTag.BRAND,               # vendor -> brand
            'os_version': FieldTag.VERSION,         # os_version -> version
            'ip_v4': FieldTag.IPV4,                 # ip_v4 -> ipv4
            'battery_level': FieldTag.BATTERY,      # battery_level -> battery
        }
        return field_mapping.get(field_name)


class TlvDecoder:
    """TLV消息解码器"""

    @staticmethod
    def decode_message(data: bytes) -> Tuple[int, Dict[str, Any]]:
        """解码完整消息"""
        if len(data) < 5:
            raise ValueError("Message too short")

        # 解析消息头：Type(1) + Length(4字节大端序)
        message_type = data[0]
        total_length = struct.unpack('>I', data[1:5])[0]
        pos = 5

        # 验证总长度
        expected_total_length = len(data) - 5
        if total_length != expected_total_length:
            raise ValueError(f"Message length mismatch: header says {total_length}, actual {expected_total_length}")

        fields = {}
        while pos < len(data):
            if pos + 4 > len(data):
                break

            # 解析TLV：Tag(2) + Length(2)
            tag, length = struct.unpack('>HH', data[pos:pos+4])
            pos += 4

            if pos + length > len(data):
                raise ValueError(f"Invalid TLV length: pos={pos}, length={length}, data_len={len(data)}")

            value_bytes = data[pos:pos+length]
            pos += length

            # 解码值（传入tag以便特殊处理某些字段）
            value = TlvDecoder._decode_value(value_bytes, length, tag)
            field_name = TlvDecoder._get_field_name(tag)

            if field_name:
                fields[field_name] = value

        return message_type, fields

    @staticmethod
    def _decode_value(value_bytes: bytes, length: int, tag: int = 0) -> Any:
        """解码字段值
        
        Args:
            value_bytes: 原始字节数据
            length: 数据长度
            tag: 字段标签（用于特殊处理某些字段）
        """
        if length == 0:
            return None

        # 特殊处理token字段：8字节二进制 -> 16字符十六进制字符串
        if tag == FieldTag.TOKEN and length == 8:
            return value_bytes.hex()
        
        # 特殊处理整数类型（与服务端保持一致）
        if length == 4:
            try:
                return struct.unpack('>I', value_bytes)[0]
            except struct.error:
                pass  # 不是有效的4字节整数，继续其他解码
        elif length == 8:
            try:
                return struct.unpack('>Q', value_bytes)[0]
            except struct.error:
                pass  # 不是有效的8字节整数，继续其他解码

        # 尝试作为字符串解码
        try:
            return value_bytes.decode('utf-8')
        except UnicodeDecodeError:
            # 如果不是字符串，尝试作为二进制数据
            return value_bytes

    @staticmethod
    def _get_field_name(tag: int) -> Optional[str]:
        """根据标签获取字段名"""
        tag_mapping = {
            # 基础字段
            FieldTag.HOSTNAME: 'hostname',
            FieldTag.CHALLENGE_ID: 'challenge_id',
            FieldTag.CHALLENGE: 'challenge',
            FieldTag.USERNAME: 'username',
            FieldTag.RESPONSE: 'response',
            FieldTag.RESULT: 'result',
            FieldTag.DESCRIPTION: 'description',
            FieldTag.TOKEN: 'token',
            FieldTag.STATE: 'state',
            FieldTag.SERIAL_NO: 'serial_no',
            FieldTag.BRAND: 'brand',
            FieldTag.MODEL: 'model',
            FieldTag.OS: 'os',
            FieldTag.VERSION: 'version',
            FieldTag.WMSIZE: 'wmsize',
            FieldTag.IPV4: 'ipv4',
            FieldTag.IPV6: 'ipv6',
            FieldTag.BATTERY: 'battery',

            # 任务字段
            FieldTag.TASKID: 'taskid',
            FieldTag.SCRIPT_NAME: 'script_name',
            FieldTag.PACKAGE: 'package',
            FieldTag.APPNAME: 'appname',
            FieldTag.SCRIPT: 'script',
            FieldTag.CRC: 'crc',
            FieldTag.FILENAME: 'filename',
            FieldTag.CONTENT: 'content',
            FieldTag.FILELEN: 'filelen',
            FieldTag.SCRIPTFILE: 'scriptfile',
            FieldTag.PARAMETERS: 'parameters',
            FieldTag.PROCTYPE: 'proctype',
            FieldTag.BLOCK: 'block',

            # 容器字段
            FieldTag.UE_LIST: 'ue_list',
            FieldTag.UE_ITEM: 'ue_item',
            FieldTag.APP_LIST: 'app_list',
            FieldTag.APP_ITEM: 'app_item',

            # 兼容性字段名
            FieldTag.TASKID: 'task_id',
            FieldTag.DESCRIPTION: 'error_message',
        }
        return tag_mapping.get(tag)


def encode_register_request(hostname: str, username: str) -> bytes:
    """编码注册请求消息 (0x01)"""
    fields = {
        'hostname': hostname,
        'username': username
    }
    return TlvEncoder.encode_message(fields, MessageType.REGISTER_REQUEST)


def encode_register_response(challenge_id: int, username: str, response: str) -> bytes:
    """编码注册响应消息 (0x03)
    
    Args:
        challenge_id: 挑战序号
        username: 认证用户名
        response: CHAP响应（十六进制字符串，将被转换为16字节二进制数据）
    """
    # 将十六进制字符串转换为bytes（服务端期望16字节二进制数据，而不是32字节ASCII字符串）
    if isinstance(response, str):
        response_bytes = bytes.fromhex(response)
    else:
        response_bytes = response
    
    fields = {
        'challenge_id': challenge_id,
        'username': username,
        'response': response_bytes
    }
    return TlvEncoder.encode_message(fields, MessageType.REGISTER_RESPONSE)


def encode_heartbeat(token: str, state: int, ue_list: List[Dict[str, Any]]) -> bytes:
    """编码心跳消息 (0x11)"""
    fields = {
        'token': token,
        'state': state,
        'ue_list': ue_list
    }
    return TlvEncoder.encode_message(fields, MessageType.REPORT_MSG)


def encode_task_start_response(task_id: str, result: int, sub_result: List[Dict[str, Any]],
                               files: bytes, crc: str, block: str = "") -> bytes:
    """编码任务开始响应消息 (0x34)"""
    fields = {
        'task_id': task_id,
        'result': result,
        'sub_result': sub_result,
        'files': files,
        'crc': crc
    }
    if block:
        fields['block'] = block
    return TlvEncoder.encode_message(fields, MessageType.TASK_START_RESPONSE)


def encode_deregister_request(executor_name: str) -> bytes:
    """编码注销请求消息 (0x05)"""
    fields = {
        'executor_name': executor_name
    }
    return TlvEncoder.encode_message(fields, MessageType.DEREGISTER_REQUEST)


def encode_app_list_query(serial_no: str) -> bytes:
    """编码应用列表查询消息 (0x21)"""
    fields = {
        'serial_no': serial_no
    }
    return TlvEncoder.encode_message(fields, MessageType.APP_LIST_QUERY)


def encode_app_list_response(app_list: List[Dict[str, Any]]) -> bytes:
    """编码应用列表响应消息 (0x22)"""
    fields = {
        'app_list': app_list
    }
    return TlvEncoder.encode_message(fields, MessageType.APP_LIST_RESPONSE)


def encode_app_install_response(result: int, error_msg: str = "") -> bytes:
    """编码应用安装响应消息 (0x24)"""
    fields = {
        'result': result
    }
    if error_msg:
        fields['error_message'] = error_msg
    return TlvEncoder.encode_message(fields, MessageType.APP_INSTALL_RESPONSE)


def encode_screencap_query(serial_no: str) -> bytes:
    """编码截屏查询消息 (0x25)"""
    fields = {
        'serial_no': serial_no
    }
    return TlvEncoder.encode_message(fields, MessageType.SCREEN_CAP_QUERY)


def encode_screencap_response(image_data: bytes) -> bytes:
    """编码截屏响应消息 (0x26)"""
    fields = {
        'image_data': image_data
    }
    return TlvEncoder.encode_message(fields, MessageType.SCREEN_CAP_RESPONSE)


def encode_app_install_request(serial_no: str, task_id: str, app_name: str, package_data: bytes) -> bytes:
    """编码应用安装请求消息 (0x23)"""
    fields = {
        'serial_no': serial_no,
        'task_id': task_id,
        'app_name': app_name,
        'package_data': package_data
    }
    return TlvEncoder.encode_message(fields, MessageType.APP_INSTALL_REQUEST)


def encode_script_update_notify(script_name: str, version: str, file_data: bytes, crc: str) -> bytes:
    """编码脚本更新通知消息 (0x31)"""
    fields = {
        'script_name': script_name,
        'version': version,
        'files': file_data,  # 使用files字段存储脚本数据
        'crc': crc
    }
    return TlvEncoder.encode_message(fields, MessageType.SCRIPT_UPDATE_NOTIFY)


def encode_script_update_ack(script_name: str, version: str, result: int, error_msg: str = "") -> bytes:
    """编码脚本更新确认消息 (0x32)"""
    fields = {
        'script_name': script_name,
        'version': version,
        'result': result
    }
    if error_msg:
        fields['error_message'] = error_msg
    return TlvEncoder.encode_message(fields, MessageType.SCRIPT_UPDATE_ACK)


def encode_task_start_request(task_id: int, script_name: str, version: str,
                            serial_no_list: List[str], proctype: int, parameters: str) -> bytes:
    """编码任务开始请求消息 (0x33)"""
    fields = {
        'taskid': task_id,
        'script_name': script_name,
        'version': version,
        'serial_no_list': serial_no_list,
        'proctype': proctype,
        'parameters': parameters
    }
    return TlvEncoder.encode_message(fields, MessageType.TASK_START_REQUEST)


def encode_task_start_response(task_id: str, result: int, sub_result: List[Dict[str, Any]],
                               files: bytes, crc: str, block: str = "") -> bytes:
    """编码任务开始响应消息 (0x34)"""
    fields = {
        'task_id': task_id,
        'result': result,
        'sub_result': sub_result,
        'files': files,
        'crc': crc
    }
    if block:
        fields['block'] = block
    return TlvEncoder.encode_message(fields, MessageType.TASK_START_RESPONSE)


def encode_task_stop_request(task_id: str) -> bytes:
    """编码任务停止请求消息 (0x35)"""
    fields = {
        'task_id': task_id
    }
    return TlvEncoder.encode_message(fields, MessageType.TASK_STOP_REQUEST)


def encode_task_stop_response(task_id: str, result: int) -> bytes:
    """编码任务停止响应消息 (0x36)
    
    Args:
        task_id: 任务ID
        result: 停止结果 (0=成功, 非0=失败)
    """
    fields = {
        'task_id': task_id,
        'result': result
    }
    return TlvEncoder.encode_message(fields, MessageType.TASK_STOP_RESPONSE)


def decode_message(data: bytes) -> Tuple[int, Dict[str, Any]]:
    """解码消息的便捷函数"""
    return TlvDecoder.decode_message(data)
