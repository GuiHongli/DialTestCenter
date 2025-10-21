/**
 * 预处理规则管理页面
 */

import React from 'react';
import { Layout } from 'antd';
import PreprocessRuleManagement from '../components/PreprocessRuleManagement.jsx';

const { Content } = Layout;

const PreprocessRuleManagementPage = () => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Content>
        <PreprocessRuleManagement />
      </Content>
    </Layout>
  );
};

export default PreprocessRuleManagementPage;
