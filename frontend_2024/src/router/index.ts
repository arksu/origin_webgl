import { createRouter, createWebHistory } from 'vue-router'
import type { NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw } from 'vue-router'
import { RouteNames } from '@/router/routeNames'
import Login from '@/views/Login.vue'
import guards from '@/router/guard'

const routes: Array<RouteRecordRaw> = [
  {
    path: "/login",
    name: RouteNames.LOGIN,
    component: Login
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routes
})

router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
  guards(from, to, next);
})

export default router
