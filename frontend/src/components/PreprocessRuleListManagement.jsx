/**
 * 预处理规则列表管理组件
 */

import React, { useState, useEffect } from 'react';
import {
  Table,
  Space,
  Select,
  Input,
  Button,
  Tag,
  Tooltip,
  message,
  Card,
  Row,
  Col,
  Typography
} from 'antd';
import { ReloadOutlined, FileTextOutlined } from '@ant-design/icons';
import { preprocessRuleService } from '../services/preprocessRuleService.js';
import { useI18n } from '../contexts/I18nContext.jsx';
import { useTranslation } from '../hooks/useTranslation.js';
const { Option } = Select;
const { Title, Text } = Typography;

const PreprocessRuleListManagement = ({ refreshTrigger }) => {
  const { language } = useI18n();
  const { t, translateCommon } = useTranslation();
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  
  // 筛选条件
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedBusiness, setSelectedBusiness] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedApp, setSelectedApp] = useState('');
  const [selectedRule, setSelectedRule] = useState('');
  
  // 筛选选项
  const [filterOptions, setFilterOptions] = useState({
    businessTypes: [],
    categories: [],
    appNames: [],
    ruleNames: []
  });

  // 当refreshTrigger变化时，重新加载数据
  useEffect(() => {
    if (refreshTrigger !== undefined && refreshTrigger > 0) {
      loadRules();
      loadFilterOptions();
    }
  }, [refreshTrigger]);

  useEffect(() => {
    loadRules();
    loadFilterOptions();
  }, [currentPage, pageSize, searchKeyword, selectedBusiness, selectedCategory, selectedApp, selectedRule]);

  useEffect(() => {
    if (selectedBusiness) {
      loadCategoriesByBusiness(selectedBusiness);
    } else {
      setFilterOptions(prev => ({ ...prev, categories: [] }));
    }
    setSelectedCategory('');
    setSelectedApp('');
    setSelectedRule('');
  }, [selectedBusiness]);

  useEffect(() => {
    if (selectedBusiness && selectedCategory) {
      loadAppNamesByBusinessAndCategory(selectedBusiness, selectedCategory);
    } else {
      setFilterOptions(prev => ({ ...prev, appNames: [] }));
    }
    setSelectedApp('');
    setSelectedRule('');
  }, [selectedCategory]);

  useEffect(() => {
    if (selectedBusiness && selectedCategory && selectedApp) {
      loadRuleNamesByBusinessAndCategoryAndApp(selectedBusiness, selectedCategory, selectedApp);
    } else {
      setFilterOptions(prev => ({ ...prev, ruleNames: [] }));
    }
    setSelectedRule('');
  }, [selectedApp]);

  const loadRules = async () => {
    try {
      setLoading(true);
      const response = await preprocessRuleService.getPreprocessRules({
        page: currentPage,
        pageSize,
        keyword: searchKeyword,
        businessZh: selectedBusiness,
        category: selectedCategory,
        appName: selectedApp,
        ruleName: selectedRule
      });
      
      // handlePagedApiResponse 已经返回了 result.data，所以直接使用
      if (response && response.data && Array.isArray(response.data)) {
        setRules(response.data);
        setTotal(response.total);
      } else {
        // 如果API返回的数据格式不正确，设置默认值
        setRules([]);
        setTotal(0);
      }
    } catch (error) {
      message.error(translateCommon('error'));
      // 发生错误时设置默认值
      setRules([]);
      setTotal(0);
    } finally {
      setLoading(false);
    }
  };

  const loadFilterOptions = async () => {
    try {
      const response = await preprocessRuleService.getFilterOptions();
      // handleApiResponse 已经返回了 result.data，data 是一个对象，包含 businessTypes, categories, appNames, ruleNames
      if (response && typeof response === 'object' && !Array.isArray(response)) {
        // 确保所有字段都存在，如果不存在则使用空数组
        setFilterOptions({
          businessTypes: Array.isArray(response.businessTypes) ? response.businessTypes : [],
          categories: Array.isArray(response.categories) ? response.categories : [],
          appNames: Array.isArray(response.appNames) ? response.appNames : [],
          ruleNames: Array.isArray(response.ruleNames) ? response.ruleNames : []
        });
      } else {
        // 如果API返回的数据格式不正确，设置默认值
        setFilterOptions({
          businessTypes: [],
          categories: [],
          appNames: [],
          ruleNames: []
        });
      }
    } catch (error) {
      console.error('Failed to load filter options:', error);
      // 发生错误时设置默认值
      setFilterOptions({
        businessTypes: [],
        categories: [],
        appNames: [],
        ruleNames: []
      });
    }
  };

  const loadCategoriesByBusiness = async (businessZh) => {
    try {
      const response = await preprocessRuleService.getCategoriesByBusiness(businessZh);
      // handleApiResponse 已经返回了 result.data，所以直接使用
      if (response && Array.isArray(response)) {
        setFilterOptions(prev => ({ ...prev, categories: response }));
      }
    } catch (error) {
      console.error('Failed to load categories:', error);
    }
  };

  const loadAppNamesByBusinessAndCategory = async (businessZh, category) => {
    try {
      const response = await preprocessRuleService.getAppNamesByBusinessAndCategory(businessZh, category);
      // handleApiResponse 已经返回了 result.data，所以直接使用
      if (response && Array.isArray(response)) {
        setFilterOptions(prev => ({ ...prev, appNames: response }));
      }
    } catch (error) {
      console.error('Failed to load app names:', error);
    }
  };

  const loadRuleNamesByBusinessAndCategoryAndApp = async (businessZh, category, appName) => {
    try {
      const response = await preprocessRuleService.getRuleNamesByBusinessAndCategoryAndApp(businessZh, category, appName);
      // handleApiResponse 已经返回了 result.data，所以直接使用
      if (response && Array.isArray(response)) {
        setFilterOptions(prev => ({ ...prev, ruleNames: response }));
      }
    } catch (error) {
      console.error('Failed to load rule names:', error);
    }
  };

  const handleResetFilters = () => {
    setSearchKeyword('');
    setSelectedBusiness('');
    setSelectedCategory('');
    setSelectedApp('');
    setSelectedRule('');
    setCurrentPage(1);
  };

  const columns = [
    {
      title: t('preprocessRule.rule.name'),
      dataIndex: 'ruleName',
      key: 'ruleName',
      render: (text) => (
        <Tooltip title={text}>
          <span>{text}</span>
        </Tooltip>
      )
    },
    {
      title: t('preprocessRule.rule.business'),
      dataIndex: 'businessZh',
      key: 'business',
      render: (businessZh, record) => {
        const displayText = language === 'en' ? record.businessEn : record.businessZh;
        return <Tag color="blue">{displayText}</Tag>;
      }
    },
    {
      title: t('preprocessRule.rule.category'),
      dataIndex: 'category',
      key: 'category',
      render: (category) => (
        <Tag color="green">{category}</Tag>
      )
    },
    {
      title: t('preprocessRule.rule.appName'),
      dataIndex: 'appName',
      key: 'appName',
      render: (appName) => (
        <Tag color="orange">{appName}</Tag>
      )
    },
    {
      title: t('preprocessRule.rule.isCustom'),
      dataIndex: 'isCustom',
      key: 'isCustom',
      render: (isCustom) => (
        <Tag color={isCustom ? 'purple' : 'default'}>
          {isCustom ? t('preprocessRule.rule.custom') : t('preprocessRule.rule.standard')}
        </Tag>
      )
    },
    {
      title: t('preprocessRule.rule.content'),
      dataIndex: 'content',
      key: 'content',
      render: (content) => (
        <Tooltip title={content}>
          <span style={{ maxWidth: '200px', display: 'inline-block', overflow: 'hidden', textOverflow: 'ellipsis' }}>
            {content}
          </span>
        </Tooltip>
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
            {t('preprocessRule.management.rulesTab')}
          </Title>
          <Text type="secondary" style={{ fontSize: '14px', textAlign: 'left' }}>
            {language === 'en' 
              ? 'View and manage preprocess rules extracted from ZIP packages, support multi-dimensional filtering'
              : '查看和管理从ZIP包中提取的预处理规则，支持多维度筛选'
            }
          </Text>
        </div>
        <Space>
          <Button
            icon={<ReloadOutlined />}
            onClick={() => loadRules()}
          >
            {translateCommon('refresh')}
          </Button>
        </Space>
      </div>

      {/* 多维度筛选器 */}
      <Card style={{ marginBottom: '16px' }}>
        {/* 第一行：业务类型、分类、应用名称、规则名称 */}
        <Row gutter={[16, 16]} style={{ textAlign: 'left' }}>
          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '4px' }}>
              <span style={{ fontSize: '14px', color: '#262626', fontWeight: 500 }}>
                {t('preprocessRule.rule.business')}
              </span>
            </div>
            <Select
              placeholder={language === 'en' ? 'Select business type' : '选择业务类型'}
              allowClear
              style={{ width: '100%' }}
              value={selectedBusiness}
              onChange={(value) => {
                setSelectedBusiness(value || '');
                setCurrentPage(1);
              }}
              size="middle"
            >
              {filterOptions.businessTypes.map((type) => (
                <Option key={type} value={type}>
                  {type}
                </Option>
              ))}
            </Select>
          </Col>

          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '4px' }}>
              <span style={{ fontSize: '14px', color: '#262626', fontWeight: 500 }}>
                {t('preprocessRule.rule.category')}
              </span>
            </div>
            <Select
              placeholder={language === 'en' ? 'Select category' : '选择分类'}
              allowClear
              style={{ width: '100%' }}
              value={selectedCategory}
              onChange={(value) => {
                setSelectedCategory(value || '');
                setCurrentPage(1);
              }}
              disabled={!selectedBusiness}
              size="middle"
            >
              {filterOptions.categories.map((category) => (
                <Option key={category} value={category}>
                  {category}
                </Option>
              ))}
            </Select>
          </Col>

          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '4px' }}>
              <span style={{ fontSize: '14px', color: '#262626', fontWeight: 500 }}>
                {t('preprocessRule.rule.appName')}
              </span>
            </div>
            <Select
              placeholder={language === 'en' ? 'Select app' : '选择应用'}
              allowClear
              style={{ width: '100%' }}
              value={selectedApp}
              onChange={(value) => {
                setSelectedApp(value || '');
                setCurrentPage(1);
              }}
              disabled={!selectedBusiness || !selectedCategory}
              size="middle"
            >
              {filterOptions.appNames.map((app) => (
                <Option key={app} value={app}>
                  {app}
                </Option>
              ))}
            </Select>
          </Col>

          <Col xs={24} sm={12} md={6}>
            <div style={{ marginBottom: '4px' }}>
              <span style={{ fontSize: '14px', color: '#262626', fontWeight: 500 }}>
                {t('preprocessRule.rule.name')}
              </span>
            </div>
            <Select
              placeholder={language === 'en' ? 'Select rule name' : '选择规则名称'}
              allowClear
              style={{ width: '100%' }}
              value={selectedRule}
              onChange={(value) => {
                setSelectedRule(value || '');
                setCurrentPage(1);
              }}
              disabled={!selectedBusiness || !selectedCategory || !selectedApp}
              size="middle"
            >
              {filterOptions.ruleNames.map((rule) => (
                <Option key={rule} value={rule}>
                  {rule}
                </Option>
              ))}
            </Select>
          </Col>
        </Row>

        {/* 第二行：关键词搜索、搜索按钮和重置按钮 */}
        <Row gutter={[16, 16]} style={{ textAlign: 'left', marginTop: '16px' }}>
          <Col xs={24} sm={16} md={18}>
            <Input
              placeholder={t('preprocessRule.rule.searchPlaceholder')}
              value={searchKeyword}
              onChange={(e) => setSearchKeyword(e.target.value)}
              allowClear
              onPressEnter={() => {
                setCurrentPage(1);
                loadRules();
              }}
              size="middle"
              style={{ 
                borderRadius: '8px',
                border: '1px solid #d9d9d9'
              }}
            />
          </Col>
          <Col xs={24} sm={8} md={6}>
            <Space style={{ width: '100%' }}>
              <Button
                type="primary"
                onClick={() => {
                  setCurrentPage(1);
                  loadRules();
                }}
                size="middle"
                style={{ 
                  borderRadius: '8px',
                  flex: 1,
                  boxShadow: '0 2px 4px rgba(24, 144, 255, 0.2)'
                }}
              >
                {translateCommon('search')}
              </Button>
              <Button
                icon={<ReloadOutlined />}
                onClick={handleResetFilters}
                size="middle"
                style={{ 
                  borderRadius: '8px',
                  flex: 1
                }}
              >
                {translateCommon('reset')}
              </Button>
            </Space>
          </Col>
        </Row>
      </Card>

      {/* 表格 */}
      <Table
        columns={columns}
        dataSource={rules}
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
    </div>
  );
};

PreprocessRuleListManagement.displayName = 'PreprocessRuleListManagement'

export default PreprocessRuleListManagement;