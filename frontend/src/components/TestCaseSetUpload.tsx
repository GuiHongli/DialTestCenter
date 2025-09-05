import { InboxOutlined } from '@ant-design/icons'
import type { UploadFile, UploadProps } from 'antd'
import { Button, Form, Input, message, Modal, Upload } from 'antd'
import React, { useState } from 'react'
import testCaseSetService from '../services/testCaseSetService'
import { TestCaseSetUploadData } from '../types/testCaseSet'

const { Dragger } = Upload
const { TextArea } = Input

interface TestCaseSetUploadProps {
  visible: boolean
  onCancel: () => void
  onSuccess: () => void
}

const TestCaseSetUpload: React.FC<TestCaseSetUploadProps> = ({
  visible,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm()
  const [fileList, setFileList] = useState<UploadFile[]>([])
  const [uploading, setUploading] = useState(false)

  const handleUpload = async () => {
    try {
      if (fileList.length === 0) {
        message.error('请选择要上传的文件')
        return
      }

      const file = fileList[0].originFileObj || fileList[0]
      if (!file) {
        message.error('文件无效')
        return
      }

      // 验证文件
      const validation = testCaseSetService.validateTestCaseSetFile(file)
      if (!validation.valid) {
        message.error(validation.message)
        return
      }

      setUploading(true)

      const uploadData: TestCaseSetUploadData = {
        file,
        description: form.getFieldValue('description'),
      }

      const result = await testCaseSetService.uploadTestCaseSet(uploadData)

      if (result.success) {
        message.success('用例集上传成功')
        form.resetFields()
        setFileList([])
        onSuccess()
        onCancel()
      } else {
        message.error(result.message || '上传失败')
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : '上传失败')
    } finally {
      setUploading(false)
    }
  }

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    accept: '.zip',
    fileList,
    beforeUpload: (file) => {
      const validation = testCaseSetService.validateTestCaseSetFile(file)
      if (!validation.valid) {
        message.error(validation.message)
        return false
      }

      // 解析文件名
      const parsed = testCaseSetService.parseFileName(file.name)
      if (parsed) {
        form.setFieldsValue({
          name: parsed.name,
          version: parsed.version,
        })
      }

      setFileList([file])
      return false // 阻止自动上传
    },
    onRemove: () => {
      setFileList([])
      form.resetFields(['name', 'version'])
    },
  }

  const handleCancel = () => {
    form.resetFields()
    setFileList([])
    onCancel()
  }

  return (
    <Modal
      title="上传用例集"
      open={visible}
      onCancel={handleCancel}
      footer={[
        <Button key="cancel" onClick={handleCancel}>
          取消
        </Button>,
        <Button
          key="upload"
          type="primary"
          loading={uploading}
          onClick={handleUpload}
        >
          上传
        </Button>,
      ]}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item label="选择文件">
          <Dragger {...uploadProps}>
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">点击或拖拽文件到此区域上传</p>
            <p className="ant-upload-hint">
              支持ZIP格式，文件大小不超过100MB
              <br />
              文件名格式：用例集名称_版本号.zip
            </p>
          </Dragger>
        </Form.Item>

        <Form.Item
          label="用例集名称"
          name="name"
          rules={[{ required: true, message: '请输入用例集名称' }]}
        >
          <Input placeholder="从文件名自动解析" readOnly />
        </Form.Item>

        <Form.Item
          label="版本号"
          name="version"
          rules={[{ required: true, message: '请输入版本号' }]}
        >
          <Input placeholder="从文件名自动解析" readOnly />
        </Form.Item>

        <Form.Item label="描述" name="description">
          <TextArea
            rows={3}
            placeholder="请输入用例集描述（可选）"
            maxLength={500}
            showCount
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default TestCaseSetUpload
