import {createRouter, createWebHistory, NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from "vue-router";

import Login from "@/views/Login.vue";
import Signup from "@/views/Signup.vue";
import Game from "@/views/game/Game.vue";
import NotFound from "@/views/NotFound.vue";
import Characters from "@/views/characters/Characters.vue";
import NewCharacter from "@/views/characters/NewCharacter.vue";
import About from "@/views/About.vue";
import guards from "./guards"

const routes: Array<RouteRecordRaw> = [
    {
        path: "/",
        name: "Game",
        component: Game
    },
    {
        path: "/login",
        name: "Login",
        component: Login
    },
    {
        path: "/signup",
        name: "Signup",
        component: Signup
    },
    {
        path: "/characters",
        name: "Characters",
        component: Characters
    },
    {
        path: "/new-character",
        name: "NewCharacter",
        // component: () => import("@/views/characters/NewCharacter.vue")
        component: NewCharacter
    },
    {
        path: "/about",
        name: "About",
        // route level code-splitting
        // this generates a separate chunk (about.[hash].js) for this route
        // which is lazy-loaded when the route is visited.
        // component: () => import(/* webpackChunkName: "about" */ "@/views/About.vue")
        component: About
    },
    {
        path: "/:catchAll(.*)",
        component: NotFound,
    }
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
});

router.beforeEach((to: RouteLocationNormalized, from: RouteLocationNormalized, next: NavigationGuardNext) => {
    guards(from, to, next);
})

export default router;
