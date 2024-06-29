<script lang="ts">
import {defineComponent} from 'vue'

export default defineComponent({
  name: "Logo"
})
</script>

<template>
  <div class="logo-container">
    <img src="../../assets/img/logo.png" alt="logo">
  </div>
</template>

<style scoped lang="scss">

.logo-container {
  line-height: 55px;
  text-align: center;
}

img {
  display: inline;
  max-width: 100%;
  max-height: 100%;
  vertical-align: middle;
}

</style>
