<!--suppress HtmlFormInputWithoutLabel -->
<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          Create new character
          <input v-focus type="text" placeholder="Name" required v-model="name">
          <input type="button" value="back" :disabled="isProcessing" class="login-button button-padding"
                 :onclick="back">
          <input type="submit" value="create" :disabled="isProcessing" class="login-button button-padding">
        </form>
      </div>

    </div>
  </div>
</template>s

<script lang="ts">
import {defineComponent} from "vue";
import router from "@/router";
import Client from "@/net/Client";

export default defineComponent({
  name: "NewCharacter",
  data() {
    return {
      name: null as string | null,
      isProcessing: false as boolean
    }
  },
  methods: {
    submit: function (e: Event) {
      // не дадим отработать стандартному обработчику
      e.preventDefault();

      if (!Client.instance.ssid) {
        Client.instance.networkError("Auth required")
        return;
      }

      this.isProcessing = true;

      const requestOptions = {
        method: 'PUT',
        headers: {'Content-Type': 'application/json', 'Authorization': Client.instance.ssid},
        body: JSON.stringify({
          name: this.name,
        })
      };

      fetch(Client.apiUrl + "/api/characters", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.name !== undefined) {
                await router.push({name: "Characters"})
              } else {
                Client.instance.networkError("failed create character");
              }
            } else {
              const responseText = await response.text()
              Client.instance.networkError("error " + response.status + " " + (responseText.length < 64 ? responseText : response.statusText));
            }
          })
          .catch(error => {
            Client.instance.networkError(error.message || error);
          })
          .finally(() => {
            this.isProcessing = false;
          });
    },
    back() {
      router.push({name: "Characters"})
    }
  }
});
</script>

<style scoped>

</style>