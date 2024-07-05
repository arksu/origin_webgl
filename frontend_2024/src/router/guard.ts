import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { RouteNames } from '@/router/routeNames'

export default function guards(from: RouteLocationNormalized, to: RouteLocationNormalized, next: NavigationGuardNext) {

  const isAuthenticated = !!localStorage.getItem('token')
  if (to.name == RouteNames.LOGIN && !isAuthenticated) {
    next()
  } else if (to.matched.some(record => record.meta.requiresAuth) && !isAuthenticated) {
    console.log('redirect to login because authentication fail')
    next({ name: RouteNames.LOGIN })
  } else {
    next()
  }
}