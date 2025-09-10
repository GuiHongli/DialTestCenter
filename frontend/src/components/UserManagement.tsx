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
import { DeleteOutlined, EditOutlined, PlusOutlined, ReloadOutlined } from '@ant-design/icons';
import React, { useCallback, useEffect, useState } from 'react';
import { useTranslation } from '../hooks/useTranslation';
import { createUser, deleteUser, getAllUsers, updateUser } from '../services/userService';
import { User, UserCreateParams, UserUpdateParams } from '../types/user';
import UserForm from './UserForm';

const { Title, Text } = Typography;

const UserManagement: React.FC = () => {
  const { translateUser, translateCommon } = useTranslation();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [formVisible, setFormVisible] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formLoading, setFormLoading] = useState(false);
  const [searchText, setSearchText] = useState('');

  // 加载用户列表
  const loadUsers = useCallback(async () => {
    try {
      setLoading(true);
      const userList = await getAllUsers();
      setUsers(userList);
    } catch (error) {
      console.error('Failed to load users:', error);
      message.error(translateUser('loadFailed'));
    } finally {
      setLoading(false);
    }
  }, []);

  // 初始加载
  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  // 处理新增用户
  const handleAddUser = () => {
    setEditingUser(null);
    setFormVisible(true);
  };

  // 处理编辑用户
  const handleEditUser = (user: User) => {
    setEditingUser(user);
    setFormVisible(true);
  };

  // 处理删除用户
  const handleDeleteUser = (user: User) => {
    Modal.confirm({
      title: translateUser('confirmDelete'),
      content: translateUser('deleteDescription'),
      okText: translateCommon('confirm'),
      cancelText: translateCommon('cancel'),
      onOk: async () => {
        try {
          await deleteUser(user.id);
          message.success(translateUser('deleteSuccess'));
          loadUsers();
        } catch (error) {
          console.error('Failed to delete user:', error);
          message.error(translateUser('deleteFailed'));
        }
      },
    });
  };

  // 处理表单提交
  const handleFormSubmit = async (values: UserCreateParams | UserUpdateParams) => {
    try {
      setFormLoading(true);
      if (editingUser) {
        await updateUser(editingUser.id, values as UserUpdateParams);
        message.success(translateUser('updateSuccess'));
      } else {
        await createUser(values as UserCreateParams);
        message.success(translateUser('createSuccess'));
      }
      setFormVisible(false);
      loadUsers();
    } catch (error) {
      console.error('Failed to submit form:', error);
      message.error(editingUser ? translateUser('updateFailed') : translateUser('createFailed'));
    } finally {
      setFormLoading(false);
    }
  };

  // 处理表单取消
  const handleFormCancel = () => {
    setFormVisible(false);
    setEditingUser(null);
  };

  // 过滤用户列表
  const filteredUsers = users.filter(user =>
    user.username.toLowerCase().includes(searchText.toLowerCase())
  );

  // 表格列定义
  const columns = [
    {
      title: translateUser('table.id'),
      dataIndex: 'id',
      key: 'id',
      width: 80,
      sorter: (a: User, b: User) => a.id - b.id,
    },
    {
      title: translateUser('table.username'),
      dataIndex: 'username',
      key: 'username',
      sorter: (a: User, b: User) => a.username.localeCompare(b.username),
    },
    {
      title: translateUser('table.lastLoginTime'),
      dataIndex: 'lastLoginTime',
      key: 'lastLoginTime',
      render: (time: string) => time ? new Date(time).toLocaleString() : '-',
      sorter: (a: User, b: User) => {
        if (!a.lastLoginTime && !b.lastLoginTime) return 0;
        if (!a.lastLoginTime) return 1;
        if (!b.lastLoginTime) return -1;
        return new Date(a.lastLoginTime).getTime() - new Date(b.lastLoginTime).getTime();
      },
    },
    {
      title: translateUser('table.createdTime'),
      dataIndex: 'createdTime',
      key: 'createdTime',
      render: (time: string) => new Date(time).toLocaleString(),
      sorter: (a: User, b: User) => new Date(a.createdTime).getTime() - new Date(b.createdTime).getTime(),
    },
    {
      title: translateUser('table.actions'),
      key: 'actions',
      width: 120,
      render: (_: any, record: User) => (
        <Space>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => handleEditUser(record)}
            size="small"
          >
            {translateUser('table.edit')}
          </Button>
          <Button
            type="text"
            danger
            icon={<DeleteOutlined />}
            onClick={() => handleDeleteUser(record)}
            size="small"
          >
            {translateUser('table.delete')}
          </Button>
        </Space>
      ),
    },
  ];

  return (
    <div style={{ textAlign: 'left' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px' }}>
        <div>
          <Title level={2} style={{ textAlign: 'left', margin: 0 }}>
            {translateUser('title')}
          </Title>
          <Text type="secondary" style={{ textAlign: 'left' }}>
            {translateUser('description')}
          </Text>
        </div>
        <Space>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            onClick={handleAddUser}
          >
            {translateUser('addUser')}
          </Button>
          <Button
            icon={<ReloadOutlined />}
            onClick={loadUsers}
            loading={loading}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>


      {/* 搜索和过滤 */}
      <Card style={{ marginBottom: '16px' }}>
        <Row gutter={16}>
          <Col span={8}>
            <Space.Compact style={{ width: '100%' }}>
              <span style={{ 
                padding: '4px 8px', 
                backgroundColor: '#f5f5f5', 
                border: '1px solid #d9d9d9',
                borderRight: 'none',
                borderRadius: '6px 0 0 6px',
                fontSize: '14px',
                color: '#666',
                display: 'flex',
                alignItems: 'center',
                minWidth: '200px',
                justifyContent: 'center'
              }}>
                {translateUser('table.username')}
              </span>
              <Input
                placeholder={translateUser('form.usernamePlaceholder')}
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                allowClear
                style={{ borderRadius: '0 6px 6px 0' }}
              />
            </Space.Compact>
          </Col>
        </Row>
      </Card>

      {/* 用户表格 */}
      <Card>
        <Table
          columns={columns}
          dataSource={filteredUsers}
          rowKey="id"
          loading={loading}
          pagination={{
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total, range) =>
              translateUser('table.pagination')
                .replace('{{start}}', range[0].toString())
                .replace('{{end}}', range[1].toString())
                .replace('{{total}}', total.toString()),
          }}
          scroll={{ x: 800 }}
          size="middle"
        />
      </Card>

      {/* 用户表单 */}
      <UserForm
        visible={formVisible}
        onCancel={handleFormCancel}
        onSubmit={handleFormSubmit}
        user={editingUser}
        loading={formLoading}
      />
    </div>
  );
};

export default UserManagement;
