/**
 * 软件包管理页面
 */

import React from 'react';
import { Layout } from 'antd';
import SoftwarePackageManagement from '../components/SoftwarePackageManagement';

const { Content } = Layout;

const SoftwarePackageManagementPage: React.FC = () => {
  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Content>
        <SoftwarePackageManagement />
      </Content>
    </Layout>
  );
};

export default SoftwarePackageManagementPage;
