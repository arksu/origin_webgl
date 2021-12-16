<template>
  <login-field></login-field>
</template>

<script lang="ts">
import {defineComponent} from 'vue'

import LoginField from "../components/LoginField.vue"

export default defineComponent({
  name: "Login",
  components: {LoginField}
})
</script>

<style scoped>

</style>
