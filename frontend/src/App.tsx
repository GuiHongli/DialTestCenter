import { ConfigProvider } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import { Route, Routes } from 'react-router-dom'
import './App.css'
import Layout from './components/Layout'
import Home from './pages/Home'
import UserManagement from './pages/UserManagement'

function App() {
  return (
    <ConfigProvider locale={zhCN}>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/users" element={<UserManagement />} />
        </Routes>
      </Layout>
    </ConfigProvider>
  )
}

export default App
