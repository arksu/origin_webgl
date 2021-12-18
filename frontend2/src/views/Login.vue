<template>
  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message/>

          <login-field v-model="login"/>
          <password-field v-model="password"/>
          <submit-button :loading="isLoading" caption="login"/>

          <div class="signup-link">
            Not a member?
            <router-link :to="{ name: 'SIGN_UP'}">Signup now</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'

import LoginField from "../components/LoginField.vue";
import PasswordField from "../components/PasswordField.vue"
import SubmitButton from "../components/SubmitButton.vue";
import ErrorMessage from "../components/ErrorMessage.vue";
import Logo from "../components/Logo.vue";
import {makeHash} from "../utils/passwordHash";
import {useMainStore} from "../store";
import {useApi} from "../composition/useApi";

export default defineComponent({
  name: "Login",
  components: {Logo, ErrorMessage, LoginField, PasswordField, SubmitButton},
  setup() {
    const store = useMainStore()

    const login = ref(localStorage.getItem('login') || '');
    const password = ref('');

    const savedHash = localStorage.getItem('hash') || ''

    const request = {
      login: '',
      hash: ''
    }

    const {isLoading, data, isSuccess, fetch} = useApi("login", {
      method: "POST",
      authorized: false,
      logoutOnError: false,
      data: request
    })

    const submit = async () => {
      store.ssid = null
      store.lastError = null

      request.login = login.value
      const hash = savedHash || makeHash(password.value);
      request.hash = hash;
      await fetch()

      if (isSuccess.value) {
        store.onSuccessLogin(data.value.ssid)

        // запомним что ввели в поля ввода в локалсторадже
        localStorage.setItem("login", login.value || "")
        localStorage.setItem("hash", hash)
      }
    }

    // TODO: only for dev
    if (login.value && savedHash && !store.wasAutoLogin && !store.lastError) {
      store.wasAutoLogin = true
      submit()
    }

    return {login, password, submit, isLoading}
  }
})
</script>

<style scoped>


</style>
