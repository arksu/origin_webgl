<template>

</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../store/game";

/**
 * песочные часы - индикатор прогресса текущего действия
 */
export default defineComponent({
  name: "ActionHourGlass",
  setup() {
    const store = useGameStore()

    return {}
  }
})
</script>
<style scoped>

</style>
