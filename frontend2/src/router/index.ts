import {createRouter, createWebHistory, NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from "vue-router";
import Login from "../views/Login.vue";
import NotFound from "../views/NotFound.vue";
import About from "../views/About.vue";
import guards from "./guards";

const routes: Array<RouteRecordRaw> = [
    {
        path: "/login",
        name: "Login",
        component: Login
    },
    {
        path: "/about",
        name: "About",
        component: About
    },
    {
        path: "/:catchAll(.*)",
        component: NotFound,
    }
];


const router = createRouter({
    // history: createWebHistory(process.env.BASE_URL),
    history: createWebHistory(),
    routes
});


router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    guards(from, to, next);
})

export default router;
