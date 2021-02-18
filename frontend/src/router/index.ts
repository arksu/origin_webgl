import {createRouter, createWebHistory, NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from "vue-router";
import store from "@/store/store"

import Login from "@/views/Login.vue";
import Signup from "@/views/Signup.vue";
import Game from "@/views/Game.vue";
import NotFound from "@/views/NotFound.vue";
import Client from "@/net/Client";
import Characters from "@/views/characters/Characters.vue";
import NewCharacter from "@/views/characters/NewCharacter.vue";
import About from "@/views/About.vue";

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
    console.warn("route ", from.name, " => ", to.name);
    // всегда даем переход на "о нас"
    if (to.name == 'About') {
        next();
    }
    // всегда даем зарегистрироваться
    else if (to.name == 'Signup') {
        next();
    } else if (to.name == 'Login') {
        next();
    } else if (to.name == 'Game') {
        if (!store.getters.isLogged) {
            console.log("not logged")
            next({name: "Login"})
        } else if (Client.instance.selectedCharacterId == undefined) {
            next({name: "Characters"})
        } else {
            next();
        }
    }
    // если не авторизованы надо перейти на логин форму
    else if (to.name !== 'Login' && !store.getters.isLogged) {
        // это первый запуск?
        if (from.name == undefined) {
            // Client.instance.needAutologin = true;
        }
        console.log("auth required, redirect to login")
        next({name: "Login"})
    } else {
        next();
    }
})

export default router;