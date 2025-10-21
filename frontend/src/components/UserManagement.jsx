import {
  Button,
  Card,
  Col,
  Input,
  message,
  Modal,
  Row,
  Space,
  Table,
  Typography,
} from 'antd';
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined, SearchOutlined } from '@ant-design/icons';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from '../hooks/useTranslation.js';
import { usePermission, PagePermission } from '../hooks/usePermission.js';
import { createUser, deleteUser, getUsers, updateUser } from '../services/userService.js';
import { UserRoleService } from '../services/userRoleService.js';
import UserForm from './UserForm';

const { Title, Text } = Typography;

const UserManagement = () => {
  const { translateUser, translateCommon } = useTranslation();
  const { hasPagePermission } = usePermission();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [pagination, setPagination] = useState({
    current: 1,
    pageSize: 10,
    total: 0,
  });

  useEffect(() => {
    // 页面进入时加载用户列表
    loadUsers(0, 10);
  }, []);

  // 加载用户列表
  const loadUsers = async (page = 0, pageSize = 10, username) => {
    try {
      setLoading(true);
      const response = await getUsers(page, pageSize, username);
      setUsers(response.data);
      setPagination({
        current: response.page + 1, // 转换为1开始的页码
        pageSize: response.pageSize,
        total: response.total,
      });
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUser('loadFailed'));
    } finally {
      setLoading(false);
    }
  };

  const handleCreateUser = async (formData) => {
    try {
      setFormLoading(true);
      await createUser(formData);
      setFormVisible(false);
      message.success(translateUser('createSuccess'));
      await loadUsers(pagination.current - 1, pagination.pageSize, searchText);
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUser('createFailed'));
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdateUser = async (formData) => {
    if (!editingUser) return;

    try {
      setFormLoading(true);
      await updateUser(editingUser.id, formData);
      setEditingUser(null);
      setFormVisible(false);
      message.success(translateUser('updateSuccess'));
      await loadUsers(pagination.current - 1, pagination.pageSize, searchText);
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUser('updateFailed'));
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeleteUser = async (id) => {
    try {
      await deleteUser(id);
      message.success(translateUser('deleteSuccess'));
      await loadUsers(pagination.current - 1, pagination.pageSize, searchText);
    } catch (err) {
      message.error(err instanceof Error ? err.message : translateUser('deleteFailed'));
    }
  };

  const handleEdit = (record) => {
    setEditingUser(record);
    setFormVisible(true);
  };

  const handleAdd = () => {
    setEditingUser(null);
    setFormVisible(true);
  };

  const handleModalCancel = () => {
    setFormVisible(false);
    setEditingUser(null);
  };

  // 处理搜索
  const handleSearch = () => {
    loadUsers(0, pagination.pageSize, searchText);
  };

  // 处理分页变化
  const handleTableChange = (pagination) => {
    loadUsers(pagination.current - 1, pagination.pageSize, searchText);
  };

  // 表格列配置
  const columns = useMemo(() => [
    {
      title: translateUser('table.id'),
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a, b) => a.id - b.id,
    },
    {
      title: translateUser('table.username'),
      dataIndex: 'username',
      key: 'username',
      width: 150,
      sorter: (a, b) => a.username.localeCompare(b.username),
    },
    {
      title: translateUser('table.lastLoginTime'),
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      width: 200,
      render: (text) => text ? new Date(text).toLocaleString('zh-CN') : '-',
    },
    {
      title: translateUser('table.actions'),
      key: 'action',
      width: 120,
      render: (_, record) => (
        <Space size="small">
          <PagePermission pageId="user-management" operation="edit">
            <Button
              type="text"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            />
          </PagePermission>
          <PagePermission pageId="user-management" operation="delete">
            <Button
              type="text"
              danger
              icon={<DeleteOutlined />}
              onClick={() => handleDeleteUser(record.id)}
            />
          </PagePermission>
        </Space>
      ),
    },
  ], [translateUser, translateCommon]);

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
            {translateUser('title')}
          </Title>
          <Text type="secondary">{translateUser('description')}</Text>
        </div>
        <Space>
          <PagePermission pageId="user-management" operation="create">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleAdd}
            >
              {translateUser('addUser')}
            </Button>
          </PagePermission>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => loadUsers(pagination.current - 1, pagination.pageSize, searchText)}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 搜索和过滤 */}
      <Card style={{ marginBottom: '16px' }}>
        <Row gutter={[16, 16]} align="middle">
          <Col xs={24} sm={8} md={6}>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '8px',
              height: '40px'
            }}>
              <SearchOutlined style={{ color: '#1890ff', fontSize: '16px' }} />
              <span style={{ 
                fontSize: '14px', 
                color: '#262626',
                fontWeight: 500,
                whiteSpace: 'nowrap'
              }}>
                {translateUser('filters.searchUsername')}:
              </span>
            </div>
          </Col>
          <Col xs={24} sm={16} md={14}>
            <Input
              placeholder={translateUser('form.usernamePlaceholder')}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              allowClear
              onPressEnter={handleSearch}
              size="middle"
              style={{ 
                borderRadius: '8px',
                boxShadow: '0 2px 4px rgba(0,0,0,0.02)'
              }}
              prefix={<SearchOutlined style={{ color: '#bfbfbf' }} />}
            />
          </Col>
          <Col xs={24} sm={24} md={4}>
            <Button
              type="primary"
              onClick={handleSearch}
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

      {/* 用户表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={{
            ...pagination,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) => 
              translateUser('table.pagination', { start: range[0], end: range[1], total }),
          }}
          onChange={handleTableChange}
          scroll={{ x: 800 }}
          size="middle"
        />
      </Card>

      {/* 新增/编辑表单对话框 */}
      <UserForm
        visible={formVisible}
        user={editingUser}
        onSubmit={editingUser ? handleUpdateUser : handleCreateUser}
        onCancel={handleModalCancel}
        loading={formLoading}
      />
    </div>
  );
};

export default UserManagement;
