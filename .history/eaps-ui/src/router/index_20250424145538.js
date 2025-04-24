import { createRouter, createWebHistory } from 'vue-router'

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/auth-test',
    name: 'AuthTest',
    component: () => import('../views/AuthTest.vue'),
    meta: { requiresAuth: false }  // 设为false便于测试
  },
  {
    path: '/student',
    name: 'StudentLayout',
    component: () => import('../views/student/Layout.vue'),
    meta: { requiresAuth: true, role: 'student' },
    children: [
      {
        path: 'profile',
        name: 'StudentProfile',
        component: () => import('../views/student/Profile.vue')
      },
      {
        path: 'jobs',
        name: 'StudentJobs',
        component: () => import('../views/student/Jobs.vue')
      },
      {
        path: 'applications',
        name: 'StudentApplications',
        component: () => import('../views/student/Applications.vue')
      },
      {
        path: 'chat',
        name: 'StudentChat',
        component: () => import('../views/student/Chat.vue')
      }
    ]
  },
  {
    path: '/company',
    name: 'CompanyLayout',
    component: () => import('../views/company/Layout.vue'),
    meta: { requiresAuth: true, role: 'company' },
    children: [
      {
        path: 'profile',
        name: 'CompanyProfile',
        component: () => import('../views/company/Profile.vue')
      },
      {
        path: 'jobs',
        name: 'CompanyJobs',
        component: () => import('../views/company/Jobs.vue')
      },
      {
        path: 'applications',
        name: 'CompanyApplications',
        component: () => import('../views/company/Applications.vue')
      },
      {
        path: 'chat',
        name: 'CompanyChat',
        component: () => import('../views/company/Chat.vue')
      }
    ]
  },
  {
    path: '/counselor',
    name: 'CounselorLayout',
    component: () => import('../views/counselor/Layout.vue'),
    meta: { requiresAuth: true, role: 'counselor' },
    children: [
      {
        path: 'profile',
        name: 'CounselorProfile',
        component: () => import('../views/counselor/Profile.vue')
      },
      {
        path: 'dashboard',
        name: 'CounselorDashboard',
        component: () => import('../views/counselor/Dashboard.vue')
      },
      {
        path: 'companies',
        name: 'CounselorCompanies',
        component: () => import('../views/counselor/Companies.vue')
      },
      {
        path: 'jobs',
        name: 'CounselorJobs',
        component: () => import('../views/counselor/Jobs.vue')
      },
      {
        path: 'reports',
        name: 'CounselorReports',
        component: () => import('../views/counselor/Reports.vue')
      },
      {
        path: 'chat',
        name: 'CounselorChat',
        component: () => import('../views/counselor/Chat.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token') || ''
  const userRole = localStorage.getItem('userRole') || ''
  const userInfoStr = localStorage.getItem('userInfo') || '{}'
  
  // 尝试从userInfo中获取备用角色信息
  let backupRole = ''
  try {
    const userInfo = JSON.parse(userInfoStr)
    backupRole = userInfo.role || ''
    // 如果localStorage中没有角色但userInfo中有，则使用userInfo中的角色
    if (!userRole && backupRole) {
      console.log('从userInfo中恢复角色:', backupRole)
      localStorage.setItem('userRole', backupRole.toLowerCase())
    }
  } catch (e) {
    console.error('解析userInfo失败:', e)
  }
  
  // 使用从userInfo恢复的角色或原始角色
  const effectiveRole = userRole || (backupRole ? backupRole.toLowerCase() : '')
  
  console.log('路由守卫:', {
    to: to.path,
    requiresAuth: to.meta.requiresAuth,
    role: to.meta.role,
    hasToken: !!token,
    userRole: effectiveRole,
    tokenLength: token ? token.length : 0
  })
  
  // 处理未登录情况
  if (to.meta.requiresAuth && !token) {
    console.log('需要登录，但未检测到token，重定向到登录页')
    next('/login?auth_required=true')
    return
  }
  
  // 处理角色权限 - 如果是前往测试页，不做权限校验
  if (to.meta.role && effectiveRole && to.path !== '/auth-test') {
    const routeRole = String(to.meta.role).toLowerCase()
    const currentRole = String(effectiveRole).toLowerCase()
    
    console.log('角色比较:', { 
      routeRole, 
      currentRole, 
      匹配: routeRole === currentRole 
    })
    
    if (routeRole !== currentRole) {
      console.log('角色不匹配，重定向到测试页面')
      // 重定向到测试页面进行诊断
      next('/auth-test?role_mismatch=true')
      return
    }
  }
  
  // 如果路由有效但用户可能存在认证问题，引导到测试页诊断
  if ((to.meta.requiresAuth || to.meta.role) && (!token || !effectiveRole)) {
    console.log('可能存在认证问题，导航到测试页')
    if (to.path !== '/auth-test') {
      next('/auth-test?auth_issue=true')
      return
    }
  }
  
  // 通过所有检查，放行
  console.log('路由通过:', to.path)
  next()
})

export default router 