<script lang="ts">
import {computed, defineComponent} from 'vue'

export default defineComponent({
  name: "PasswordField",
  props: {
    modelValue: String
  },
  setup(props, {emit}) {
    const value = computed({
      get: () => props.modelValue,
      set: (value) => emit('update:modelValue', value)
    })

    return {
      value,
    }
  }
})

</script>

<template>
  <input type="password" id="password" placeholder="Password" required autocomplete="current-password" v-model="value">
</template>

<style scoped>

</style>
