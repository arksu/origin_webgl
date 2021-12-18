import {createRouter, createWebHistory, NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from "vue-router";
import Login from "../views/Login.vue";
import NotFound from "../views/NotFound.vue";
import SignUp from "../views/SignUp.vue";
import About from "../views/About.vue";
import guards from "./guards";
import {RouteNames} from "./routeNames";
import Characters from "../views/characters/index.vue";

const routes: Array<RouteRecordRaw> = [
    {
        path: "/login",
        name: RouteNames.LOGIN,
        component: Login
    },
    {
        path: "/signup",
        name: RouteNames.SIGN_UP,
        component: SignUp
    },
    {
        path: "/characters",
        name: RouteNames.CHARACTERS,
        component: Characters
    },
    {
        path: "/about",
        name: RouteNames.ABOUT,
        component: About
    },
    {
        path: "/:catchAll(.*)",
        component: NotFound,
    }
];


const router = createRouter({
    history: createWebHistory(),
    routes
});


router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    guards(from, to, next);
})

export default router;
