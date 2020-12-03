<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        Characters
        <char-row v-for="c in list" :id="c.id" :name="c.name"  @deleted="getList"/>
        <input type="button" value="logout" :disabled="isProcessing" class="login-button padding-button"
               :onclick="exit">

      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import Client from "@/net/Client";
import CharacterRow from "@/views/characters/CharacterRow.vue";

type CharacterResponse = {
  id: number
  name: string
}
type Response = {
  list: Array<CharacterResponse>
}

export default defineComponent({
  name: "Characters",
  components: {
    'char-row': CharacterRow
  },
  data() {
    return {
      list: null as Array<CharacterResponse> | null,
      isProcessing: false as boolean
    }
  },
  methods: {
    getList: function () {
      if (!Client.instance.ssid) {
        Client.instance.networkError("Auth required")
        return;
      }

      const requestOptions = {
        method: 'GET',
        headers: {'Content-Type': 'application/json', 'Authorization': Client.instance.ssid}
      };

      fetch(Net.apiUrl + "/api/characters", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.list !== undefined) {
                this.list = data.list;
                const e = 5 - this.list!!.length
                for (let i = 0; i < e; i++) {
                  this.list!!.push({id: 0, name: 'Create New'})
                }
              } else {
                Client.instance.networkError("no characters list");
              }
            } else {
              Client.instance.networkError("error " + response.status + " " + (await response.text() || response.statusText));
            }
          })
          .catch(error => {
            Client.instance.networkError(error.message || error);
          })
          .finally(() => {
            this.isProcessing = false;
          });
    },
    exit: function () {
      Client.instance.logout();
    }
  },
  mounted() {
    this.getList();
  }
});
</script>

<style scoped>

</style>