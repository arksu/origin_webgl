<template>
  <div class="padding-all">
    <div class="form-container">

      <div class="login-panel">
        Characters
        <row v-for="c in list" :name="c"></row>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'
import Row from "./Row.vue";

export default defineComponent({
  name: "CharactersList",
  components: {Row},
  setup() {
    const list = ref(["sdf", "sdfdd"])

    return {list}
  }
})
</script>

<style scoped>

</style>