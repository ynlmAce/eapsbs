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
  const userRole = localStorage.getItem('userRole')
  
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.role && to.meta.role !== userRole) {
    next('/login')
  } else {
    next()
  }
})

export default router 