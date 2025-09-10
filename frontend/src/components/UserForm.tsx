import { Button, Form, Input, Modal } from 'antd';
import React, { useEffect } from 'react';
import { useTranslation } from '../hooks/useTranslation';
import { User, UserCreateParams, UserUpdateParams } from '../types/user';

interface UserFormProps {
  visible: boolean;
  onCancel: () => void;
  onSubmit: (values: UserCreateParams | UserUpdateParams) => Promise<void>;
  user?: User | null;
  loading?: boolean;
}

const UserForm: React.FC<UserFormProps> = ({
  visible,
  onCancel,
  onSubmit,
  user,
  loading = false,
}) => {
  const { translateUser } = useTranslation();
  const [form] = Form.useForm();

  const isEdit = !!user;

  useEffect(() => {
    if (visible) {
      if (user) {
        form.setFieldsValue({
          username: user.username,
        });
      } else {
        form.resetFields();
      }
    }
  }, [visible, user, form]);

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      await onSubmit(values);
      form.resetFields();
    } catch (error) {
      console.error('Form validation failed:', error);
    }
  };

  const handleCancel = () => {
    form.resetFields();
    onCancel();
  };

  return (
    <Modal
      title={isEdit ? translateUser('editUser') : translateUser('addUser')}
      open={visible}
      onCancel={handleCancel}
      onOk={handleSubmit}
      confirmLoading={loading}
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        preserve={false}
      >
        <Form.Item
          name="username"
          label={translateUser('form.username')}
          rules={[
            { required: true, message: translateUser('form.usernamePlaceholder') },
            { min: 3, max: 50, message: '用户名长度必须在3-50个字符之间' },
            { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' },
          ]}
        >
          <Input placeholder={translateUser('form.usernamePlaceholder')} />
        </Form.Item>

        <Form.Item
          name="password"
          label={translateUser('form.password')}
          rules={[
            { required: !isEdit, message: translateUser('form.passwordPlaceholder') },
            { min: 6, max: 50, message: '密码长度必须在6-50个字符之间' },
          ]}
        >
          <Input.Password placeholder={translateUser('form.passwordPlaceholder')} />
        </Form.Item>

        {!isEdit && (
          <Form.Item
            name="confirmPassword"
            label={translateUser('form.confirmPassword')}
            dependencies={['password']}
            rules={[
              { required: true, message: translateUser('form.confirmPasswordPlaceholder') },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('password') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error(translateUser('form.passwordMismatch')));
                },
              }),
            ]}
          >
            <Input.Password placeholder={translateUser('form.confirmPasswordPlaceholder')} />
          </Form.Item>
        )}
      </Form>
    </Modal>
  );
};

export default UserForm;
