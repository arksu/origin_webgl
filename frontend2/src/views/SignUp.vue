<template>
  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message/>

          <login-field v-model="login"/>
          <email-field v-model="email"/>
          <password-field v-model="password"/>
          <submit-button :loading="isLoading" caption="create account"/>

          <div class="signup-link">
            Already have an account?
            <router-link :to="{ name: 'LOGIN'}">Log in</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'
import Logo from "../components/Logo.vue";
import ErrorMessage from "../components/ErrorMessage.vue";
import LoginField from "../components/LoginField.vue";
import PasswordField from "../components/PasswordField.vue";
import EmailField from "../components/EmailField.vue";
import SubmitButton from "../components/SubmitButton.vue";
import {useMainStore} from "../store";
import {useApi} from "../composition/useApi";
import {makeHash} from "../utils/passwordHash";

export default defineComponent({
  name: "SignUp",
  components: {Logo, ErrorMessage, LoginField, PasswordField, EmailField, SubmitButton},
  setup() {
    const store = useMainStore()

    const login = ref('');
    const password = ref('');
    const email = ref('');

    const request = {
      login: '',
      email: '',
      password: ''
    }

    const {isLoading, data, isSuccess, fetch} = useApi("signup", {
      method: "POST",
      authorized: false,
      logoutOnError: false,
      data: request
    })

    const submit = async () => {
      store.ssid = null
      store.lastError = null

      request.login = login.value
      request.email = email.value
      request.password = password.value
      await fetch()

      if (isSuccess.value) {
        store.onSuccessLogin(data.value.ssid)

        // запомним что ввели в поля ввода в локалсторадже
        localStorage.setItem("login", login.value || "")
        localStorage.setItem("hash", makeHash(password.value))
      }
    }

    return {
      login, password, email, submit, isLoading
    }
  }
})
</script>

<style scoped>

</style>
