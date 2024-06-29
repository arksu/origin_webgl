<script lang="ts">
import {defineComponent, ref} from 'vue'

import LoginField from "../components/LoginField.vue";
import PasswordField from "../components/PasswordField.vue"
import SubmitButton from "../components/SubmitButton.vue";
import ErrorMessage from "../components/ErrorMessage.vue";
import Logo from "@/components/Logo.vue";
import {makeHash} from "@/util/passwordHash";
// import {useMainStore} from "../store/main";
// import {useApi} from "../composition/useApi";

export default defineComponent({
  name: "Login",
  components: {Logo, ErrorMessage, LoginField, PasswordField, SubmitButton},
  setup() {
    // const store = useMainStore()

    const login = ref(localStorage.getItem('login') || '');
    const password = ref('');

    const savedHash = localStorage.getItem('hash') || ''

    const request = {
      login: '',
      hash: ''
    }

    const submit = async () => {


      request.login = login.value
      // const hash = (savedHash && !store.wasAutoLogin) ? savedHash : makeHash(password.value);
      // request.hash = hash;

    }

    return {login, password, submit}
  }
})
</script>

<template>
  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message/>

          <login-field v-model="login"/>
          <password-field v-model="password"/>
          <!--          <submit-button :loading="isLoading" caption="login"/>-->

          <div class="signup-link">
            Not a member?
<!--            <router-link :to="{ name: 'SIGN_UP'}">Signup now</router-link>-->
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
