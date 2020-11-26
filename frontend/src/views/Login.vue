<!--suppress HtmlFormInputWithoutLabel -->
<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="logo-container">
        <img src="assets/logo.png" alt="logo">
      </div>
      <div class="login-form">
        <form @submit="submit" action="#">
          <div class="error-message" v-if="errorText != null">
            {{ errorText }}
          </div>
          <input v-focus type="text" placeholder="Login" required v-model="login">
          <input type="password" placeholder="Password" required v-model="password">
          <br>
          <input type="submit" value="login" :disabled="isProcessing">
          <div class="signup-link">
            Not a member?
            <router-link to="/signup">Signup now</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Client from "@/net/Client";
import Net from "@/net/Net";

export default defineComponent({
  name: "Login",
  data() {
    return {
      login: null as string | null,
      password: null as string | null,
      errorText: null as string | null,
      isProcessing: false as boolean
    }
  },
  methods: {
    /**
     * обработка формы ввода
     */
    submit: function (e: Event) {
      console.log("submitted");
      // не дадим отработать стандартному обработчику
      e.preventDefault();

      // запомним что ввели в поля ввода
      localStorage.setItem("login", this.login || "");
      localStorage.setItem("password", this.password || "");

      // запустим процедуру логина
      this.loginImpl();
    },
    /**
     * авторизация на сервере
     */
    loginImpl: function () {
      // взведем флаг попытки входа
      Client.instance.wasLoginTry = true;

      this.isProcessing = true;
      this.errorText = null;
      console.log("loginImpl " + this.login);

      const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          login: this.login,
          hash: this.password
        })
      };

      fetch(Net.apiUrl + "/login", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.error !== undefined) {
                this.errorText = data.error;
              }
              console.log(data)
            } else {
              const error = "status: " + response.status + " " + (await response.text());
              this.errorText = error;
              console.warn(error)
            }
          })
          .catch(error => {
            this.errorText = error;
            console.error('There was an error!', error);
          })
          .finally(() => {
            this.isProcessing = false;
          });
    }
  },
  watch: {
    login: function () {
      this.errorText = null;
    },
    password: function () {
      this.errorText = null;
    }
  },
  mounted() {
    // сразу заполним поля ввода тем что сохранили ранее
    this.login = localStorage.getItem("login");
    this.password = localStorage.getItem("password");

    // если это самый первый запуск - сразу попробуем авторизоваться на сервере
    if (Client.instance.needAutologin && !Client.instance.wasLoginTry) {
      Client.instance.needAutologin = false;
      // также должны быть какие то данные в сохраненном логине и пароле
      if (this.login !== null && this.login.length > 0 && this.password !== null && this.password.length > 0) {
        this.loginImpl();
      }
    }
  }
});

</script>