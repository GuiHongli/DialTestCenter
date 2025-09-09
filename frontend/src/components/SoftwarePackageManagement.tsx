/**
 * 软件包管理组件
 */

import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Popconfirm,
  message,
  Upload,
  Modal,
  Form,
  Input,
  Select,
  Card,
  Statistic,
  Row,
  Col,
  Tag,
  Tooltip,
  Typography,
} from 'antd';
import {
  UploadOutlined,
  DownloadOutlined,
  DeleteOutlined,
  EditOutlined,
  InboxOutlined,
  FileZipOutlined,
  AndroidOutlined,
  AppleOutlined,
  ReloadOutlined,
  AppstoreOutlined,
} from '@ant-design/icons';
import type { UploadProps } from 'antd';
import { 
  SoftwarePackage, 
  SoftwarePackageListParams,
  SoftwarePackageUpdateParams 
} from '../types/softwarePackage';
import {
  getSoftwarePackages,
  deleteSoftwarePackage,
  downloadSoftwarePackage,
  updateSoftwarePackage,
  uploadSoftwarePackage,
  uploadZipPackage,
  getSoftwarePackageStatistics,
} from '../services/softwarePackageService';

const { Dragger } = Upload;
const { Option } = Select;
const { TextArea } = Input;
const { Title, Text } = Typography;

interface SoftwarePackageManagementProps {
  // 可以添加props类型定义
}

const SoftwarePackageManagement: React.FC<SoftwarePackageManagementProps> = () => {
  const [softwarePackages, setSoftwarePackages] = useState<SoftwarePackage[]>([]);
  const [loading, setLoading] = useState(false);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [filters, setFilters] = useState<SoftwarePackageListParams>({});
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingPackage, setEditingPackage] = useState<SoftwarePackage | null>(null);
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [uploadType, setUploadType] = useState<'single' | 'zip'>('single');
  const [statistics, setStatistics] = useState({ android: 0, ios: 0, total: 0 });
  const [form] = Form.useForm();

  // 加载软件包列表
  const loadSoftwarePackages = async (params: SoftwarePackageListParams = {}) => {
    setLoading(true);
    try {
      const response = await getSoftwarePackages({
        page: pagination.current,
        pageSize: pagination.pageSize,
        ...filters,
        ...params,
      });
      
      setSoftwarePackages(response.data);
      setPagination(prev => ({
        ...prev,
        total: response.total,
        current: response.page,
      }));
    } catch (error) {
      message.error('加载软件包列表失败');
      console.error('Error loading software packages:', error);
    } finally {
      setLoading(false);
    }
  };

  // 加载统计信息
  const loadStatistics = async () => {
    try {
      const response = await getSoftwarePackageStatistics();
      if (response.success) {
        setStatistics(response.data);
      }
    } catch (error) {
      console.error('Error loading statistics:', error);
    }
  };

  useEffect(() => {
    loadSoftwarePackages();
    loadStatistics();
  }, []);

  // 删除软件包
  const handleDelete = async (id: number) => {
    try {
      await deleteSoftwarePackage(id);
      message.success('删除成功');
      loadSoftwarePackages();
      loadStatistics();
    } catch (error) {
      message.error('删除失败');
      console.error('Error deleting software package:', error);
    }
  };

  // 下载软件包
  const handleDownload = async (record: SoftwarePackage) => {
    try {
      await downloadSoftwarePackage(record.id, record.softwareName);
      message.success('下载成功');
    } catch (error) {
      message.error('下载失败');
      console.error('Error downloading software package:', error);
    }
  };

  // 编辑软件包
  const handleEdit = (record: SoftwarePackage) => {
    setEditingPackage(record);
    form.setFieldsValue({
      softwareName: record.softwareName,
      description: record.description,
    });
    setEditModalVisible(true);
  };

  // 保存编辑
  const handleSaveEdit = async () => {
    try {
      const values = await form.validateFields();
      if (editingPackage) {
        const updateParams: SoftwarePackageUpdateParams = {
          softwareName: values.softwareName,
          description: values.description,
        };
        await updateSoftwarePackage(editingPackage.id, updateParams);
        message.success('更新成功');
        setEditModalVisible(false);
        loadSoftwarePackages();
      }
    } catch (error) {
      message.error('更新失败');
      console.error('Error updating software package:', error);
    }
  };

  // 单个文件上传
  const handleSingleUpload: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const response = await uploadSoftwarePackage(file as File);
      if (response.success) {
        message.success('上传成功');
        onSuccess?.(response);
        loadSoftwarePackages();
        loadStatistics();
      } else {
        message.error(response.message || '上传失败');
        onError?.(new Error(response.message || 'Upload failed'));
      }
    } catch (error) {
      message.error('上传失败');
      onError?.(error as Error);
    }
  };

  // ZIP包上传
  const handleZipUpload: UploadProps['customRequest'] = async ({ file, onSuccess, onError }) => {
    try {
      const response = await uploadZipPackage(file as File);
      if (response.success) {
        message.success(`ZIP包上传成功，共上传 ${response.count} 个软件包`);
        onSuccess?.(response);
        loadSoftwarePackages();
        loadStatistics();
      } else {
        message.error(response.message || '上传失败');
        onError?.(new Error(response.message || 'Upload failed'));
      }
    } catch (error) {
      message.error('上传失败');
      onError?.(error as Error);
    }
  };

  // 格式化文件大小
  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 B';
    
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    if (i === 0) {
      return `${bytes} ${sizes[i]}`;
    } else {
      return `${(bytes / Math.pow(k, i)).toFixed(i === 1 ? 0 : 2)} ${sizes[i]}`;
    }
  };

  // 格式化日期时间
  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('zh-CN');
  };

  // 表格列定义
  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a: SoftwarePackage, b: SoftwarePackage) => a.id - b.id,
    },
    {
      title: '软件名称',
      dataIndex: 'softwareName',
      key: 'softwareName',
      width: 200,
      sorter: (a: SoftwarePackage, b: SoftwarePackage) => a.softwareName.localeCompare(b.softwareName),
      render: (text: string) => (
        <Space>
          <AppstoreOutlined />
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: '平台',
      dataIndex: 'platform',
      key: 'platform',
      width: 120,
      filters: [
        { text: 'Android', value: 'android' },
        { text: 'iOS', value: 'ios' },
      ],
      onFilter: (value: any, record: SoftwarePackage) => record.platform === value,
      render: (platform: string) => (
        <Tag color={platform === 'android' ? 'green' : 'blue'} icon={platform === 'android' ? <AndroidOutlined /> : <AppleOutlined />}>
          {platform === 'android' ? 'Android' : 'iOS'}
        </Tag>
      ),
    },
    {
      title: '文件大小',
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 120,
      sorter: (a: SoftwarePackage, b: SoftwarePackage) => a.fileSize - b.fileSize,
      render: (size: number) => formatFileSize(size),
    },
    {
      title: '创建者',
      dataIndex: 'creator',
      key: 'creator',
      width: 120,
      sorter: (a: SoftwarePackage, b: SoftwarePackage) => a.creator.localeCompare(b.creator),
    },
    {
      title: '创建时间',
      dataIndex: 'createdTime',
      key: 'createdTime',
      width: 180,
      sorter: (a: SoftwarePackage, b: SoftwarePackage) => 
        new Date(a.createdTime).getTime() - new Date(b.createdTime).getTime(),
      render: (text: string) => formatDateTime(text),
    },
    {
      title: '操作',
      key: 'action',
      width: 150,
      render: (_, record: SoftwarePackage) => (
        <Space size="small">
          <Tooltip title="下载">
            <Button
              type="text"
              icon={<DownloadOutlined />}
              onClick={() => handleDownload(record)}
            />
          </Tooltip>
          <Tooltip title="编辑">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </Tooltip>
          <Popconfirm
            title="确定要删除这个软件包吗？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Tooltip title="删除">
              <Button
                type="text"
                danger
                icon={<DeleteOutlined />}
              />
            </Tooltip>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      {/* 页面标题和操作按钮 */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center', 
        marginBottom: '24px' 
      }}>
        <div style={{ textAlign: 'left' }}>
          <Title level={2} style={{ margin: 0, textAlign: 'left' }}>
            <AppstoreOutlined style={{ marginRight: '8px' }} />
            软件包管理
          </Title>
          <Text type="secondary" style={{ fontSize: '14px', textAlign: 'left' }}>
            管理Android和iOS软件包，支持上传、下载、编辑和删除操作
          </Text>
        </div>
        <Space>
          <Button
            type="primary"
            icon={<UploadOutlined />}
            onClick={() => {
              setUploadType('single');
              setUploadModalVisible(true);
            }}
          >
            上传单个软件包
          </Button>
          <Button
            icon={<FileZipOutlined />}
            onClick={() => {
              setUploadType('zip');
              setUploadModalVisible(true);
            }}
          >
            上传ZIP包
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => loadSoftwarePackages()}
          >
            刷新
          </Button>
        </Space>
      </div>

      {/* 统计信息卡片 */}
      <Row gutter={16} style={{ marginBottom: '24px' }}>
        <Col span={8}>
          <Card>
            <Statistic
              title="Android软件包"
              value={statistics.android}
              prefix={<AndroidOutlined />}
              valueStyle={{ color: '#3f8600' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="iOS软件包"
              value={statistics.ios}
              prefix={<AppleOutlined />}
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
        <Col span={8}>
          <Card>
            <Statistic
              title="总计"
              value={statistics.total}
              valueStyle={{ color: '#722ed1' }}
            />
          </Card>
        </Col>
      </Row>

      {/* 筛选器 */}
      <Card style={{ marginBottom: '16px' }}>
        <Space>
          <Select
            placeholder="选择平台"
            style={{ width: 120 }}
            allowClear
            onChange={(value) => {
              setFilters(prev => ({ ...prev, platform: value }));
              loadSoftwarePackages({ platform: value });
            }}
          >
            <Option value="android">Android</Option>
            <Option value="ios">iOS</Option>
          </Select>
          <Input
            placeholder="搜索软件名称"
            style={{ width: 200 }}
            onChange={(e) => {
              const value = e.target.value;
              setFilters(prev => ({ ...prev, softwareName: value }));
              loadSoftwarePackages({ softwareName: value });
            }}
          />
        </Space>
      </Card>

      {/* 软件包列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={softwarePackages}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => `第 ${range[0]}-${range[1]} 条，共 ${total} 条`,
            onChange: (page, pageSize) => {
              setPagination(prev => ({ ...prev, current: page, pageSize: pageSize || 10 }));
              loadSoftwarePackages({ page, pageSize });
            },
          }}
          scroll={{ x: 800 }}
          size="middle"
        />
      </Card>

      {/* 编辑模态框 */}
      <Modal
        title="编辑软件包信息"
        open={editModalVisible}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="softwareName"
            label="软件名称"
            rules={[{ required: true, message: '请输入软件名称' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 上传模态框 */}
      <Modal
        title={uploadType === 'single' ? '上传单个软件包' : '上传ZIP包'}
        open={uploadModalVisible}
        onCancel={() => setUploadModalVisible(false)}
        footer={null}
        width={600}
      >
        <Dragger
          name="file"
          multiple={false}
          accept={uploadType === 'single' ? '.apk,.ipa' : '.zip'}
          customRequest={uploadType === 'single' ? handleSingleUpload : handleZipUpload}
          showUploadList={false}
        >
          <p className="ant-upload-drag-icon">
            <InboxOutlined />
          </p>
          <p className="ant-upload-text">
            {uploadType === 'single' 
              ? '点击或拖拽APK/IPA文件到此区域上传' 
              : '点击或拖拽ZIP文件到此区域上传'
            }
          </p>
          <p className="ant-upload-hint">
            {uploadType === 'single' 
              ? '支持单个APK或IPA文件上传' 
              : '支持包含多个APK/IPA文件的ZIP包上传'
            }
          </p>
        </Dragger>
      </Modal>
    </div>
  );
};

export default SoftwarePackageManagement;
