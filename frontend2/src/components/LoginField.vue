<template>
  <input v-focus type="text" placeholder="Login" required v-model="login">
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "LoginField"
})
</script>

<style scoped>

</style>
