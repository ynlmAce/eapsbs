import request from './axios'

/**
 * 获取学生个人档案信息
 * @returns {Promise} 返回学生档案信息
 */
export function getStudentProfile() {
  // 模拟API返回数据
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        error: 0,
        body: {
          name: '张三',
          studentId: '20200101',
          school: '示例大学',
          major: '计算机科学与技术',
          grade: '大三',
          contact: '13800138000',
          selfIntroduction: '我是一名热爱编程的计算机专业学生，擅长Web开发和数据分析，希望能够在互联网行业找到合适的工作机会。',
          educationExperience: [
            {
              schoolName: '示例大学',
              major: '计算机科学与技术',
              degree: '本科',
              timeRange: ['2020-09-01', '2024-06-30'],
              description: '主修课程：数据结构、算法分析、操作系统、计算机网络、数据库系统等'
            }
          ],
          internshipExperience: [
            {
              companyName: '科技有限公司',
              position: '前端开发实习生',
              timeRange: ['2022-07-01', '2022-09-30'],
              description: '参与公司电商平台的前端开发，使用Vue.js负责用户界面开发和优化，提高了页面加载速度和用户体验。'
            }
          ],
          projectExperience: [
            {
              projectName: '校园二手交易平台',
              role: '全栈开发',
              timeRange: ['2021-10-01', '2021-12-30'],
              description: '使用Vue.js和Spring Boot开发的校园二手交易平台，我负责前后端开发，实现了用户注册、商品发布、搜索和在线交流等功能。'
            }
          ],
          skills: ['java', 'javascript', 'html', 'vue', 'mysql'],
          jobPreferences: {
            jobType: ['fulltime', 'internship'],
            expectedSalary: '10k-15k',
            locations: ['beijing', 'shanghai', 'hangzhou'],
            industries: ['internet', 'finance']
          },
          behaviorScore: 90
        },
        message: '获取成功'
      });
    }, 1000);
  });

  // 实际API调用
  // return request({
  //   url: '/student/profile',
  //   method: 'get'
  // })
}

/**
 * 更新学生个人档案信息
 * @param {Object} data 学生档案信息
 * @returns {Promise} 更新结果
 */
export function updateStudentProfile(data) {
  // 模拟API返回数据
  return new Promise((resolve) => {
    setTimeout(() => {
      console.log('接收到的更新数据:', data);
      resolve({
        error: 0,
        body: {},
        message: '保存成功'
      });
    }, 1000);
  });

  // 实际API调用
  // return request({
  //   url: '/student/profile',
  //   method: 'post',
  //   data
  // })
}

/**
 * 上传学生简历文件
 * @param {FormData} formData 包含文件的表单数据
 * @returns {Promise} 上传结果
 */
export function uploadResumeFile(formData) {
  // 模拟API返回数据
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        error: 0,
        body: {
          fileId: 'resume-' + Date.now(),
          fileName: formData.get('file').name,
          fileUrl: URL.createObjectURL(formData.get('file'))
        },
        message: '上传成功'
      });
    }, 1500);
  });

  // 实际API调用
  // return request({
  //   url: '/student/resume/upload',
  //   method: 'post',
  //   data: formData,
  //   headers: {
  //     'Content-Type': 'multipart/form-data'
  //   }
  // })
}

/**
 * 删除已上传的简历文件
 * @param {String} fileId 文件ID
 * @returns {Promise} 删除结果
 */
export function deleteResumeFile(fileId) {
  // 模拟API返回数据
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        error: 0,
        body: {},
        message: '删除成功'
      });
    }, 800);
  });

  // 实际API调用
  // return request({
  //   url: `/student/resume/${fileId}`,
  //   method: 'delete'
  // })
} 