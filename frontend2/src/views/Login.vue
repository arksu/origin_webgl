<template>

  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message/>

          <login-field v-model="login"/>
          <password-field v-model="password"/>
          <submit-button :disabled="false"/>

          <div class="signup-link">
            Not a member?
            <router-link :to="{ name: 'SignUp'}">Signup now</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {ref, defineComponent} from 'vue'

import LoginField from "../components/LoginField.vue";
import PasswordField from "../components/PasswordField.vue"
import SubmitButton from "../components/SubmitButton.vue";
import ErrorMessage from "../components/ErrorMessage.vue";
import Logo from "../components/Logo.vue";
import {makeHash} from "../utils/passwordHash";
import {useMainStore} from "../store";

export default defineComponent({
  name: "Login",
  components: {Logo, ErrorMessage, LoginField, PasswordField, SubmitButton},
  setup() {
    const store = useMainStore()

    const login = ref('');
    const password = ref('');

    const submit = () => {
      // запомним что ввели в поля ввода
      localStorage.setItem("login", login.value || "");
      localStorage.setItem("password", password.value || "");

      const hash = makeHash(password.value);
      store.lastError = null
      store.ssid = null

      // login.value = ''
      // password.value = ''
    }

    return {login, password, submit}
  }
})
</script>

<style scoped>


</style>
