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
  const token = localStorage.getItem('token')
  const userRole = localStorage.getItem('userRole') || ''
  
  console.log('路由守卫:', {
    to: to.path,
    requiresAuth: to.meta.requiresAuth,
    role: to.meta.role,
    hasToken: !!token,
    userRole
  })
  
  // 处理未登录情况
  if (to.meta.requiresAuth && !token) {
    console.log('需要登录，但未检测到token，重定向到登录页')
    next('/login')
    return
  }
  
  // 处理角色权限
  if (to.meta.role && userRole) {
    const routeRole = String(to.meta.role).toLowerCase()
    const currentRole = String(userRole).toLowerCase()
    
    console.log('角色比较:', { 
      routeRole, 
      currentRole, 
      匹配: routeRole === currentRole 
    })
    
    if (routeRole !== currentRole) {
      console.log('角色不匹配，重定向到对应角色首页')
      // 根据当前角色重定向到对应首页
      if (currentRole === 'student') {
        next('/student/profile')
      } else if (currentRole === 'company') {
        next('/company/profile')
      } else if (currentRole === 'counselor') {
        next('/counselor/dashboard')
      } else {
        next('/login')
      }
      return
    }
  }
  
  // 通过所有检查，放行
  console.log('路由通过:', to.path)
  next()
})

export default router 