import { ConfigProvider } from 'antd'
import enUS from 'antd/es/locale/en_US'
import zhCN from 'antd/es/locale/zh_CN'
import React, { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react'
import zh from '../locales/zh.json'
import en from '../locales/en.json'

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
  const [language, setLanguageState] = useState<SupportedLanguage>('zh')
  const [isReady, setIsReady] = useState(false)

  // 简单本地字典
  const dictionaries = useMemo(() => ({ zh, en }), [])

  // 初始化语言设置
  useEffect(() => {
    const stored = localStorage.getItem('i18nextLng') as SupportedLanguage | null
    if (stored === 'zh' || stored === 'en') {
      setLanguageState(stored)
    } else {
      const browserLang = (navigator.language || 'zh').startsWith('zh') ? 'zh' : 'en'
      setLanguageState(browserLang as SupportedLanguage)
    }
    setIsReady(true)
  }, [])

  // 切换语言函数
  const setLanguage = (lang: SupportedLanguage) => {
    setLanguageState(lang)
    localStorage.setItem('i18nextLng', lang)
  }

  // 简单的 key 取值函数：支持命名空间用点分隔
  const t = (fullKey: string): string => {
    const dict = language === 'zh' ? dictionaries.zh : dictionaries.en
    const parts = fullKey.split('.')
    let cur: any = dict
    for (const p of parts) {
      if (cur && typeof cur === 'object' && p in cur) {
        cur = (cur as any)[p]
      } else {
        return fullKey
      }
    }
    return typeof cur === 'string' ? cur : fullKey
  }

  // Context 值
  const contextValue: I18nContextType = {
    language,
    setLanguage,
    t,
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
