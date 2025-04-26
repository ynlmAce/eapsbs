import request from '@/utils/request';
import { handleResponse } from '@/utils/api';

/**
 * 用户登录
 * @param {Object} loginData - 登录数据
 * @param {string} loginData.username - 用户名
 * @param {string} loginData.password - 密码
 * @returns {Promise} - 返回登录结果
 */
export function login(loginData) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: loginData || {}
  }).then(handleResponse);
}

/**
 * 用户注册
 * @param {Object} registerData - 注册数据
 * @param {string} registerData.username - 用户名
 * @param {string} registerData.password - 密码
 * @param {string} registerData.role - 角色(student/company/counselor)
 * @param {Object} registerData.profile - 用户基本资料
 * @returns {Promise} - 返回注册结果
 */
export function register(registerData) {
  return request({
    url: '/auth/register',
    method: 'post',
    data: registerData || {}
  }).then(handleResponse);
}

/**
 * 修改密码
 * @param {Object} passwordData - 密码数据
 * @param {string} passwordData.oldPassword - 旧密码
 * @param {string} passwordData.newPassword - 新密码
 * @param {string} passwordData.confirmPassword - 确认新密码
 * @returns {Promise} - 返回修改结果
 */
export function changePassword(passwordData) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data: passwordData || {}
  }).then(handleResponse);
}

/**
 * 重置密码
 * @param {Object} resetData - 重置数据
 * @param {string} resetData.username - 用户名
 * @param {string} resetData.securityQuestion - 密保问题
 * @param {string} resetData.securityAnswer - 密保答案
 * @param {string} resetData.newPassword - 新密码
 * @returns {Promise} - 返回重置结果
 */
export function resetPassword(resetData) {
  return request({
    url: '/auth/reset-password',
    method: 'post',
    data: resetData || {}
  }).then(handleResponse);
}

/**
 * 获取当前用户信息
 * @returns {Promise} - 返回用户信息
 */
export function getUserInfo() {
  return request({
    url: '/auth/user-info',
    method: 'post',
    data: {}
  }).then(handleResponse);
}

/**
 * 用户登出
 * @returns {Promise} - 返回登出结果
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post',
    data: {}
  }).then(handleResponse);
}

/**
 * 获取密保问题
 * @param {Object} userData - 用户数据
 * @param {string} userData.username - 用户名
 * @returns {Promise} - 返回密保问题
 */
export function getSecurityQuestion(userData) {
  return request({
    url: '/auth/security-question',
    method: 'post',
    data: userData || {}
  }).then(handleResponse);
}

/**
 * 检查用户名是否可用
 * @param {Object} checkData - 检查数据
 * @param {string} checkData.username - 用户名
 * @returns {Promise} - 返回检查结果
 */
export function checkUsernameAvailable(checkData) {
  return request({
    url: '/auth/check-username',
    method: 'post',
    data: checkData || {}
  }).then(handleResponse);
} 