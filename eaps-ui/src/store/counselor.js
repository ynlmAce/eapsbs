import { getTasksCount, getTasksList, processTask } from '@/api/counselor'

const state = {
  // 工作台统计数据
  dashboard: {
    companyCertTasks: 0,
    jobAuditTasks: 0,
    reportedRatings: 0,
    reportedMessages: 0,
    total: 0
  },
  // 任务列表
  companyTasks: [],
  jobTasks: [],
  reportTasks: [],
  // 任务加载状态
  loading: {
    dashboard: false,
    companies: false,
    jobs: false,
    reports: false
  }
}

const mutations = {
  SET_DASHBOARD_DATA(state, data) {
    state.dashboard = data
  },
  SET_COMPANY_TASKS(state, tasks) {
    state.companyTasks = tasks
  },
  SET_JOB_TASKS(state, tasks) {
    state.jobTasks = tasks
  },
  SET_REPORT_TASKS(state, tasks) {
    state.reportTasks = tasks
  },
  SET_LOADING(state, { module, status }) {
    state.loading[module] = status
  },
  // 处理任务后更新dashboard计数
  UPDATE_DASHBOARD_COUNT(state, { type }) {
    if (type === 'companyCertification' && state.dashboard.companyCertTasks > 0) {
      state.dashboard.companyCertTasks--
      state.dashboard.total--
    } else if (type === 'jobAudit' && state.dashboard.jobAuditTasks > 0) {
      state.dashboard.jobAuditTasks--
      state.dashboard.total--
    } else if (type === 'reportHandling') {
      // 对于举报，需要区分是评价举报还是消息举报
      if (state.dashboard.reportedRatings > 0) {
        state.dashboard.reportedRatings--
        state.dashboard.total--
      } else if (state.dashboard.reportedMessages > 0) {
        state.dashboard.reportedMessages--
        state.dashboard.total--
      }
    }
  },
  // 从列表中移除已处理的任务
  REMOVE_TASK_FROM_LIST(state, { type, taskId }) {
    if (type === 'companyCertification') {
      state.companyTasks = state.companyTasks.filter(task => task.id !== taskId)
    } else if (type === 'jobAudit') {
      state.jobTasks = state.jobTasks.filter(task => task.id !== taskId)
    } else if (type === 'reportHandling') {
      state.reportTasks = state.reportTasks.filter(task => task.id !== taskId)
    }
  }
}

const actions = {
  // 获取工作台统计数据
  async fetchDashboardData({ commit }) {
    commit('SET_LOADING', { module: 'dashboard', status: true })
    try {
      const response = await getTasksCount()
      if (response.error === 0) {
        commit('SET_DASHBOARD_DATA', response.body)
      } else {
        console.error('获取工作台数据失败', response.message)
      }
    } catch (error) {
      console.error('获取工作台数据异常', error)
    } finally {
      commit('SET_LOADING', { module: 'dashboard', status: false })
    }
  },
  
  // 获取企业认证任务列表
  async fetchCompanyTasks({ commit }, { page = 1, pageSize = 10, filters = {} }) {
    commit('SET_LOADING', { module: 'companies', status: true })
    try {
      const response = await getTasksList({
        type: 'companyCertification',
        page,
        pageSize,
        ...filters
      })
      if (response.error === 0) {
        commit('SET_COMPANY_TASKS', response.body.list || [])
        return response.body
      } else {
        console.error('获取企业认证任务失败', response.message)
        return { list: [], total: 0 }
      }
    } catch (error) {
      console.error('获取企业认证任务异常', error)
      return { list: [], total: 0 }
    } finally {
      commit('SET_LOADING', { module: 'companies', status: false })
    }
  },
  
  // 获取岗位审核任务列表
  async fetchJobTasks({ commit }, { page = 1, pageSize = 10, filters = {} }) {
    commit('SET_LOADING', { module: 'jobs', status: true })
    try {
      const response = await getTasksList({
        type: 'jobAudit',
        page,
        pageSize,
        ...filters
      })
      if (response.error === 0) {
        commit('SET_JOB_TASKS', response.body.list || [])
        return response.body
      } else {
        console.error('获取岗位审核任务失败', response.message)
        return { list: [], total: 0 }
      }
    } catch (error) {
      console.error('获取岗位审核任务异常', error)
      return { list: [], total: 0 }
    } finally {
      commit('SET_LOADING', { module: 'jobs', status: false })
    }
  },
  
  // 获取举报处理任务列表
  async fetchReportTasks({ commit }, { page = 1, pageSize = 10, filters = {} }) {
    commit('SET_LOADING', { module: 'reports', status: true })
    try {
      const response = await getTasksList({
        type: 'reportHandling',
        page,
        pageSize,
        ...filters
      })
      if (response.error === 0) {
        commit('SET_REPORT_TASKS', response.body.list || [])
        return response.body
      } else {
        console.error('获取举报处理任务失败', response.message)
        return { list: [], total: 0 }
      }
    } catch (error) {
      console.error('获取举报处理任务异常', error)
      return { list: [], total: 0 }
    } finally {
      commit('SET_LOADING', { module: 'reports', status: false })
    }
  },
  
  // 处理任务 (审核通过/拒绝/删除)
  async processTask({ commit, dispatch }, { taskId, type, action, reason, notes }) {
    try {
      const response = await processTask(taskId, action, reason, notes)
      if (response.error === 0) {
        // 更新统计数据和列表
        commit('UPDATE_DASHBOARD_COUNT', { type })
        commit('REMOVE_TASK_FROM_LIST', { type, taskId })
        
        // 重新获取最新数据
        dispatch('fetchDashboardData')
        
        // 根据类型刷新对应列表
        if (type === 'companyCertification') {
          dispatch('fetchCompanyTasks', { page: 1, pageSize: 10 })
        } else if (type === 'jobAudit') {
          dispatch('fetchJobTasks', { page: 1, pageSize: 10 })
        } else if (type === 'reportHandling') {
          dispatch('fetchReportTasks', { page: 1, pageSize: 10 })
        }
        
        return { success: true, ...response.body }
      } else {
        console.error('处理任务失败', response.message)
        return { success: false, message: response.message }
      }
    } catch (error) {
      console.error('处理任务异常', error)
      return { success: false, message: error.message }
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
} 