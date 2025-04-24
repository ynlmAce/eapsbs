// 导出所有API
import * as userApi from './user'
import * as studentApi from './student'
import * as jobApi from './job'
import * as chatApi from './chat'
import * as ratingApi from './rating'
import * as jobTagApi from './jobTag'
import * as counselorApi from './counselor'

export {
  userApi,
  studentApi,
  jobApi,
  chatApi,
  ratingApi,
  jobTagApi,
  counselorApi
}

// 导出axios实例，用于特殊处理
export { default as request } from './axios' 