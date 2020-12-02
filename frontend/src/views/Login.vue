<!--suppress HtmlFormInputWithoutLabel -->
<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="logo-container">
        <img src="assets/logo.png" alt="logo">
      </div>
      <div class="login-panel">
        <form @submit="submit" action="#">
          <div class="error-message" v-if="errorText != null">
            {{ errorText }}
          </div>
          <input v-focus type="text" placeholder="Login" required v-model="login">
          <input type="password" placeholder="Password" required v-model="password">
          <br>
          <input type="submit" value="login" :disabled="isProcessing" class="login-button">
          <div class="signup-link">
            Not a member?
            <router-link :to="{ name: 'Signup'}">Signup now</router-link>
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
import {hexToBase64, log2} from "@/utils/Util";
import {syncScrypt} from "scrypt-js"
import router from "@/router";

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
      // не дадим отработать стандартному обработчику
      e.preventDefault();

      // запомним что ввели в поля ввода
      localStorage.setItem("login", this.login || "");
      localStorage.setItem("password", this.password || "");

      // запустим процедуру логина
      this.loginImpl();
    },
    makeHash: function (): string {
      console.log("loginImpl " + this.login);

      let enc = new TextEncoder();
      let passwordArray = enc.encode(this.password!!);

      // формируем scrypt hash
      const N = 2048, r = 8, p = 1;
      const dkLen = 32;

      // генерим случайную соль каждый раз
      let saltBuffer = new Uint8Array(16);
      window.crypto.getRandomValues(saltBuffer);

      let saltHex: string = Array.prototype.map.call(
          saltBuffer,
          x => ('00' + x.toString(16)).slice(-2)
      ).join('');

      let hashHex = Array.prototype.map.call(
          syncScrypt(passwordArray, saltBuffer, N, r, p, dkLen),
          x => ('00' + x.toString(16)).slice(-2)
      ).join('');

      // собственно сам хэш
      // let hashHex = scrypt(this.password!!, saltBuffer, N, r, p, dkLen).toString('hex');

      let params: any = log2(N) << 16 | r << 8 | p;
      params = params.toString(16);
      let hash = '$s0$' + params + '$' + hexToBase64(saltHex) + '$' + hexToBase64(hashHex);
      console.log("password hash: " + hash)
      return hash;
    },
    /**
     * авторизация на сервере
     */
    loginImpl: function () {
      // взведем флаг попытки входа
      Client.instance.wasLoginTry = true;

      this.isProcessing = true;
      this.errorText = null;

      let hash = this.makeHash();

      const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          login: this.login,
          hash: hash
        })
      };

      fetch(Net.apiUrl + "/api/login", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.error !== undefined) {
                this.errorText = data.error;
              } else if (data.ssid !== undefined) {
                Client.instance.ssid = data.ssid;
                await router.push({name: "Characters"})
              }
              console.log(data)
            } else {
              const error = "error " + response.status + " " + (await response.text() || response.statusText);
              this.errorText = error;
              console.warn(error)
            }
          })
          .catch(error => {
            this.errorText = error.message || error;
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