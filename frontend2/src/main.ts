import "./scss/main.scss"
import {createApp} from "vue";

import App from "./App.vue";
import router from "./router";

createApp(App)
    // добавим свою кастомную директиву для автофокуса input (v-focus)
    // используем в форме логина и регистрации
    .directive('focus', {
        mounted(el) {
            el.focus();
        }
    })
    .use(router)
    .mount("#app")


