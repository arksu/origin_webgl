import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'
import { RouteNames } from '@/router/routeNames'

export default function guards(from: RouteLocationNormalized, to: RouteLocationNormalized, next: NavigationGuardNext) {

  if (to.name == RouteNames.LOGIN) {
    next();
  }

  next({name: RouteNames.LOGIN})
}