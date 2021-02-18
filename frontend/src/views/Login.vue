<!--suppress HtmlFormInputWithoutLabel -->
<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="logo-container">
        <img src="assets/logo.png" alt="logo">
      </div>
      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <div class="error-message" v-if="$store.state.lastError !== undefined">
            {{ $store.state.lastError }}
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
import {hexToBase64, log2} from "@/utils/Util";
import {syncScrypt} from "scrypt-js"
import {ActionTypes} from "@/store/action-types";
import {MutationTypes} from "@/store/mutation-types";

export default defineComponent({
  name: "Login",
  data() {
    return {
      login: null as string | null,
      password: null as string | null,
      isProcessing: false as boolean,
      submitPressed: false as boolean
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
      this.submitPressed = true;

      this.isProcessing = true;
      this.$store.commit(MutationTypes.SET_LAST_ERROR, undefined)

      let hash = this.makeHash();

      const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          login: this.login,
          hash: hash
        })
      };

      fetch(Client.apiUrl + "/api/login", requestOptions)
          .then(async response => {
            console.log(response)
            if (response.ok) {
              const data = await response.json()
              if (data.ssid !== undefined) {
                this.$store.dispatch(ActionTypes.SUCCESS_LOGIN, data.ssid)
              } else {
                this.$store.commit(MutationTypes.SET_LAST_ERROR, "no ssid in response")
              }
            } else {
              const responseText = await response.text()
              const error = "error " + response.status + " " + (responseText.length < 64 ? responseText : response.statusText);
              this.$store.commit(MutationTypes.SET_LAST_ERROR, error)
              console.warn(error)
            }
          })
          .catch(error => {
            this.$store.commit(MutationTypes.SET_LAST_ERROR, error.message || error)
            console.error('error', error);
            console.log(error.message)
          })
          .finally(() => {
            this.isProcessing = false;
          });
    }
  },
  watch: {
    login: function () {
      if (this.submitPressed) this.$store.commit(MutationTypes.SET_LAST_ERROR, undefined)
    },
    password: function () {
      if (this.submitPressed) this.$store.commit(MutationTypes.SET_LAST_ERROR, undefined)
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
        return;
      }
    }
  }
});

</script>