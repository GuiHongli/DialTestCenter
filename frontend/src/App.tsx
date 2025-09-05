import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { Route, Routes } from 'react-router-dom'
import './App.css'
import Layout from './components/Layout'
import Home from './pages/Home'
import UserRoleManagementPage from './pages/UserRoleManagement'

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/dashboard" element={<Home />} />
          <Route path="/user-roles" element={<UserRoleManagementPage />} />
        </Routes>
      </Layout>
    </ConfigProvider>
  )
}

export default App
