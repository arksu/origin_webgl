<template>

  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message/>

          <login-field v-model="login"/>
          <password-field v-model="password"/>
          <submit-button :disabled="isLoading"/>

          <div class="signup-link">
            Not a member?
            <router-link :to="{ name: 'SignUp'}">Signup now</router-link>
          </div>

          isLoading : {{ isLoading }}<br>
          response:
          <pre>{{ response }}</pre>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue'

import LoginField from "../components/LoginField.vue";
import PasswordField from "../components/PasswordField.vue"
import SubmitButton from "../components/SubmitButton.vue";
import ErrorMessage from "../components/ErrorMessage.vue";
import Logo from "../components/Logo.vue";
import {makeHash} from "../utils/passwordHash";
import {useMainStore} from "../store";
import {useApi} from "../composition/useFetch";

export default defineComponent({
  name: "Login",
  components: {Logo, ErrorMessage, LoginField, PasswordField, SubmitButton},
  setup() {
    const store = useMainStore()

    const login = ref('');
    const password = ref('');

    const request = {
      login: '',
      hash: ''
    }

    const {isLoading, response, data, error, fetch} = useApi("login1", {
      method: "POST",
      skip: true,
      data: request
    })
    store.lastError = error

    const submit = async () => {

      const hash = makeHash(password.value);
      store.ssid = null;

      request.login = login.value
      request.hash = hash
      store.lastError = null
      await fetch()

      console.log("success")
      // запомним что ввели в поля ввода
      localStorage.setItem("login", login.value || "");
      localStorage.setItem("password", password.value || "");

      // login.value = ''
      // password.value = ''
    }

    onMounted(() => {
    })

    return {login, password, submit, isLoading, response}
  }
})
</script>

<style scoped>


</style>
