import React from 'react'
import ReactDOM from 'react-dom'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import './index.css'
import 'antd/dist/antd.css'

// Suppress dev-overlay ResizeObserver loop errors (common in AntD layouts)
window.addEventListener('error', (e) => {
  const msg = (e as ErrorEvent).message || ''
  if (msg.includes('ResizeObserver loop') || msg.includes('ResizeObserver')) {
    e.stopImmediatePropagation()
  }
})

ReactDOM.render(
  <BrowserRouter>
    <App />
  </BrowserRouter>,
  document.getElementById('root')
)
