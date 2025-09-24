/**
 * Cookie工具函数
 * 用于管理xUsername cookie
 */

/**
 * 设置xUsername cookie
 * @param username 用户名
 * @param days cookie有效期（天数，默认30天）
 */
export function setXUsernameCookie(username: string, days: number = 30): void {
  const expires = new Date();
  expires.setTime(expires.getTime() + (days * 24 * 60 * 60 * 1000));
  
  document.cookie = `xUsername=${encodeURIComponent(username)};expires=${expires.toUTCString()};path=/`;
}

/**
 * 删除xUsername cookie
 */
export function removeXUsernameCookie(): void {
  document.cookie = 'xUsername=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/;';
}

/**
 * 检查是否有xUsername cookie
 * @returns 是否存在xUsername cookie
 */
export function hasXUsernameCookie(): boolean {
  const cookies = document.cookie.split(';');
  return cookies.some(cookie => cookie.trim().startsWith('xUsername='));
}
