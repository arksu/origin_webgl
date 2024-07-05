<script lang="ts">
import { defineComponent, ref } from 'vue'
import SubmitButton from '@/components/SubmitButton.vue'
import { useApi } from '@/net/useApi'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'
import ErrorMessage from '@/components/ErrorMessage.vue'

export default defineComponent({
  components: { ErrorMessage, SubmitButton },
  setup() {
    const name = ref('')

    const back = () => {
      router.replace({ name: RouteNames.CHARACTERS })
    }

    const request = {
      name: ''
    }

    const { isLoading, isSuccess, fetch } = useApi('character', {
      method: 'POST',
      excludeErrorStatuses: [400],
      data: request
    })

    const submit = async () => {
      console.log('submit create new character')
      request.name = name.value
      await fetch()
      if (isSuccess.value) {
        await router.replace({ name: RouteNames.CHARACTERS })
      }
    }

    return {
      name, back, submit, isLoading
    }
  }
})
</script>

<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <error-message />

          <input v-focus id="name" type="text" placeholder="Name" required v-model="name">

          <button type="button" class="login-button padding" v-bind:disabled="isLoading" @click="back">back</button>
          <submit-button class="padding" :loading="isLoading" caption="create" />
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.padding {
  margin: 0 10px 0 10px;
}
</style>
