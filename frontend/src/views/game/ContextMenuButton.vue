<script setup lang="ts">
const props = defineProps<{
  caption: string
}>()

</script>

<template>
  <div class="context-menu-button">
    <p>{{caption}}</p>
  </div>
</template>

<style scoped lang="scss">
.context-menu-button {
  position: relative;
  left: 20px;
}
</style>