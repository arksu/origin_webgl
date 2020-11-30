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
          <input type="text" placeholder="Email (optional)" v-model="email">
          <input type="password" placeholder="Password" required v-model="password">
          <br>
          <input type="submit" value="register" :disabled="isProcessing">
          <div class="signup-link">
            Already have an account?
            <router-link :to=" { name: 'Login' }">Sign in</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";

export default defineComponent({
  name: "Signup",
  data() {
    return {
      login: null as string | null,
      email: null as string | null,
      password: null as string | null,
      errorText: null as string | null,
      isProcessing: false as boolean
    }
  },
  methods: {
    submit: function (e: Event) {
      e.preventDefault();

      // запомним что ввели в поля ввода
      localStorage.setItem("login", this.login || "");
      localStorage.setItem("password", this.password || "");

      this.signupImpl();
    },
    /**
     * авторизация на сервере
     */
    signupImpl: function () {
      this.isProcessing = true;
      this.errorText = null;
      console.log("sign up " + this.login);

      const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          login: this.login,
          email: this.email,
          password: this.password
        })
      };

      fetch(Net.apiUrl + "/api/signup", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.error !== undefined) {
                this.errorText = data.error;
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
    email: function () {
      this.errorText = null;
    },
    password: function () {
      this.errorText = null;
    }
  },
});

</script>