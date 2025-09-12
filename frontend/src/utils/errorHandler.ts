/**
 * 错误处理工具类
 * 用于处理常见的开发环境错误，特别是 ResizeObserver 相关错误
 */

/**
 * 检查是否为 ResizeObserver 相关错误
 */
export const isResizeObserverError = (message: string): boolean => {
  return (
    message.includes('ResizeObserver loop completed with undelivered notifications') ||
    message.includes('ResizeObserver loop') ||
    message.includes('ResizeObserver')
  );
};

/**
 * 初始化错误处理
 * 抑制 ResizeObserver 相关的错误和警告
 */
export const initErrorHandler = (): void => {
  // 抑制 console.error 中的 ResizeObserver 错误
  const originalConsoleError = console.error;
  console.error = (...args) => {
    if (
      typeof args[0] === 'string' &&
      isResizeObserverError(args[0])
    ) {
      return;
    }
    originalConsoleError.apply(console, args);
  };

  // 抑制 console.warn 中的 ResizeObserver 警告
  const originalConsoleWarn = console.warn;
  console.warn = (...args) => {
    if (
      typeof args[0] === 'string' &&
      isResizeObserverError(args[0])
    ) {
      return;
    }
    originalConsoleWarn.apply(console, args);
  };

  // 处理全局错误事件
  window.addEventListener('error', (e) => {
    const msg = (e as ErrorEvent).message || '';
    if (isResizeObserverError(msg)) {
      e.stopImmediatePropagation();
      e.preventDefault();
      return false;
    }
  });

  // 处理未捕获的 Promise 拒绝
  window.addEventListener('unhandledrejection', (e) => {
    const msg = e.reason?.message || '';
    if (isResizeObserverError(msg)) {
      e.preventDefault();
      return false;
    }
  });
};
