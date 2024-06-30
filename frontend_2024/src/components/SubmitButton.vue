<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "SubmitButton",
  props: {
    caption: {
      type: String,
      required: true
    },
    loading: {
      type: Boolean,
      required: false,
      default: false
    }
  },
  emits: ['onClick'],
  setup() {
  }
})
</script>

<template>
  <button type="submit" class="login-button" v-bind:class="{'login-button-loading': loading}" v-bind:disabled="loading"
          @click="$emit('onClick')">
    <span class="button__text">{{ caption }}</span>
  </button>
</template>

<style scoped>

</style>
