<template>
  <input type="submit" value="login" v-bind:disabled="disabled" class="login-button">
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "SubmitButton",
  props: {
    disabled: {
      type: Boolean,
      required: true
    }
  }
})
</script>

<style scoped>

</style>
