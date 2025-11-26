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
  Card,
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
import {
  getSoftwarePackages,
  deleteSoftwarePackage,
  downloadSoftwarePackage,
  updateSoftwarePackage,
  uploadSoftwarePackage,
  uploadZipPackage,
  getSoftwarePackageStatistics,
} from '../services/softwarePackageService.js';
import { useTranslation } from '../hooks/useTranslation.js';
import { usePermission } from '../hooks/usePermission.js';

const { Dragger } = Upload;
const { TextArea } = Input;
const { Title, Text } = Typography;

const SoftwarePackageManagement = () => {
  const { translateSoftwarePackage, translateCommon } = useTranslation();
  const { hasPagePermission } = usePermission();
  
  // 检查各种操作权限
  const canUpload = hasPagePermission('software-package', 'upload');
  const canDownload = hasPagePermission('software-package', 'download');
  const canEdit = hasPagePermission('software-package', 'edit');
  const canDelete = hasPagePermission('software-package', 'delete');
  const [softwarePackages, setSoftwarePackages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedRowKeys, setSelectedRowKeys] = useState([]);
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });
  const [filters, setFilters] = useState({});
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [editingPackage, setEditingPackage] = useState(null);
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [uploadType, setUploadType] = useState('single');
  const [form] = Form.useForm();
  const [fileList, setFileList] = useState([]);

  // 加载软件包列表
  const loadSoftwarePackages = async (params = {}) => {
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
      message.error(translateSoftwarePackage('messages.loadFailed'));
      console.error('Error loading software packages:', error);
    } finally {
      setLoading(false);
    }
  };

  // 加载统计信息
  const loadStatistics = async () => {
    try {
      const response = await getSoftwarePackageStatistics();
      // 这里可以设置统计信息状态，如果需要显示统计信息的话
      console.log('Statistics loaded:', response.data);
    } catch (error) {
      console.error('Error loading statistics:', error);
    }
  };

  useEffect(() => {
    loadSoftwarePackages();
  }, []);

  // 删除软件包
  const handleDelete = async (id) => {
    try {
      await deleteSoftwarePackage(id);
      message.success(translateSoftwarePackage('messages.deleteSuccess'));
      loadSoftwarePackages();
    } catch (error) {
      message.error(translateSoftwarePackage('messages.deleteFailed'));
      console.error('Error deleting software package:', error);
    }
  };

  // 下载软件包（单个）
  const handleDownload = async (record) => {
    try {
      await downloadSoftwarePackage([record.id], record.softwareName);
      message.success(translateSoftwarePackage('messages.downloadSuccess'));
    } catch (error) {
      message.error(translateSoftwarePackage('messages.downloadFailed'));
      console.error('Error downloading software package:', error);
    }
  };

  // 批量下载软件包
  const handleBatchDownload = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning(translateSoftwarePackage('messages.selectPackagesToDownload') || '请选择要下载的软件包');
      return;
    }
    
    try {
      // 生成可读的时间戳格式：YYYYMMDDHHmmss
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      const day = String(now.getDate()).padStart(2, '0');
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
      const zipFileName = `software_packages_${year}${month}${day}${hours}${minutes}${seconds}`;
      
      await downloadSoftwarePackage(selectedRowKeys, zipFileName);
      message.success(translateSoftwarePackage('messages.batchDownloadSuccess') || `成功下载 ${selectedRowKeys.length} 个软件包`);
      setSelectedRowKeys([]);
    } catch (error) {
      message.error(translateSoftwarePackage('messages.batchDownloadFailed') || '批量下载失败');
      console.error('Error batch downloading software packages:', error);
    }
  };

  // 编辑软件包
  const handleEdit = (record) => {
    setEditingPackage(record);
    form.setFieldsValue({
      description: record.description,
    });
    setEditModalVisible(true);
  };

  // 保存编辑
  const handleSaveEdit = async () => {
    try {
      const values = await form.validateFields();
      if (editingPackage) {
        const updateParams = {
          softwareName: values.softwareName,
          description: values.description,
        };
        await updateSoftwarePackage(editingPackage.id, updateParams);
        message.success(translateSoftwarePackage('messages.updateSuccess'));
        setEditModalVisible(false);
        loadSoftwarePackages();
      }
    } catch (error) {
      message.error(translateSoftwarePackage('messages.updateFailed'));
      console.error('Error updating software package:', error);
    }
  };

  // 带描述信息的上传处理
  const handleUploadWithDescription = async () => {
    try {
      // 检查文件是否已选择
      if (fileList.length === 0) {
        message.error('请选择要上传的文件');
        return;
      }

      const file = fileList[0].originFileObj || fileList[0];
      if (!file) {
        message.error('文件无效');
        return;
      }

      // 获取描述信息
      const description = form.getFieldValue('description');
      
      if (uploadType === 'single') {
        try {
          const response = await uploadSoftwarePackage(file, description);
          if (response.success) {
            message.success(translateSoftwarePackage('messages.uploadSuccess'));
            form.resetFields();
            setFileList([]);
            setUploadModalVisible(false);
            loadSoftwarePackages();
          } else {
            message.error(response.message || translateSoftwarePackage('messages.uploadFailed'));
          }
        } catch (error) {
          // 检查是否是重复上传错误
          if (error.message && (error.message.includes('已存在的软件名称') || error.message.includes('软件名称已存在'))) {
            // 提取软件包名称
            let packageName = '该软件包';
            if (error.message.includes('软件名称已存在:')) {
              const parts = error.message.split('软件名称已存在: ');
              if (parts.length > 1) {
                packageName = parts[parts.length - 1];
              }
            } else if (error.message.includes(':')) {
              const parts = error.message.split(': ');
              if (parts.length > 1) {
                packageName = parts[parts.length - 1];
              }
            }
            
            // 显示覆盖确认对话框
            Modal.confirm({
              title: translateSoftwarePackage('messages.packageExists'),
              content: translateSoftwarePackage('messages.packageExistsContent', { packageName }),
              okText: translateSoftwarePackage('messages.overwriteUpdate'),
              cancelText: translateSoftwarePackage('messages.skip'),
              onOk: async () => {
                try {
                  const overwriteResponse = await uploadSoftwarePackage(file, description, true);
                  if (overwriteResponse.success) {
                    message.success(translateSoftwarePackage('messages.uploadSuccess'));
                    form.resetFields();
                    setFileList([]);
                    setUploadModalVisible(false);
                    loadSoftwarePackages();
                  } else {
                    message.error(overwriteResponse.message || translateSoftwarePackage('messages.overwriteFailed'));
                  }
                } catch (overwriteError) {
                  message.error(translateSoftwarePackage('messages.overwriteFailed'));
                }
              },
              onCancel: () => {
                message.info(translateSoftwarePackage('messages.skippedDuplicate'));
              }
            });
          } else {
            message.error(error.message || translateSoftwarePackage('messages.uploadFailed'));
          }
        }
      } else {
        try {
          const response = await uploadZipPackage(file, false, description);
          if (response.success) {
            message.success(translateSoftwarePackage('messages.zipUploadSuccess', { count: response.count }));
            form.resetFields();
            setFileList([]);
            setUploadModalVisible(false);
            loadSoftwarePackages();
            loadStatistics();
          } else {
            message.error(response.message || translateSoftwarePackage('messages.uploadFailed'));
          }
        } catch (error) {
          // 检查是否是重复上传错误
          if (error.message && (error.message.includes('已存在的软件名称') || error.message.includes('ZIP包中包含已存在的软件名称'))) {
            // 提取软件包名称
            let packageName = '该软件包';
            if (error.message.includes('ZIP包中包含已存在的软件名称:')) {
              const parts = error.message.split('ZIP包中包含已存在的软件名称: ');
              if (parts.length > 1) {
                packageName = parts[parts.length - 1];
              }
            } else if (error.message.includes(':')) {
              const parts = error.message.split(': ');
              if (parts.length > 1) {
                packageName = parts[parts.length - 1];
              }
            }
            
            // 显示覆盖确认对话框
            Modal.confirm({
              title: translateSoftwarePackage('messages.packageExists'),
              content: translateSoftwarePackage('messages.packageExistsContent', { packageName }),
              okText: translateSoftwarePackage('messages.overwriteUpdate'),
              cancelText: translateSoftwarePackage('messages.skip'),
              onOk: async () => {
                try {
                  const overwriteResponse = await uploadZipPackage(file, true, description);
                  if (overwriteResponse.success) {
                    message.success(translateSoftwarePackage('messages.zipUploadSuccess', { count: overwriteResponse.count }));
                    form.resetFields();
                    setFileList([]);
                    setUploadModalVisible(false);
                    loadSoftwarePackages();
                    loadStatistics();
                  } else {
                    message.error(overwriteResponse.message || translateSoftwarePackage('messages.overwriteFailed'));
                  }
                } catch (overwriteError) {
                  message.error(translateSoftwarePackage('messages.overwriteFailed'));
                }
              },
              onCancel: () => {
                message.info(translateSoftwarePackage('messages.skippedDuplicate'));
              }
            });
          } else {
            message.error(error.message || translateSoftwarePackage('messages.uploadFailed'));
          }
        }
      }
    } catch (error) {
      console.error('Upload error:', error);
      message.error(translateSoftwarePackage('messages.uploadFailed'));
    }
  };

  // 格式化文件大小
  const formatFileSize = (bytes) => {
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


  // 表格列定义
  const columns = [
    {
      title: translateSoftwarePackage('table.id'),
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a, b) => a.id - b.id,
    },
    {
      title: translateSoftwarePackage('table.softwareName'),
      dataIndex: 'softwareName',
      key: 'softwareName',
      width: 200,
      sorter: (a, b) => a.softwareName.localeCompare(b.softwareName),
      render: (text) => (
        <Space>
          <AppstoreOutlined />
          <Text strong>{text}</Text>
        </Space>
      ),
    },
    {
      title: translateSoftwarePackage('table.platform'),
      key: 'platform',
      width: 120,
      filters: [
        { text: 'Android', value: 'android' },
        { text: 'iOS', value: 'ios' },
      ],
      onFilter: (value, record) => {
        const fileName = record.softwareName.toLowerCase();
        if (value === 'android') return fileName.endsWith('.apk');
        if (value === 'ios') return fileName.endsWith('.ipa');
        return false;
      },
      render: (text, record) => {
        const fileName = record.softwareName.toLowerCase();
        const isAndroid = fileName.endsWith('.apk');
        const isIOS = fileName.endsWith('.ipa');
        
        if (isAndroid) {
          return (
            <Tag color="green" icon={<AndroidOutlined />}>
              Android
            </Tag>
          );
        } else if (isIOS) {
          return (
            <Tag color="blue" icon={<AppleOutlined />}>
              iOS
            </Tag>
          );
        }
        return <Tag>Unknown</Tag>;
      },
    },
    {
      title: translateSoftwarePackage('table.fileSize'),
      dataIndex: 'fileSize',
      key: 'fileSize',
      width: 120,
      sorter: (a, b) => a.fileSize - b.fileSize,
      render: (size) => formatFileSize(size),
    },
    {
      title: translateSoftwarePackage('table.description'),
      dataIndex: 'description',
      key: 'description',
      width: 200,
      render: (text) => (
        <Text type="secondary">{text || '-'}</Text>
      ),
    },
    {
      title: translateSoftwarePackage('table.actions'),
      key: 'action',
      width: 150,
      render: (_, record) => (
        <Space size="small">
          {canDownload && (
            <Tooltip title={translateSoftwarePackage('table.download')}>
              <Button
                type="text"
                icon={<DownloadOutlined />}
                onClick={() => handleDownload(record)}
              />
            </Tooltip>
          )}
          {canEdit && (
            <Tooltip title={translateSoftwarePackage('table.edit')}>
              <Button
                type="text"
                icon={<EditOutlined />}
                onClick={() => handleEdit(record)}
              />
            </Tooltip>
          )}
          {canDelete && (
            <Popconfirm
              title={translateSoftwarePackage('messages.confirmDelete')}
              onConfirm={() => handleDelete(record.id)}
              okText={translateCommon('confirm')}
              cancelText={translateCommon('cancel')}
            >
              <Tooltip title={translateSoftwarePackage('table.delete')}>
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                />
              </Tooltip>
            </Popconfirm>
          )}
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
            {translateSoftwarePackage('title')}
          </Title>
          <Text type="secondary" style={{ fontSize: '14px', textAlign: 'left' }}>
            {translateSoftwarePackage('description')}
          </Text>
        </div>
        <Space>
          {canUpload && (
            <>
              <Button
                type="primary"
                icon={<UploadOutlined />}
                onClick={() => {
                  setUploadType('single');
                  setUploadModalVisible(true);
                }}
              >
                {translateSoftwarePackage('uploadSingle')}
              </Button>
              <Button
                icon={<FileZipOutlined />}
                onClick={() => {
                  setUploadType('zip');
                  setUploadModalVisible(true);
                }}
              >
                {translateSoftwarePackage('uploadZip')}
              </Button>
            </>
          )}
          {canDownload && (
            <Button
              icon={<DownloadOutlined />}
              onClick={handleBatchDownload}
            >
              {selectedRowKeys.length > 0 
                ? `${translateSoftwarePackage('messages.batchDownload')} (${selectedRowKeys.length})`
                : translateSoftwarePackage('messages.batchDownload')}
            </Button>
          )}
          <Button
            icon={<ReloadOutlined />}
            onClick={() => loadSoftwarePackages()}
          >
            {translateSoftwarePackage('refresh')}
          </Button>
        </Space>
      </div>

      {/* 软件名称搜索筛选器 */}
      <Card style={{ marginBottom: '16px' }}>
        <Row gutter={[16, 16]} style={{ textAlign: 'left' }}>
          <Col xs={24} sm={8} md={6}>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center',
              height: '100%',
              paddingRight: '12px'
            }}>
              <span style={{ 
                fontSize: '14px', 
                color: '#262626',
                fontWeight: 500,
                whiteSpace: 'nowrap'
              }}>
                {translateSoftwarePackage('filters.softwareName')}:
              </span>
            </div>
          </Col>
          <Col xs={24} sm={16} md={14}>
            <Input
              placeholder={translateSoftwarePackage('filters.softwareNamePlaceholder')}
              value={filters.softwareName || ''}
              onChange={(e) => {
                const value = e.target.value;
                setFilters(prev => ({ ...prev, softwareName: value }));
              }}
              allowClear
              onPressEnter={() => loadSoftwarePackages({ softwareName: filters.softwareName, page: 1 })}
              size="middle"
              style={{ 
                borderRadius: '8px',
                border: '1px solid #d9d9d9'
              }}
            />
          </Col>
          <Col xs={24} sm={8} md={4}>
            <Button
              type="primary"
              onClick={() => loadSoftwarePackages({ softwareName: filters.softwareName, page: 1 })}
              size="middle"
              style={{ 
                borderRadius: '8px',
                minWidth: '100px',
                boxShadow: '0 2px 4px rgba(24, 144, 255, 0.2)'
              }}
            >
              {translateCommon('search')}
            </Button>
          </Col>
        </Row>
      </Card>

      {/* 软件包列表 */}
      <Card>
        <Table
          columns={columns}
          dataSource={softwarePackages}
          rowKey="id"
          loading={loading}
          rowSelection={
            canDownload
              ? {
                  selectedRowKeys,
                  onChange: (selectedKeys) => {
                    setSelectedRowKeys(selectedKeys);
                  },
                }
              : null
          }
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              translateSoftwarePackage('table.pagination')
                .replace('{{start}}', range[0].toString())
                .replace('{{end}}', range[1].toString())
                .replace('{{total}}', total.toString()),
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
        title={translateSoftwarePackage('modal.editTitle')}
        open={editModalVisible}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="description"
            label={translateSoftwarePackage('modal.description')}
          >
            <TextArea rows={3} placeholder={translateSoftwarePackage('modal.descriptionPlaceholder')} />
          </Form.Item>
        </Form>
      </Modal>

      {/* 上传模态框 */}
      <Modal
        title={uploadType === 'single' ? translateSoftwarePackage('modal.uploadSingleTitle') : translateSoftwarePackage('modal.uploadZipTitle')}
        open={uploadModalVisible}
        onCancel={() => {
          setUploadModalVisible(false);
          form.resetFields();
          setFileList([]);
        }}
        footer={[
          <Button key="cancel" onClick={() => {
            setUploadModalVisible(false);
            form.resetFields();
            setFileList([]);
          }}>
            {translateCommon('cancel')}
          </Button>,
          <Button key="upload" type="primary" onClick={handleUploadWithDescription}>
            {translateCommon('upload')}
          </Button>
        ]}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item label={translateSoftwarePackage('modal.selectFile')}>
            <Dragger
              name="file"
              multiple={false}
              accept={uploadType === 'single' ? '.apk,.ipa' : '.zip'}
              fileList={fileList}
              beforeUpload={(file) => {
                // 文件大小验证
                const maxSingleSize = 500 * 1024 * 1024; // 500MB
                const maxZipSize = 1024 * 1024 * 1024; // 1GB
                const maxSize = uploadType === 'single' ? maxSingleSize : maxZipSize;
                const maxSizeText = uploadType === 'single' ? '500MB' : '1GB';
                
                if (file.size > maxSize) {
                  message.error(`文件大小不能超过${maxSizeText}`);
                  return false;
                }
                
                const newFileList = [file];
                setFileList(newFileList);
                return false; // 阻止自动上传
              }}
              onRemove={() => {
                setFileList([]);
              }}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">
                {translateSoftwarePackage('modal.uploadText')}
              </p>
              <p className="ant-upload-hint">
                {uploadType === 'single' 
                  ? translateSoftwarePackage('modal.uploadHintSingle')
                  : translateSoftwarePackage('modal.uploadHintZip')
                }
              </p>
            </Dragger>
          </Form.Item>

          <Form.Item 
            label={translateSoftwarePackage('modal.description')} 
            name="description"
          >
            <TextArea
              rows={3}
              placeholder={translateSoftwarePackage('modal.descriptionPlaceholder')}
              maxLength={500}
              showCount
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

SoftwarePackageManagement.displayName = 'SoftwarePackageManagement'

export default SoftwarePackageManagement;