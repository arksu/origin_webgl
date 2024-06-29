<script lang="ts">
import { computed, defineComponent, ref } from 'vue'
// import {useMainStore} from "../store/main";

export default defineComponent({
  setup() {
    // const store = useMainStore();

    // return {error: computed(() => store.lastError)};
    const error = ref('111')

    return {error}
  },
});
</script>

<template>
  <div class="error-message" v-if="error != null">
    {{ error }}
  </div>
</template>

<style scoped>
</style>
