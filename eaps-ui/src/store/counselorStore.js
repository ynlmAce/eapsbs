import { defineStore } from 'pinia'
import { getTasksCount, getTasksList, processTask } from '@/api/counselor'

export const useCounselorStore = defineStore('counselor', {
  state: () => ({
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
  }),
  
  actions: {
    // 设置工作台数据
    setDashboardData(data) {
      this.dashboard = data
    },
    
    // 设置任务列表
    setCompanyTasks(tasks) {
      this.companyTasks = tasks
    },
    
    setJobTasks(tasks) {
      this.jobTasks = tasks
    },
    
    setReportTasks(tasks) {
      this.reportTasks = tasks
    },
    
    // 设置加载状态
    setLoading(module, status) {
      this.loading[module] = status
    },
    
    // 处理任务后更新dashboard计数
    updateDashboardCount(type, subtype = null) {
      if (type === 'companyCertification' && this.dashboard.companyCertTasks > 0) {
        this.dashboard.companyCertTasks--
        this.dashboard.total--
      } else if (type === 'jobAudit' && this.dashboard.jobAuditTasks > 0) {
        this.dashboard.jobAuditTasks--
        this.dashboard.total--
      } else if (type === 'reportHandling') {
        // 对于举报，需要区分是评价举报还是消息举报
        if (subtype === 'rating' && this.dashboard.reportedRatings > 0) {
          this.dashboard.reportedRatings--
          this.dashboard.total--
        } else if (subtype === 'message' && this.dashboard.reportedMessages > 0) {
          this.dashboard.reportedMessages--
          this.dashboard.total--
        } else if (!subtype) {
          // 如果没有子类型，随机减少一个
          if (this.dashboard.reportedRatings > 0) {
            this.dashboard.reportedRatings--
            this.dashboard.total--
          } else if (this.dashboard.reportedMessages > 0) {
            this.dashboard.reportedMessages--
            this.dashboard.total--
          }
        }
      }
    },
    
    // 从列表中移除已处理的任务
    removeTaskFromList(type, taskId) {
      if (type === 'companyCertification') {
        this.companyTasks = this.companyTasks.filter(task => task.id !== taskId)
      } else if (type === 'jobAudit') {
        this.jobTasks = this.jobTasks.filter(task => task.id !== taskId)
      } else if (type === 'reportHandling') {
        this.reportTasks = this.reportTasks.filter(task => task.id !== taskId)
      }
    },
    
    // 获取工作台统计数据
    async fetchDashboardData() {
      this.setLoading('dashboard', true)
      try {
        const response = await getTasksCount()
        if (response.error === 0) {
          this.setDashboardData(response.body)
        } else {
          console.error('获取工作台数据失败', response.message)
        }
      } catch (error) {
        console.error('获取工作台数据异常', error)
      } finally {
        this.setLoading('dashboard', false)
      }
    },
    
    // 获取企业认证任务列表
    async fetchCompanyTasks({ page = 1, pageSize = 10, filters = {} } = {}) {
      this.setLoading('companies', true)
      try {
        const response = await getTasksList({
          type: 'companyCertification',
          page,
          pageSize,
          ...filters
        })
        
        if (response.error === 0) {
          this.setCompanyTasks(response.body.list || [])
          return response.body
        } else {
          console.error('获取企业认证任务失败', response.message)
          return { list: [], total: 0 }
        }
      } catch (error) {
        console.error('获取企业认证任务异常', error)
        return { list: [], total: 0 }
      } finally {
        this.setLoading('companies', false)
      }
    },
    
    // 获取岗位审核任务列表
    async fetchJobTasks({ page = 1, pageSize = 10, filters = {} } = {}) {
      this.setLoading('jobs', true)
      try {
        const response = await getTasksList({
          type: 'jobAudit',
          page,
          pageSize,
          ...filters
        })
        
        if (response.error === 0) {
          this.setJobTasks(response.body.list || [])
          return response.body
        } else {
          console.error('获取岗位审核任务失败', response.message)
          return { list: [], total: 0 }
        }
      } catch (error) {
        console.error('获取岗位审核任务异常', error)
        return { list: [], total: 0 }
      } finally {
        this.setLoading('jobs', false)
      }
    },
    
    // 获取举报处理任务列表
    async fetchReportTasks({ page = 1, pageSize = 10, filters = {} } = {}) {
      this.setLoading('reports', true)
      try {
        const response = await getTasksList({
          type: 'reportHandling',
          page,
          pageSize,
          ...filters
        })
        
        if (response.error === 0) {
          this.setReportTasks(response.body.list || [])
          return response.body
        } else {
          console.error('获取举报处理任务失败', response.message)
          return { list: [], total: 0 }
        }
      } catch (error) {
        console.error('获取举报处理任务异常', error)
        return { list: [], total: 0 }
      } finally {
        this.setLoading('reports', false)
      }
    },
    
    // 处理任务 (审核通过/拒绝/删除)
    async processTask({ taskId, type, subtype = null, action, reason, notes }) {
      try {
        const response = await processTask(taskId, action, reason, notes)
        
        if (response.error === 0) {
          // 更新统计数据和列表
          this.updateDashboardCount(type, subtype)
          this.removeTaskFromList(type, taskId)
          
          // 重新获取最新数据
          this.fetchDashboardData()
          
          // 根据类型刷新对应列表
          if (type === 'companyCertification') {
            this.fetchCompanyTasks({ page: 1, pageSize: 10 })
          } else if (type === 'jobAudit') {
            this.fetchJobTasks({ page: 1, pageSize: 10 })
          } else if (type === 'reportHandling') {
            this.fetchReportTasks({ page: 1, pageSize: 10 })
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
}) 