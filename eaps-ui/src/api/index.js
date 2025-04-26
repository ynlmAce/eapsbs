// 统一导出所有API模块
import * as userApi from './user'
import * as studentApi from './student'
import * as companyApi from './company'
import * as jobApi from './job'
import * as ratingApi from './rating'
import * as chatApi from './chat'
import * as counselorApi from './counselor'

export {
  userApi,
  studentApi,
  companyApi,
  jobApi,
  ratingApi,
  chatApi,
  counselorApi
}

// 导出常用API函数
export const { login, register, logout, getUserInfo } = userApi
export const { getJobList, getJobDetail, applyForJob } = jobApi
export const { getStudentProfile, updateStudentProfile } = studentApi
export const { getCompanyProfile, updateCompanyProfile } = companyApi

// 导出axios实例，用于特殊处理
export { default as request } from './axios' 