import { useI18n, useLanguage } from '../contexts/I18nContext'

/**
 * 便捷的翻译 Hook
 * 提供类型安全的翻译功能
 */
export const useTranslation = () => {
  const { t, language, isReady } = useI18n()

  // 带命名空间的翻译函数
  const translate = (namespace: string, key: string, options?: any) => {
    return t(`${namespace}.${key}`, options)
  }

  // 通用翻译函数
  const translateCommon = (key: string, options?: any) => {
    return t(`common.${key}`, options)
  }

  // 应用相关翻译
  const translateApp = (key: string, options?: any) => {
    return t(`app.${key}`, options)
  }

  // 导航相关翻译
  const translateNavigation = (key: string, options?: any) => {
    return t(`navigation.${key}`, options)
  }

  // 首页相关翻译
  const translateHome = (key: string, options?: any) => {
    return t(`home.${key}`, options)
  }

  // 用户相关翻译
  const translateUser = (key: string, options?: any) => {
    return t(`user.${key}`, options)
  }

  // 软件包相关翻译
  const translateSoftwarePackage = (key: string, options?: any) => {
    return t(`softwarePackage.${key}`, options)
  }

  // 用户角色相关翻译
  const translateUserRole = (key: string, options?: any) => {
    return t(`userRole.${key}`, options)
  }

  // 测试用例集相关翻译
  const translateTestCaseSet = (key: string, options?: any) => {
    return t(`testCaseSet.${key}`, options)
  }

  // 操作记录相关翻译
  const translateOperationLog = (key: string, options?: any) => {
    return t(`operationLog.${key}`, options)
  }

  // 语言相关翻译
  const translateLanguage = (key: string, options?: any) => {
    return t(`language.${key}`, options)
  }

  // Footer相关翻译
  const translateFooter = (key: string, options?: any) => {
    return t(`footer.${key}`, options)
  }

  return {
    t,
    translate,
    translateCommon,
    translateApp,
    translateNavigation,
    translateHome,
    translateUser,
    translateSoftwarePackage,
    translateUserRole,
    translateTestCaseSet,
    translateOperationLog,
    translateLanguage,
    translateFooter,
    language,
    isReady
  }
}

// 导出 useLanguage hook
export { useLanguage }

// 导出类型定义
export type TranslationFunction = (key: string, options?: any) => string
