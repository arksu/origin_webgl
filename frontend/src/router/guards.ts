import {NavigationGuardNext, RouteLocationNormalized} from "vue-router";
import {useMainStore} from "../store/main";
import {RouteNames} from "./routeNames";

export default function guards(from: RouteLocationNormalized, to: RouteLocationNormalized, next: NavigationGuardNext) {
    const store = useMainStore()

    console.warn("route ", from.name, " => ", to.name);
    // всегда даем переход на "о нас"
    if (to.name == RouteNames.ABOUT || to.name == 'test') {
        next();
    }
    // всегда даем зарегистрироваться
    else if (to.name == RouteNames.SIGN_UP) {
        next();
    } else if (to.name == RouteNames.LOGIN) {
        next();
    } else if (to.name == RouteNames.GAME) {
        if (!store.isLogged) {
            console.log("not logged")
            next({name: RouteNames.LOGIN})
            // TODO
            // } else if (Client.instance.selectedCharacterId == undefined) {
            //     next({name: "Characters"})
        } else {
            next();
        }
    }
    // если не авторизованы надо перейти на логин форму
    else if (to.name !== RouteNames.LOGIN && !store.isLogged) {
        // это первый запуск?
        if (from.name == undefined) {
            // TODO
            // Client.instance.needAutologin = true;
        }
        console.log("auth required, redirect to login")
        next({name: RouteNames.LOGIN})
    } else {
        next();
    }
}
