/**
 * 处理API响应数据
 * @param {Object} response - API响应对象
 * @returns {Promise} - 成功时返回响应body，失败时抛出错误
 */
export function handleResponse(response) {
  console.log('处理API响应:', response);
  
  // 检查响应是否存在
  if (!response) {
    console.error('API响应为空');
    return Promise.reject(new Error('网络错误，请稍后重试'));
  }

  // 检查是否已经是处理过的数据（不包含error字段）
  if (typeof response !== 'object' || !('error' in response)) {
    console.log('响应已经是处理过的数据，直接返回');
    return Promise.resolve(response);
  }

  // 解构响应数据
  const { error, message, body } = response;
  
  console.log(`API响应状态: error=${error}, message=${message || '无'}, hasBody=${!!body}`);

  // 根据error码处理不同情况
  if (error === 0) {
    // 成功情况，返回body数据
    return Promise.resolve(body);
  } else if (error === 401) {
    // 未登录或登录失效
    console.warn('API返回401未授权');
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userId');
    
    // 避免在同一页面连续多次重定向导致的浏览器警告
    if (!window.location.href.includes('/login')) {
      window.location.href = '/login?session_expired=true';
    }
    return Promise.reject(new Error('请先登录'));
  } else if (error === 403) {
    // 权限不足
    console.warn('API返回403权限不足');
    return Promise.reject(new Error('您没有权限执行此操作'));
  } else if (error === 500) {
    // 系统异常
    console.error('API返回500系统异常:', message);
    return Promise.reject(new Error('系统异常: ' + message));
  } else if (error === 400) {
    // 请求参数错误
    console.error('API返回400参数错误:', message);
    return Promise.reject(new Error(message || '请求参数错误'));
  } else {
    // 其他业务异常
    console.error(`API返回未知错误码 ${error}:`, message);
    return Promise.reject(new Error(message || '操作失败'));
  }
} 