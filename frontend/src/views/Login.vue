<template>
  <div class="padding-all">
    <!--    <div class="header">-->
    <!--      <h1>Login</h1>-->
    <!--    </div>-->
    <div class="design-w3l">
      <div class="logo-container">
        <img src="assets/logo.png" alt="logo">
      </div>
      <div class="mail-form-agile">
        <form @submit="submit" action="#">
          <input type="text" placeholder="Login" required>
          <input type="password" placeholder="Password" class="padding" required>
          <br>
          <input type="submit" value="login">
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Login",
  methods: {
    submit: function (e: Event) {
      console.log("submitted")
      e.preventDefault();
    }
  }
});

</script>

<style scoped>

</style>