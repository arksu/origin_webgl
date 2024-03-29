import "./scss/main.scss"
import {createApp} from "vue";
import {createPinia} from 'pinia'
import router from "./router";

import App from "./App.vue";
import {padMixin} from "./utils/mixins";

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mixin(padMixin)

// добавим свою кастомную директиву для автофокуса input (v-focus)
// используем в форме логина и регистрации
app.directive('focus', {
    mounted(el) {
        el.focus();
    }
})
app.mount("#app")


