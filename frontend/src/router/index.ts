import {createRouter, createWebHistory, NavigationGuardNext, RouteLocationNormalized, RouteRecordRaw} from "vue-router";
import Login from "../views/Login.vue";
import NotFound from "../views/NotFound.vue";
import SignUp from "../views/SignUp.vue";
import About from "../views/About.vue";
import Characters from "../views/characters/index.vue";
import CreateNewCharacter from "../views/characters/CreateNew.vue";
import Test from "../views/Test.vue"
import GameView from "../views/game/GameView.vue";

import guards from "./guards";
import {RouteNames} from "./routeNames";


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
        path: "/new-character",
        name: RouteNames.NEW_CHARACTER,
        component: CreateNewCharacter
    },
    {
        path: "/game",
        name: RouteNames.GAME,
        component: GameView
    },
    {
        path: "/about",
        name: RouteNames.ABOUT,
        component: About
    },
    {
        path: "/test",
        name: "test",
        component : Test
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
