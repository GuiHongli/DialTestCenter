import unittest

from .base import BaseTestCase
from .config import WS_ENABLE, AGENT_NTLM_HASH
from .tlv_codec import (MessageType, encode_app_list_query, encode_app_list_response,
                       encode_app_install_request, encode_app_install_response,
                       encode_screencap_query, encode_screencap_response)


@unittest.skipUnless(WS_ENABLE, "WS测试默认关闭，设置 EXEC_WS_ENABLE=1 以启用")
class TestUEManagementIT05(BaseTestCase):
    """IT-05: UE应用管理流程"""

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_05_001_app_list_query(self):
        """IT-05-001: 应用列表响应（Agent发送AppList-Response）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 注意：AppList-Query是CloudUDN主动发送给Agent的，这里只测试Agent的响应
            # 模拟Agent响应应用列表
            app_list = [
                {"name": "TestApp1", "version": "1.0.0", "package": "com.test.app1"},
                {"name": "TestApp2", "version": "2.1.0", "package": "com.test.app2"},
                {"name": "SystemApp", "version": "1.0.0", "package": "com.system.app"}
            ]
            response_msg = encode_app_list_response(app_list)
            self._ws_send_tlv(ws, response_msg)
            
            # 验证消息发送成功（没有异常）
            import time
            time.sleep(0.2)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_05_002_app_install_request(self):
        """IT-05-002: 应用安装响应（Agent发送AppInstall-Response）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 注意：AppInstall-Request是CloudUDN主动发送给Agent的，这里只测试Agent的响应
            # 模拟安装成功响应
            response_msg = encode_app_install_response(0)  # 0=成功
            self._ws_send_tlv(ws, response_msg)
            
            # 验证消息发送成功（没有异常）
            import time
            time.sleep(0.2)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_05_003_app_install_failure(self):
        """IT-05-003: 应用安装失败"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 发送应用安装请求
            serial_no = "SN002"
            task_id = "INSTALL_TASK_002"
            app_name = "CorruptedApp.apk"
            package_data = b"corrupted_data"

            install_msg = encode_app_install_request(serial_no, task_id, app_name, package_data)
            self._ws_send_tlv(ws, install_msg)

            # 模拟安装失败响应
            response_msg = encode_app_install_response(1, "Package parsing failed")  # 1=失败
            self._ws_send_tlv(ws, response_msg)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_05_004_screencap_query(self):
        """IT-05-004: 截屏响应（Agent发送ScreenCap-Response）"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 注意：ScreenCap-Query是CloudUDN主动发送给Agent的，这里只测试Agent的响应
            # 模拟截屏响应（PNG格式图片数据）
            # 创建一个最小的PNG文件数据（为了测试）
            png_header = b'\x89PNG\r\n\x1a\n'  # PNG文件头
            image_data = png_header + b"fake_png_data_" + b"B" * 200
            response_msg = encode_screencap_response(image_data)
            self._ws_send_tlv(ws, response_msg)
            
            # 验证消息发送成功（没有异常）
            import time
            time.sleep(0.2)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_05_005_multiple_ue_app_management(self):
        """IT-05-005: 多UE应用管理"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 为多个UE设备执行应用管理操作
            ues = ["SN001", "SN002", "SN003"]

            for serial_no in ues:
                # 查询应用列表
                query_msg = encode_app_list_query(serial_no)
                self._ws_send_tlv(ws, query_msg)

                # 模拟响应
                app_list = [
                    {"name": f"App_{serial_no}_1", "version": "1.0.0"},
                    {"name": f"App_{serial_no}_2", "version": "1.0.0"}
                ]
                response_msg = encode_app_list_response(app_list)
                self._ws_send_tlv(ws, response_msg)

                # 为第一个UE安装应用
                if serial_no == "SN001":
                    install_msg = encode_app_install_request(
                        serial_no, f"TASK_{serial_no}", "TestApp.apk", b"apk_data"
                    )
                    self._ws_send_tlv(ws, install_msg)

                    # 模拟安装成功
                    install_response = encode_app_install_response(0)
                    self._ws_send_tlv(ws, install_response)

        finally:
            ws.close()

    @unittest.skipUnless(AGENT_NTLM_HASH, "未提供 EXEC_AGENT_NTLM_HASH，无法计算")
    def test_it_05_006_app_management_error_handling(self):
        """IT-05-006: 应用管理错误处理"""
        # 注册并保持连接
        ws, token = self._ws_register_and_keep_connection_tlv()
        try:
            # 测试不存在的UE设备
            query_msg = encode_app_list_query("NON_EXISTENT_SN")
            self._ws_send_tlv(ws, query_msg)

            # 模拟错误响应
            error_response = encode_app_install_response(1, "UE device not found")
            self._ws_send_tlv(ws, error_response)

            # 测试无效的应用数据
            install_msg = encode_app_install_request(
                "SN001", "TASK_INVALID", "invalid.apk", b""
            )
            self._ws_send_tlv(ws, install_msg)

            # 模拟数据错误响应
            data_error_response = encode_app_install_response(1, "Invalid package data")
            self._ws_send_tlv(ws, data_error_response)

        finally:
            ws.close()
