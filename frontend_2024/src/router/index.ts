import { createRouter, createWebHistory } from 'vue-router'
import type { NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw } from 'vue-router'
import { RouteNames } from '@/router/routeNames'
import guards from '@/router/guard'
import Login from '@/views/Login.vue'
import Characters from '@/views/characters/index.vue'
import SignUp from '@/views/SignUp.vue'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: {
      name: RouteNames.LOGIN
    }
  },
  {
    path: '/login',
    name: RouteNames.LOGIN,
    component: Login
  },
  {
    path: '/signup',
    name: RouteNames.SIGN_UP,
    component: SignUp
  },
  {
    path: '/characters',
    name: RouteNames.CHARACTERS,
    component: Characters,
    meta: {
      requiresAuth: true
    }
  },
  {
    path: '/:pathMatch(.*)',
    redirect: {
      name: RouteNames.LOGIN,
      params: {}
    }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: routes
})

router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
  guards(from, to, next)
})

export default router
