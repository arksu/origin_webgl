<template>
  <span class="tooltip-stat">
    <slot/>
  </span>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Tooltip",
})
</script>
