<template>
  <h2>Login</h2>
  <form id="login-form" @submit="submit">
    <label>
      Login
      <input type="text">
    </label>
    <label>
      Password
      <input type="password">
    </label>
    <input type="submit" value="Subm">
  </form>
</template>

<script>
import {defineComponent} from "vue";

export default defineComponent({
  name: "Login",
  methods: {
    submit: function (e: Event) {

      e.preventDefault();
    }
  }
});

</script>

<style scoped>

</style>