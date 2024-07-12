<script lang="ts">
import { defineComponent, onMounted, onUnmounted, ref } from 'vue'

import LoginField from '@/components/LoginField.vue'
import PasswordField from '@/components/PasswordField.vue'
import SubmitButton from '@/components/SubmitButton.vue'
import ErrorMessage from '@/components/ErrorMessage.vue'
import Logo from '@/components/Logo.vue'
import { makeHash } from '@/util/passwordHash'
import { useApi } from '@/net/api'
import { useAuthStore } from '@/stores/authStore'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'

export default defineComponent({
  name: 'Login',
  computed: {
    RouteNames() {
      return RouteNames
    }
  },
  components: { Logo, ErrorMessage, LoginField, PasswordField, SubmitButton },
  setup() {
    const authStore = useAuthStore()

    const login = ref(localStorage.getItem('login') || '')
    const password = ref('')
    let intervalId = 0;

    const request = {
      login: '',
      hash: ''
    }

    const { isLoading, isSuccess, fetch } = useApi('login', {
      method: 'POST',
      data: request
    })

    const submit = async () => {
      request.login = login.value
      request.hash = makeHash(password.value)

      // для разработчика всегда сохраняем пароль
      if (authStore.devMode) {
        localStorage.setItem('password', password.value)
        localStorage.removeItem('wasLogout')
      }

      const response = await fetch()

      if (isSuccess.value) {
        localStorage.setItem('login', request.login)
        authStore.setToken(response.value.ssid)
      }
    }

    onMounted(() => {
      if (authStore.token) {
        router.push({ name: RouteNames.CHARACTERS })
      } else {
        // для разработки сразу восстановим пароль
        if (authStore.devMode) {
          password.value = localStorage.getItem('password') || ''
          console.log('password.value', password.value)

          if (localStorage.getItem('wasLogout') !== '1') {
            intervalId = setInterval(() => {
              submit()
            }, 1000);
          }
        }
      }
    })

    onUnmounted(() => {
      clearInterval(intervalId)
    })

    return { login, password, submit, isLoading }
  }
})
</script>

<template>
  <div class="padding-all">
    <div class="form-container">

      <logo />

      <div class="login-panel">
        <form action="#" @submit.prevent="submit">
          <error-message />

          <login-field v-model="login" />
          <password-field v-model="password" />
          <submit-button :loading="isLoading" caption="login" />

          <div class="signup-link">
            Not a member?
            <router-link :to="{ name: RouteNames.SIGN_UP}">Signup now</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style>
