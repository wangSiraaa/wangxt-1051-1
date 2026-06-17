import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/components/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'applications',
        name: 'Applications',
        component: () => import('@/views/ApplicationList.vue'),
        meta: { title: '许可申请' }
      },
      {
        path: 'applications/new',
        name: 'ApplicationNew',
        component: () => import('@/views/ApplicationForm.vue'),
        meta: { title: '新建申请' }
      },
      {
        path: 'applications/:id',
        name: 'ApplicationDetail',
        component: () => import('@/views/ApplicationDetail.vue'),
        meta: { title: '申请详情' }
      },
      {
        path: 'permits',
        name: 'Permits',
        component: () => import('@/views/PermitList.vue'),
        meta: { title: '许可证管理' }
      },
      {
        path: 'permits/:id',
        name: 'PermitDetail',
        component: () => import('@/views/PermitDetail.vue'),
        meta: { title: '许可证详情' }
      },
      {
        path: 'inspections',
        name: 'Inspections',
        component: () => import('@/views/InspectionList.vue'),
        meta: { title: '现场核查' }
      },
      {
        path: 'inspections/new',
        name: 'InspectionNew',
        component: () => import('@/views/InspectionForm.vue'),
        meta: { title: '新增核查' }
      },
      {
        path: 'penalty-clues',
        name: 'PenaltyClues',
        component: () => import('@/views/PenaltyClueList.vue'),
        meta: { title: '处罚线索' }
      },
      {
        path: 'penalty-clues/:id',
        name: 'PenaltyClueDetail',
        component: () => import('@/views/PenaltyClueDetail.vue'),
        meta: { title: '处罚线索详情' }
      },
      {
        path: 'audit-timeline',
        name: 'AuditTimeline',
        component: () => import('@/views/AuditTimeline.vue'),
        meta: { title: '审计时间线' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userInfo = localStorage.getItem('userInfo')
  if (to.meta.requiresAuth && !userInfo) {
    next('/login')
  } else if (to.path === '/login' && userInfo) {
    next('/')
  } else {
    next()
  }
})

export default router
