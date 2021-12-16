import {NavigationGuardNext, RouteLocationNormalized} from "vue-router";
import {useMainStore} from "../store";

export default function guards(from: RouteLocationNormalized, to: RouteLocationNormalized, next: NavigationGuardNext) {
    const store = useMainStore()

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
        if (!store.isLogged) {
            console.log("not logged")
            next({name: "Login"})
            // } else if (Client.instance.selectedCharacterId == undefined) {
            //     next({name: "Characters"})
        } else {
            next();
        }
    }
    // если не авторизованы надо перейти на логин форму
    else if (to.name !== 'Login' && !store.isLogged) {
        // это первый запуск?
        if (from.name == undefined) {
            // Client.instance.needAutologin = true;
        }
        console.log("auth required, redirect to login")
        next({name: "Login"})
    } else {
        next();
    }
}
