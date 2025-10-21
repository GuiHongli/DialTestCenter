import React from 'react'
import ReactDOM from 'react-dom'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'
import 'antd/dist/antd.css'
import { initErrorHandler } from './utils/errorHandler'

// 初始化错误处理，抑制 ResizeObserver 相关错误
initErrorHandler()

ReactDOM.render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
  document.getElementById('root')
)
