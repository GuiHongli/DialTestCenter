import { ConfigProvider } from 'antd'
import enUS from 'antd/locale/en_US'
import zhCN from 'antd/locale/zh_CN'
import React, { createContext, ReactNode, useContext, useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'

// 支持的语言类型
export type SupportedLanguage = 'zh' | 'en'

// Context 类型定义
interface I18nContextType {
  language: SupportedLanguage
  setLanguage: (lang: SupportedLanguage) => void
  t: (key: string, options?: any) => string
  isReady: boolean
}

// 创建 Context
const I18nContext = createContext<I18nContextType | undefined>(undefined)

// Provider Props 类型
interface I18nProviderProps {
  children: ReactNode
}

// Ant Design 语言包映射
const antdLocales = {
  zh: zhCN,
  en: enUS
}

// I18n Provider 组件
export const I18nProvider: React.FC<I18nProviderProps> = ({ children }) => {
  const { i18n, t, ready } = useTranslation()
  const [language, setLanguageState] = useState<SupportedLanguage>('zh')
  const [isReady, setIsReady] = useState(false)

  // 初始化语言设置
  useEffect(() => {
    if (ready) {
      const currentLang = i18n.language as SupportedLanguage
      setLanguageState(currentLang)
      setIsReady(true)
    }
  }, [ready, i18n.language])

  // 切换语言函数
  const setLanguage = (lang: SupportedLanguage) => {
    i18n.changeLanguage(lang)
    setLanguageState(lang)
    // 保存到 localStorage
    localStorage.setItem('i18nextLng', lang)
  }

  // Context 值
  const contextValue: I18nContextType = {
    language,
    setLanguage,
    t: (key: string, options?: any) => t(key, options) as string,
    isReady
  }

  // 获取当前语言的 Ant Design 语言包
  const antdLocale = antdLocales[language] || zhCN

  return (
    <I18nContext.Provider value={contextValue}>
      <ConfigProvider locale={antdLocale}>
        {children}
      </ConfigProvider>
    </I18nContext.Provider>
  )
}

// 自定义 Hook 用于使用 I18n Context
export const useI18n = (): I18nContextType => {
  const context = useContext(I18nContext)
  if (context === undefined) {
    throw new Error('useI18n must be used within an I18nProvider')
  }
  return context
}

// 语言切换 Hook
export const useLanguage = () => {
  const { language, setLanguage } = useI18n()
  
  const toggleLanguage = () => {
    const newLang: SupportedLanguage = language === 'zh' ? 'en' : 'zh'
    setLanguage(newLang)
  }

  return {
    currentLanguage: language,
    setLanguage,
    toggleLanguage,
    isChinese: language === 'zh',
    isEnglish: language === 'en'
  }
}
