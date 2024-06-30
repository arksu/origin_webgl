<script lang="ts">
import { defineComponent, ref } from 'vue'
import Logo from '@/components/Logo.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import LoginField from '@/components/LoginField.vue'
import PasswordField from '@/components/PasswordField.vue'
import EmailField from '@/components/EmailField.vue'
import SubmitButton from '@/components/SubmitButton.vue'
import { useApi } from '@/composition/useApi'
import { useAuthStore } from '@/stores/authStore'
import { RouteNames } from '@/router/routeNames'

export default defineComponent({
  name: 'SignUp',
  computed: {
    RouteNames() {
      return RouteNames
    }
  },
  components: { Logo, ErrorMessage, LoginField, PasswordField, EmailField, SubmitButton },
  setup() {
    const authStore = useAuthStore()

    const login = ref('')
    const password = ref('')
    const email = ref('')

    const request = {
      login: '',
      email: '',
      password: ''
    }

    const { isLoading, fetch } = useApi('signup', {
      method: 'POST',
      onErrorRouteName: RouteNames.SIGN_UP,
      data: request
    })

    const submit = async () => {
      request.login = login.value
      request.email = email.value
      request.password = password.value

      const response = await fetch()

      localStorage.setItem('login', request.login)
      authStore.setToken(response.value.ssid)
    }

    return {
      login, password, email, submit, isLoading
    }
  }
})
</script>

<template>
  <div class="padding-all">
    <div class="form-container">

      <logo />

      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message />

          <login-field v-model="login" />
          <email-field v-model="email" />
          <password-field v-model="password" />
          <submit-button :loading="isLoading" caption="create account" />

          <div class="signup-link">
            Already have an account?
            <router-link :to="{ name: RouteNames.LOGIN}">Log in</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
