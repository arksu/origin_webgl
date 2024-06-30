<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
// import {useMainStore} from "../store/main";

export default defineComponent({
  setup() {
    const authStore = useAuthStore()
    // const store = useMainStore();

    // return {error: computed(() => store.lastError)};

    return { authStore }
  }
})
</script>

<template>
  <div class="error-message" v-if="authStore.lastError">
    {{ authStore.lastError }}
  </div>
</template>

<style scoped>
</style>
