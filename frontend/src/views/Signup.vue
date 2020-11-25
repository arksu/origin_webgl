<!--suppress HtmlFormInputWithoutLabel -->
<template>
  <div class="padding-all">
    <!--    <div class="header">-->
    <!--      <h1>Login</h1>-->
    <!--    </div>-->
    <div class="form-container">
      <div class="logo-container">
        <img src="assets/logo.png" alt="logo">
      </div>
      <div class="login-form">
        <form @submit="submit" action="#">
          <input v-focus type="text" placeholder="Login" required autofocus>
          <input type="text" placeholder="Email (optional)" class="">
          <input type="password" placeholder="Password" required>
          <br>
          <input type="submit" value="register">
          <div class="signup-link">
            Already have an account?
            <router-link to="/login">Sign in</router-link>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "Signup",
  methods: {
    submit: function (e: Event) {
      console.log("submitted")
      e.preventDefault();
    }
  },
});

</script>

<style scoped>

</style>