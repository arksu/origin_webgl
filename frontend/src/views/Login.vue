<!--suppress HtmlFormInputWithoutLabel -->
<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="logo-container">
        <img src="assets/logo.png" alt="logo">
      </div>
      <div class="login-form">
        <form @submit="submit" action="#">
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
      console.log("loginImpl " + this.login);

      const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          login: this.login,
          password: this.password
        })
      };

      fetch(Net.apiUrl + "/login", requestOptions)
          .then(async response => {
            const data = await response.text()
            if (!response.ok) {
              const error = data || response.status;

              console.warn(error)
            } else {
              console.log(data)
            }
          })
          .catch(error => {
            console.error('There was an error!', error);
          });

      /*
      let proto = "https:" === window.location.protocol ? "wss" : "ws";
      let net = new Net(proto + "://" + window.location.hostname + ":8010/ws");
      console.log("Net url: " + net.url);

      Net.instance = net;

      net.onDisconnect = () => {
        console.log("net disconnected");
      };


      Net.instance.remoteCall("login", {
        login : this.login,
        password: this.password
      })
          .then((d) => {
            console.log("successLogin")
          })
          .catch((e) => {
            console.error(e);

            let loginBtn: HTMLButtonElement = (<HTMLButtonElement>document.getElementById("login-btn"));
            loginBtn.disabled = false;

            console.log("showLoginError")
          });

       */
    }
  },
  mounted() {
    // сразу заполним поля ввода тем что сохранили ранее
    this.login = localStorage.getItem("login");
    this.password = localStorage.getItem("password");

    // если это самый первый запуск - сразу попробуем авторизоваться на сервере
    if (!Client.instance.wasLoginTry) {
      this.loginImpl();
    }

    // взведем флаг попытки входа
    Client.instance.wasLoginTry = true;
  }
});

</script>