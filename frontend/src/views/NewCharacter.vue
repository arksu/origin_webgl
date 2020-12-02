<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        <form @submit="submit" action="#">

          <div class="error-message" v-if="errorText != null">
            {{ errorText }}
          </div>
          Create new character

          <input v-focus type="text" placeholder="Name" required v-model="name">
          <input type="submit" value="create" :disabled="isProcessing" class="login-button">

        </form>
      </div>

    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";

export default defineComponent({
  name: "NewCharacter",
  data() {
    return {
      name: null as string | null,
      errorText: null as string | null,
      isProcessing: false as boolean
    }
  },
  methods: {
    submit: function (e: Event) {
      // не дадим отработать стандартному обработчику
      e.preventDefault();
      console.log("create")
    }
  }
});
</script>

<style scoped>

</style>