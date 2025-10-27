import { InboxOutlined } from '@ant-design/icons'
import { Button, Form, Input, message, Modal, Select, Upload } from 'antd'
import React, { useState } from 'react'
import { useTranslation } from '../hooks/useTranslation.js'
import { useI18n } from '../contexts/I18nContext.jsx'
import testCaseSetService from '../services/testCaseSetService.js'

const { Dragger } = Upload
const { TextArea } = Input

const TestCaseSetUpload = ({
  visible,
  onCancel,
  onSuccess,
}) => {
  const [form] = Form.useForm()
  const [fileList, setFileList] = useState([])
  const [uploading, setUploading] = useState(false)
  const { translateTestCaseSet, translateCommon } = useTranslation()
  const { language } = useI18n()

  const handleUpload = async () => {
    try {
      // 验证表单
      await form.validateFields()
      
      if (fileList.length === 0) {
        message.error(translateTestCaseSet('upload.selectFile'))
        return
      }

      const file = fileList[0].originFileObj || fileList[0]
      if (!file) {
        message.error(translateTestCaseSet('upload.uploadFailed'))
        return
      }

      // 验证文件
      const validation = testCaseSetService.validateTestCaseSetFile(file)
      if (!validation.valid) {
        message.error(validation.message)
        return
      }

      setUploading(true)

      const uploadData = {
        file: file,
        description: form.getFieldValue('description'),
        businessZh: form.getFieldValue('businessZh'),
        businessEn: form.getFieldValue('businessEn'),
      }

      try {
        const result = await testCaseSetService.uploadTestCaseSet(uploadData)

        if (result.success) {
          message.success(translateTestCaseSet('upload.uploadSuccess'))
          form.resetFields()
          setFileList([])
          onSuccess()
          onCancel()
        } else {
          // 检查是否是重复上传错误
          if (result.message && (result.message.includes('用例集名称和版本已存在') || result.message.includes('已存在'))) {
            // 提取用例集名称
            let caseSetName = '该用例集'
            if (result.message.includes(':')) {
              const parts = result.message.split(': ')
              if (parts.length > 1) {
                caseSetName = parts[parts.length - 1]
              }
            }
            
            // 显示覆盖确认对话框
            Modal.confirm({
              title: '用例集已存在',
              content: `用例集 "${caseSetName}" 已存在，是否要覆盖更新？`,
              okText: '覆盖更新',
              cancelText: '取消',
              onOk: async () => {
                try {
                  const overwriteResult = await testCaseSetService.uploadTestCaseSetWithOverwrite(uploadData)
                  if (overwriteResult.success) {
                    message.success('覆盖更新成功')
                    form.resetFields()
                    setFileList([])
                    onSuccess()
                    onCancel()
                  } else {
                    message.error(overwriteResult.message || '覆盖更新失败')
                  }
                } catch (overwriteError) {
                  message.error('覆盖更新失败')
                }
              }
            })
          } else {
            message.error(result.message || translateTestCaseSet('upload.uploadFailed'))
          }
        }
      } catch (uploadError) {
        message.error(uploadError.message || translateTestCaseSet('upload.uploadFailed'))
      }
    } catch (error) {
      message.error(error instanceof Error ? error.message : translateTestCaseSet('upload.uploadFailed'))
    } finally {
      setUploading(false)
    }
  }

  const uploadProps = {
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
          rules={[{ required: true, message: translateTestCaseSet('upload.validation.nameRequired') }]}
        >
          <Input placeholder="从文件名自动解析" readOnly />
        </Form.Item>

        <Form.Item
          label={translateCommon('version')}
          name="version"
          rules={[{ required: true, message: translateTestCaseSet('upload.validation.versionRequired') }]}
        >
          <Input placeholder="从文件名自动解析" readOnly />
        </Form.Item>

        <Form.Item 
          label={translateTestCaseSet('upload.businessType')} 
          name="businessZh" 
          initialValue="VPN阻断业务"
          rules={[{ required: true, message: translateTestCaseSet('upload.businessTypeRequired') }]}
        >
          <Select placeholder={translateTestCaseSet('upload.businessTypePlaceholder')}>
            <Select.Option value="VPN阻断业务">
              {language === 'en' ? translateTestCaseSet('upload.businessTypes.VPN阻断业务') : 'VPN阻断业务'}
            </Select.Option>
          </Select>
        </Form.Item>

        <Form.Item 
          label={translateCommon('description')} 
          name="description"
        >
          <TextArea
            rows={3}
            placeholder={translateTestCaseSet('upload.validation.descriptionPlaceholder')}
            maxLength={500}
            showCount
          />
        </Form.Item>
      </Form>
    </Modal>
  )
}

TestCaseSetUpload.displayName = 'TestCaseSetUpload'

export default TestCaseSetUpload