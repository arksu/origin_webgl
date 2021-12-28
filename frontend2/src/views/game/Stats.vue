<template>
  <div>
    stamina: {{ store.CUR_STAMINA }}
  </div>
</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../store/game";

export default defineComponent({
  name: "Stats",
  setup() {
    const store = useGameStore()

    return {store}
  }
})
</script>

<style scoped>

</style>
