import i18n from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { initReactI18next } from 'react-i18next'

// 导入语言资源
import * as en from '../locales/en.json'
import * as zh from '../locales/zh.json'

// 语言资源
const resources = {
  zh: {
    translation: zh
  },
  en: {
    translation: en
  }
}

// 初始化 i18next
i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'zh', // 默认语言
    debug: false, // 生产环境关闭调试
    
    // 语言检测配置
    detection: {
      order: ['localStorage', 'navigator', 'htmlTag'],
      caches: ['localStorage'],
      lookupLocalStorage: 'i18nextLng'
    },

    interpolation: {
      escapeValue: false // React 已经处理了 XSS 防护
    },

    // 命名空间配置
    defaultNS: 'translation',
    ns: ['translation'],

    // 语言切换时的配置
    react: {
      useSuspense: false
    }
  })

export default i18n
