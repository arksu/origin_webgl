<template>
  <input type="text" id="email" placeholder="Email (optional)" v-model="value">
</template>

<script lang="ts">
import {computed, defineComponent} from 'vue'

export default defineComponent({
  name: "EmailField",
  props: {
    modelValue: String
  },
  setup(props, {emit}) {
    const value = computed({
      get: () => props.modelValue,
      set: (value) => {
        // if (value) {
        //   const re = /\S+@\S+\.\S+/;
        //   re.test(value)
        // }
        emit('update:modelValue', value)
      }
    })

    return {
      value,
    }
  }
})
</script>
<style scoped>

</style>
