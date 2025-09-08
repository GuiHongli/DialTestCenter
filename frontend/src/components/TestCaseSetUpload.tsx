import { InboxOutlined } from '@ant-design/icons'
import type { UploadFile, UploadProps } from 'antd'
import { Button, Form, Input, message, Modal, Select, Upload } from 'antd'
import React, { useState } from 'react'
import { useTranslation } from '../hooks/useTranslation'
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
  const { translateTestCaseSet, translateCommon } = useTranslation()

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
      const validation = testCaseSetService.validateTestCaseSetFile(file as File)
      if (!validation.valid) {
        message.error(validation.message)
        return
      }

      setUploading(true)

      const uploadData: TestCaseSetUploadData = {
        file: file as File,
        description: form.getFieldValue('description'),
        business: form.getFieldValue('business'),
      }

      const result = await testCaseSetService.uploadTestCaseSet(uploadData)

      if (result.success) {
        message.success(translateTestCaseSet('upload.uploadSuccess'))
        form.resetFields()
        setFileList([])
        onSuccess()
        onCancel()
      } else {
        message.error(result.message || translateTestCaseSet('upload.uploadFailed'))
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : translateTestCaseSet('upload.uploadFailed'))
    } finally {
      setUploading(false)
    }
  }

  const uploadProps: UploadProps = {
    name: 'file',
    multiple: false,
    accept: '.zip,.tar.gz',
    fileList,
    beforeUpload: (file) => {
      const validation = testCaseSetService.validateTestCaseSetFile(file as File)
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
      title={translateTestCaseSet('upload.title')}
      open={visible}
      onCancel={handleCancel}
      footer={[
        <Button key="cancel" onClick={handleCancel}>
          {translateCommon('cancel')}
        </Button>,
        <Button
          key="upload"
          type="primary"
          loading={uploading}
          onClick={handleUpload}
        >
          {translateCommon('upload')}
        </Button>,
      ]}
      width={600}
    >
      <Form form={form} layout="vertical">
        <Form.Item label={translateTestCaseSet('upload.selectFile')}>
          <Dragger {...uploadProps}>
            <p className="ant-upload-drag-icon">
              <InboxOutlined />
            </p>
            <p className="ant-upload-text">{translateTestCaseSet('upload.dragTip')}</p>
            <p className="ant-upload-hint">
              {translateTestCaseSet('upload.supportFormat')}
              <br />
              {translateTestCaseSet('upload.fileSizeLimit')}
            </p>
          </Dragger>
        </Form.Item>

        <Form.Item
          label={translateTestCaseSet('table.name')}
          name="name"
          rules={[{ required: true, message: '请输入用例集名称' }]}
        >
          <Input placeholder="从文件名自动解析" readOnly />
        </Form.Item>

        <Form.Item
          label={translateCommon('version')}
          name="version"
          rules={[{ required: true, message: '请输入版本号' }]}
        >
          <Input placeholder="从文件名自动解析" readOnly />
        </Form.Item>

        <Form.Item 
          label="业务类型" 
          name="business" 
          initialValue="VPN阻断业务"
          rules={[{ required: true, message: '请选择业务类型' }]}
        >
          <Select placeholder="请选择业务类型">
            <Select.Option value="VPN阻断业务">VPN阻断业务</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item label={translateCommon('description')} name="description">
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
