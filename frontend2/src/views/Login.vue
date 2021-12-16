<template>

  <div class="padding-all">
    <div class="form-container">

      <logo/>

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <login-field></login-field>

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

import LoginField from "../components/LoginField.vue"
import Logo from "../components/Logo.vue"

export default defineComponent({
  name: "Login",
  components: {LoginField, Logo}
})
</script>

<style scoped>


</style>
