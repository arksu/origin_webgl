<template>
  <div class="spinner"></div>
  <br>
</template>

<script lang="ts">
export default {
  name: "Spinner"
}
</script>

<style scoped lang="scss">
.spinner {
  display: inline-block;
  width: 52px;
  height: 52px;
}

.spinner::after {
  content: " ";
  position: absolute;
  display: block;
  width: 48px;
  height: 48px;
  margin: 0;
  border: 4px solid;
  border-radius: 80%;
  border-color: #fff transparent #fff transparent;
  animation: button-loading-spinner 1.2s ease infinite;
}
</style>