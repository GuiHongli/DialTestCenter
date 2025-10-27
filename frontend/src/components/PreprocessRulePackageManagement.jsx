/**
 * 预处理规则ZIP包管理组件
 */

import React, { useState, useEffect } from 'react';
import {
  Table,
  Button,
  Space,
  Modal,
  Form,
  Input,
  Select,
  Upload,
  message,
  Popconfirm,
  Tag,
  Tooltip,
  Card,
  Row,
  Col,
  Typography
} from 'antd';
import {
  UploadOutlined,
  DownloadOutlined,
  DeleteOutlined,
  ReloadOutlined,
  FileTextOutlined,
  InboxOutlined
} from '@ant-design/icons';
import { preprocessRuleService } from '../services/preprocessRuleService.js';
import { useI18n } from '../contexts/I18nContext.jsx';
import { useTranslation } from '../hooks/useTranslation.js';
const { Title, Text } = Typography;
const { Dragger } = Upload;
const { TextArea } = Input;

const PreprocessRulePackageManagement = () => {
  const { language } = useI18n();
  const { t, translateCommon } = useTranslation();
  const [packages, setPackages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [searchKeyword, setSearchKeyword] = useState('');
  
  const [uploadModalVisible, setUploadModalVisible] = useState(false);
  const [uploadForm] = Form.useForm();
  const [uploading, setUploading] = useState(false);
  const [fileList, setFileList] = useState([]);

  useEffect(() => {
    loadPackages();
  }, [currentPage, pageSize, searchKeyword]);

  const loadPackages = async () => {
    try {
      setLoading(true);
      const response = await preprocessRuleService.getPreprocessRulePackages({
        page: currentPage,
        pageSize,
        keyword: searchKeyword
      });
      
      // handlePagedApiResponse 已经返回了 result.data，所以直接使用
      if (response && response.data && Array.isArray(response.data)) {
        setPackages(response.data);
        setTotal(response.total);
      } else {
        // 如果API返回的数据格式不正确，设置默认值
        setPackages([]);
        setTotal(0);
      }
    } catch (error) {
      message.error(translateCommon('error'));
      // 发生错误时设置默认值
      setPackages([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (forceOverwrite = false) => {
    try {
      // 检查文件是否已选择
      if (fileList.length === 0) {
        message.error(t('preprocessRule.package.fileRequired'));
        return;
      }

      // 验证表单
      const values = await uploadForm.validateFields();

      // 获取文件对象（兼容两种情况：UploadFile 或 File）
      const file = fileList[0].originFileObj || fileList[0];
      if (!file) {
        message.error(language === 'en' ? 'File object is invalid' : '文件对象无效');
        return;
      }

      setUploading(true);
      const formData = new FormData();
      // 直接使用文件对象
      formData.append('file', file);
      formData.append('businessZh', values.businessZh);
      // businessEn 始终使用固定的英文值
      formData.append('businessEn', 'VPN_BLOCKING');
      if (values.description) {
        formData.append('description', values.description);
      }
      // 添加覆盖标志
      if (forceOverwrite) {
        formData.append('forceOverwrite', 'true');
      }
      
      // 调试日志
      console.log('FormData contents:', {
        file: file.name,
        businessZh: values.businessZh,
        businessEn: 'VPN_BLOCKING',
        description: values.description,
        forceOverwrite
      });

      const response = await preprocessRuleService.uploadPreprocessRulePackage(formData);
      
      console.log('Upload response:', response);
      console.log('Response message:', response.message);
      console.log('Force overwrite:', forceOverwrite);
      
      if (response.success) {
        message.success(t('preprocessRule.package.uploadSuccess'));
        setUploadModalVisible(false);
        uploadForm.resetFields();
        setFileList([]);
        loadPackages();
      } else {
        // 检查是否是重复错误（ZIP包重复或规则重复）
        const isDuplicateZip = response.message && response.message.includes('已存在同名ZIP包');
        const isDuplicateRule = response.message && response.message.includes('已存在的预处理规则');
        
        if ((isDuplicateZip || isDuplicateRule) && !forceOverwrite) {
          // 不要在finally中设置uploading=false，因为用户可能会选择覆盖
          setUploading(false);
          
          // 根据不同的重复类型显示不同的提示信息
          let title, content;
          if (isDuplicateZip) {
            title = language === 'en' ? 'Duplicate ZIP Package' : '重复的ZIP包';
            content = language === 'en' 
              ? 'A ZIP package with the same name already exists for this business type. Do you want to overwrite it? This will also overwrite any rules with the same name.'
              : '该业务类型下已存在同名ZIP包。是否覆盖？这也会覆盖同名的预处理规则。';
          } else {
            title = language === 'en' ? 'Duplicate Rules' : '重复的预处理规则';
            content = language === 'en' 
              ? 'The ZIP package contains rules that already exist for this business type. Do you want to overwrite them?'
              : 'ZIP包中包含已存在的预处理规则。是否覆盖这些规则？';
          }
          
          Modal.confirm({
            title,
            content,
            okText: language === 'en' ? 'Overwrite' : '覆盖',
            cancelText: language === 'en' ? 'Cancel' : '取消',
            onOk: () => {
              // 用户确认覆盖，重新调用上传函数，传入覆盖标志
              handleUpload(true);
            }
          });
          return; // 提前返回，不执行finally中的setUploading(false)
        } else {
          message.error(response.message || t('preprocessRule.package.uploadFailed'));
        }
      }
    } catch (error) {
      message.error(t('preprocessRule.package.uploadFailed'));
    } finally {
      setUploading(false);
    }
  };

  const handleDownload = async (packageId, packageName) => {
    try {
      const response = await preprocessRuleService.downloadPreprocessRulePackage(packageId);
      
      const url = window.URL.createObjectURL(new Blob([response]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', packageName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      
      message.success(t('preprocessRule.package.downloadSuccess'));
    } catch (error) {
      message.error(t('preprocessRule.package.downloadFailed'));
    }
  };

  const handleDelete = async (packageId) => {
    try {
      const response = await preprocessRuleService.deletePreprocessRulePackage(packageId);
      
      if (response.success) {
        message.success(t('preprocessRule.package.deleteSuccess'));
        loadPackages();
      } else {
        message.error(response.message || t('preprocessRule.package.deleteFailed'));
      }
    } catch (error) {
      message.error(t('preprocessRule.package.deleteFailed'));
    }
  };

  const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const columns = [
    {
      title: t('preprocessRule.package.name'),
      dataIndex: 'packageName',
      key: 'packageName',
      render: (text) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      )
    },
    {
      title: t('preprocessRule.package.business'),
      dataIndex: 'businessZh',
      key: 'business',
      render: (businessZh, record) => {
        const displayText = language === 'en' ? record.businessEn : record.businessZh;
        return <Tag color="blue">{displayText}</Tag>;
      }
    },
    {
      title: t('preprocessRule.package.fileSize'),
      dataIndex: 'fileSize',
      key: 'fileSize',
      render: (size) => formatFileSize(size)
    },
    {
      title: t('preprocessRule.package.description'),
      dataIndex: 'description',
      key: 'description',
      render: (text) => (
        <Tooltip title={text}>
          <span>{text || '-'}</span>
        </Tooltip>
      )
    },
    {
      title: translateCommon('actions'),
      key: 'actions',
      render: (record) => (
        <Space>
          <Button
            type="link"
            icon={<DownloadOutlined />}
            onClick={() => handleDownload(record.id, record.packageName)}
          >
            {translateCommon('download')}
          </Button>
          <Popconfirm
            title={t('preprocessRule.package.deleteConfirm')}
            onConfirm={() => handleDelete(record.id)}
            okText={translateCommon('confirm')}
            cancelText={translateCommon('cancel')}
          >
            <Button
              type="link"
              danger
              icon={<DeleteOutlined />}
            >
              {translateCommon('delete')}
            </Button>
          </Popconfirm>
        </Space>
      )
    }
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
            <FileTextOutlined style={{ marginRight: '8px' }} />
            {t('preprocessRule.management.packagesTab')}
          </Title>
          <Text type="secondary" style={{ fontSize: '14px', textAlign: 'left' }}>
            {language === 'en' 
              ? 'Manage preprocess rule ZIP package files, support upload, download and delete operations'
              : '管理预处理规则ZIP包文件，支持上传、下载和删除操作'
            }
          </Text>
        </div>
        <Space>
          <Button
            type="primary"
            icon={<UploadOutlined />}
            onClick={() => setUploadModalVisible(true)}
          >
            {t('preprocessRule.package.upload')}
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => loadPackages()}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 搜索筛选器 */}
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
                {t('preprocessRule.package.name')}:
              </span>
            </div>
          </Col>
          <Col xs={24} sm={16} md={14}>
            <Input
              placeholder={t('preprocessRule.package.searchPlaceholder')}
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              allowClear
              onPressEnter={() => {
                setCurrentPage(1);
                loadPackages();
              }}
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
                onClick={() => {
                  setCurrentPage(1);
                  loadPackages();
                }}
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

      {/* 表格 */}
      <Table
        columns={columns}
        dataSource={packages}
        loading={loading}
        rowKey="id"
        pagination={{
          current: currentPage,
          pageSize: pageSize,
          total: total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total, range) =>
            language === 'en' 
              ? `Showing ${range[0]}-${range[1]} of ${total} items`
              : `共 ${total} 条记录，显示第 ${range[0]}-${range[1]} 条`,
          onChange: (page, size) => {
            setCurrentPage(page);
            setPageSize(size || 10);
          },
          pageSizeOptions: ['10', '20', '50', '100']
        }}
      />

      {/* 上传模态框 */}
      <Modal
        title={language === 'en' ? 'Upload Preprocess Rule ZIP Package' : '上传预处理规则ZIP包'}
        open={uploadModalVisible}
        onCancel={() => {
          setUploadModalVisible(false);
          uploadForm.resetFields();
          setFileList([]);
        }}
        footer={[
          <Button key="cancel" onClick={() => {
            setUploadModalVisible(false);
            uploadForm.resetFields();
            setFileList([]);
          }}>
            {translateCommon('cancel')}
          </Button>,
          <Button
            key="upload"
            type="primary"
            loading={uploading}
            onClick={() => handleUpload(false)}
          >
            {translateCommon('upload')}
          </Button>,
        ]}
        width={600}
      >
        <Form
          form={uploadForm}
          layout="vertical"
        >
          <Form.Item
            name="file"
            label={t('preprocessRule.package.selectFile')}
            rules={[
              { required: true, message: t('preprocessRule.package.fileRequired') }
            ]}
          >
            <Dragger
              name="file"
              multiple={false}
              accept=".zip"
              fileList={fileList}
              beforeUpload={(file) => {
                setFileList([file]);
                return false; // 阻止自动上传
              }}
              onRemove={() => {
                setFileList([]);
              }}
              style={{ padding: '20px' }}
            >
              <p className="ant-upload-drag-icon">
                <InboxOutlined />
              </p>
              <p className="ant-upload-text">
                {language === 'en' 
                  ? 'Click or drag ZIP files to this area to upload'
                  : '点击或拖拽ZIP文件到此区域上传'
                }
              </p>
              <p className="ant-upload-hint">
                {language === 'en' 
                  ? 'Support ZIP format files\nFile size should not exceed 100MB'
                  : '支持ZIP格式文件\n文件大小不超过100MB'
                }
              </p>
            </Dragger>
          </Form.Item>

          <Form.Item
            name="businessZh"
            label={t('preprocessRule.package.business')}
            initialValue="VPN阻断业务"
            rules={[
              { required: true, message: t('preprocessRule.package.businessRequired') }
            ]}
          >
            <Select placeholder={language === 'en' ? 'Please select business type' : '请选择业务类型'}>
              <Select.Option value="VPN阻断业务">
                {language === 'en' ? t('preprocessRule.package.businessTypes.VPN阻断业务') : 'VPN阻断业务'}
              </Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="description"
            label={t('preprocessRule.package.description')}
          >
            <TextArea
              rows={3}
              placeholder={t('preprocessRule.package.descriptionPlaceholder')}
              maxLength={500}
              showCount
            />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default PreprocessRulePackageManagement;