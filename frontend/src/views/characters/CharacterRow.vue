<template>
  <div style="width: 100%; display: table; margin: 10px 0">
    <div :class="getClass()" :onclick="select">
      {{ name }} {{ id !== 0 ? "id (" + id + ")" : "" }}
    </div>
    <div v-if="id !== 0" class="character-row delete-char" :onclick="deleteChar">
      <i class="fas fa-trash-alt"></i>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import router from "@/router";
import Client from "@/net/Client";
import Net from "@/net/Net";

export default defineComponent({
  name: "CharacterRow",
  props: {
    id: Number,
    name: String
  },
  data() {
    return {
      isProcessing: false as boolean
    }
  },
  methods: {
    getClass: function (): string {
      return this.id == 0 ? "character-row new-char" : "character-row";
    },
    select(e: Event) {
      e.preventDefault()
      if (this.id == 0) {
        console.log("create new char")
        router.push({name: 'NewCharacter'})
      } else {
        console.log("selected " + this.name)
      }
    },
    deleteChar(e: Event) {
      e.preventDefault()
      console.log("delete " + this.id)

      if (!Client.instance.ssid) {
        return;
      }

      const requestOptions = {
        method: 'DELETE',
        headers: {'Authorization': Client.instance.ssid}
      };

      fetch(Net.apiUrl + "/api/characters/" + this.id, requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.ssid !== undefined) {
                Client.instance.ssid = data.ssid;
                await router.push({name: "Game"})
              }
            } else {
              const error = "error " + response.status + " " + (await response.text() || response.statusText);
              Client.instance.networkError(error);
              console.warn(error)
            }
          })
          .catch(error => {
            Client.instance.networkError(error.message || error);
            console.error('There was an error!', error);
          })
          .finally(() => {
            this.isProcessing = false;
          });
    }
  }
});
</script>

<style scoped>

</style>