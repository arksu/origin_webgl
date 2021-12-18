<template>
  <input v-focus id="name" type="text" placeholder="Name" required v-model="name">
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'

export default defineComponent({
  setup() {
    const name = ref('')

    return {
      name
    }
  }
})
</script>

<style scoped>

</style>