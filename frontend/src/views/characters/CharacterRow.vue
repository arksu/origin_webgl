<template>
  <div style="width: 100%; display: table; margin: 10px 0">
    <div :class="getClass()" :onclick="select">
      {{ name }} {{ id !== 0 ? "[id " + id + "]" : "" }}
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

export default defineComponent({
  name: "CharacterRow",
  props: {
    id: Number,
    name: String
  },
  emits: ['deleted'],
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
        Client.instance.selectedCharacterId = this.id || 0;
        router.push({name: 'Game'})
      }
    },
    deleteChar(e: Event) {
      e.preventDefault()
      console.log("delete " + this.id)

      if (!confirm("Are you sure to delete this character: " + this.name)) return;

      if (!this.$store.getters.isLogged) {
        Client.instance.networkError("Auth required")
        return;
      }

      const requestOptions = {
        method: 'DELETE',
        headers: {'Authorization': this.$store.getters.ssid!!}
      };

      fetch(Client.apiUrl + "/api/characters/" + this.id, requestOptions)
          .then(async response => {
            console.log(response)
            if (response.ok) {
              const data = await response.text()
              if (data === "ok") {
                console.log("emit")
                this.$emit('deleted')
              } else {
                Client.instance.networkError("failed delete character");
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
    }
  }
});
</script>

<style scoped>

</style>