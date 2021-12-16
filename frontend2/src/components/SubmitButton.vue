<template>
  <input type="submit" value="login" :disabled="isProcessing" class="login-button">
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "SubmitButton"
})
</script>

<style scoped>

</style>
