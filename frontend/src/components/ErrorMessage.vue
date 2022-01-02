<template>
  <div class="error-message" v-if="error != null">
    {{ error }}
  </div>
</template>

<script lang="ts">
import {computed, defineComponent} from "vue";
import {useMainStore} from "../store/main";

export default defineComponent({
  setup() {
    const store = useMainStore();

    return {error: computed(() => store.lastError)};
  },
});
</script>

<style scoped>
</style>
