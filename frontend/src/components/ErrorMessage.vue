<script lang="ts">
import { defineComponent } from 'vue'
import { useAuthStore } from '@/stores/authStore'

export default defineComponent({
  setup() {
    const authStore = useAuthStore()

    return { authStore }
  }
})
</script>

<template>
  <div class="error-message" v-if="authStore.lastError">
    {{ authStore.lastError.substring(0, 150) }}
  </div>
</template>

<style scoped>
</style>
