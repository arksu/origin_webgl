<template>
  <input v-focus id="login" type="text" placeholder="Login" required v-model="value">
</template>

<script lang="ts">
import {computed, defineComponent} from 'vue'

export default defineComponent({
  name: "LoginField",
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
