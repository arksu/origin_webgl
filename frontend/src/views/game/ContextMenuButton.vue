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
  padding: 5px;
  color: #A8B087;
  border: 2px solid #9F935D;
  border-radius: 5px;
  background-color: #363E19;
}
</style>