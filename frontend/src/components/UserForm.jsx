import { Form, Input, Modal } from 'antd';
import React, { useEffect } from 'react';
import { useTranslation } from '../hooks/useTranslation.js';

const UserForm = ({
  visible,
  onCancel,
  onSubmit,
  user,
  loading = false,
}) => {
  const { translateUser } = useTranslation();
  const [form] = Form.useForm();

  const isEdit = !!user;
  const [showConfirmPassword, setShowConfirmPassword] = React.useState(false);

  useEffect(() => {
    if (visible) {
      if (user) {
        form.setFieldsValue({
          username: user.username,
        });
        setShowConfirmPassword(false);
      } else {
        form.resetFields();
        setShowConfirmPassword(true);
      }
    }
  }, [visible, user, form]);

  const handlePasswordChange = (e) => {
    const password = e.target.value;
    if (isEdit) {
      if (password && password.length > 0) {
        setShowConfirmPassword(true);
      } else {
        setShowConfirmPassword(false);
        form.setFieldsValue({ confirmPassword: undefined });
      }
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      
      // 编辑模式下，如果密码为空，则不发送密码和确认密码字段
      if (isEdit && (!values.password || values.password.trim() === '')) {
        delete values.password;
        delete values.confirmPassword;
      }
      
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
          <Input.Password 
            placeholder={isEdit ? translateUser('form.passwordPlaceholderEdit') : translateUser('form.passwordPlaceholder')}
            onChange={handlePasswordChange}
          />
        </Form.Item>

        {(!isEdit || showConfirmPassword) && (
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

UserForm.displayName = 'UserForm'

export default UserForm;
