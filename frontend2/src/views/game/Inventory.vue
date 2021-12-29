<template>

</template>

<script lang="ts">
import {defineComponent} from 'vue'
import {useGameStore} from "../../store/game";

export default defineComponent({
  name: "Inventory",
  setup() {
    const store = useGameStore()

    return {}
  }
})
</script>
<style scoped>

</style>
