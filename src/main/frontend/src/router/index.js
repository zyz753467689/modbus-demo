import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'dashboard', component: () => import('../views/DashboardView.vue') },
  { path: '/tcp/server', name: 'tcp-server', component: () => import('../views/TcpServerView.vue') },
  { path: '/tcp/client', name: 'tcp-client', component: () => import('../views/TcpClientView.vue') },
  { path: '/rtu/server', name: 'rtu-server', component: () => import('../views/RtuServerView.vue') },
  { path: '/rtu/client', name: 'rtu-client', component: () => import('../views/RtuClientView.vue') },
  { path: '/poll', name: 'poll', component: () => import('../views/PollView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
