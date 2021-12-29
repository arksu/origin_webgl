<template>

</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../store/game";

/**
 * предмет который держим в руке (перетаскивание между инвентарями)
 */
export default defineComponent({
  name: "Hand",
  setup() {
    const store = useGameStore()

    return {}
  }
})
</script>
<style scoped>

</style>
