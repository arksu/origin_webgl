<template>

  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <login-field/>
          <password-field/>

          <submit-button/>

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
import {defineComponent} from 'vue'

import LoginField from "../components/LoginField.vue";
import PasswordField from "../components/PasswordField.vue"
import SubmitButton from "../components/SubmitButton.vue";
import Logo from "../components/Logo.vue";

export default defineComponent({
  name: "Login",
  components: {Logo, LoginField, PasswordField, SubmitButton}
})
</script>

<style scoped>


</style>
