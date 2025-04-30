import { createRouter, createWebHashHistory } from 'vue-router'
import { ElMessage } from 'element-plus'

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
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/ForgotPassword.vue'),
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
      },
      {
        path: 'company/:id',
        name: 'StudentCompanyDetail',
        component: () => import('../views/student/CompanyDetail.vue')
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
        path: 'job/create',
        name: 'CompanyJobCreate',
        component: () => import('../views/company/JobForm.vue')
      },
      {
        path: 'job/edit/:id',
        name: 'CompanyJobEdit',
        component: () => import('../views/company/JobForm.vue')
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
      },
      {
        path: 'students',
        name: 'CounselorStudents',
        component: () => import('@/views/counselor/Students.vue'),
        meta: { requiresAuth: true, role: 'counselor', title: '学生管理' }
      }
    ]
  },
  {
    path: '/forum',
    name: 'ForumList',
    component: () => import('../views/forum/ForumList.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/forum/new',
    name: 'ForumNewPost',
    component: () => import('../views/forum/NewPost.vue'),
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/forum/:id',
    name: 'ForumPostDetail',
    component: () => import('../views/forum/PostDetail.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue')
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 获取token
  const token = localStorage.getItem('token');
  // 获取当前用户角色
  const userRole = localStorage.getItem('userRole');
  
  // 判断路由是否需要身份验证
  const requiresAuth = to.meta.requiresAuth !== false; // 默认都需要登录
  // 获取路由允许的角色
  const routeRole = to.meta.role;
  
  console.log('路由守卫:', {
    to: to.path,
    requiresAuth,
    role: routeRole,
    hasToken: !!token,
    userRole,
    from: from.path
  });
  
  // 不需要登录的路由，直接通过
  if (!requiresAuth) {
    console.log('无需登录路由');
    
    // 如果已登录且访问登录/注册页，则重定向到首页或对应角色首页
    if (token && (to.path === '/login' || to.path === '/register')) {
      // 根据角色重定向到不同首页
      if (userRole === 'student') {
        return next('/student/profile');
      } else if (userRole === 'company') {
        return next('/company/profile');
      } else if (userRole === 'counselor') {
        return next('/counselor/dashboard');
      } else {
        return next('/');
      }
    }
    
    return next();
  }
  
  // 需要登录但没有token
  if (!token) {
    console.log('需要登录但没有token，重定向到登录页');
    // 保存尝试访问的页面，登录后跳转
    if (to.path !== '/login') {
      localStorage.setItem('redirectPath', to.fullPath);
    }
    return next('/login');
  }
  
  // 检查角色是否匹配
  if (routeRole && userRole) {
    // 角色比较
    const roleMatches = routeRole === userRole;
    console.log('角色比较:', {
      routeRole,
      currentRole: userRole,
      '匹配': roleMatches
    });
    
    if (!roleMatches) {
      console.log('角色不匹配，拒绝访问');
      // 重定向到对应角色的入口页面
      if (userRole === 'student') {
        return next('/student/profile');
      } else if (userRole === 'company') {
        return next('/company/profile');
      } else if (userRole === 'counselor') {
        return next('/counselor/dashboard');
      } else {
        return next('/');
      }
    }
  }
  
  // 如果一切正常，继续导航
  console.log('路由通过:', to.path);
  next();
});

export default router 