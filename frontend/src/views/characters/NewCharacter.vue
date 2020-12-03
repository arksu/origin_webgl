<!--suppress HtmlFormInputWithoutLabel -->
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
import Net from "@/net/Net";
import Client from "@/net/Client";

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
      if (!Client.instance.ssid) {
        this.errorText = "no ssid"
        return;
      }


      this.isProcessing = true;
      this.errorText = null;

      const requestOptions = {
        method: 'POST',
        headers: {'Content-Type': 'application/json', 'Authorization': Client.instance.ssid},
        body: JSON.stringify({
          name: this.name,
        })
      };

      fetch(Net.apiUrl + "/api/characters", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.error !== undefined) {
                this.errorText = data.error;
              } else if (data.ssid !== undefined) {
                Client.instance.ssid = data.ssid;
                await router.push({name: "Game"})
              }
            } else {
              const error = "error " + response.status + " " + (await response.text() || response.statusText);
              this.errorText = error;
              console.warn(error)
            }
          })
          .catch(error => {
            this.errorText = error.message || error;
            console.error('There was an error!', error);
          })
          .finally(() => {
            this.isProcessing = false;
          });
    },
    back() {
      console.log("back")
      router.push({name: "Characters"})
    }
  }
});
</script>

<style scoped>

</style>