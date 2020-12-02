<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        <div class="error-message" v-if="errorText != null">
          {{ errorText }}
        </div>
        Characters
        <char-row v-for="c in list" :id="c.id" :name="c.name"/>
        <input type="button" value="logout" :disabled="isProcessing" class="login-button" :onclick="exit">

      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import Client from "@/net/Client";
import CharacterRow from "@/views/CharacterRow.vue";
import router from "@/router";

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
      errorText: null as string | null,
      isProcessing: false as boolean
    }
  },
  methods: {
    getList: function () {
      if (!Client.instance.ssid) return;

      const requestOptions = {
        method: 'GET',
        headers: {'Content-Type': 'application/json', 'Authorization': Client.instance.ssid}
      };

      fetch(Net.apiUrl + "/api/characters", requestOptions)
          .then(async response => {
            if (response.ok) {
              const data = await response.json()
              if (data.error !== undefined) {
                this.errorText = data.error;
              } else if (data.list !== undefined) {
                this.list = data.list;
                const e = 5 - this.list!!.length
                for (let i = 0; i < e; i++) {
                  this.list!!.push({id: 0, name: 'Create New'})
                }
              } else {

              }
              console.log(data)
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
    exit: function () {
      Client.instance.ssid = undefined;
      router.push({name: "Login"})
    }
  },
  mounted() {
    this.getList();
  }
});
</script>

<style scoped>

</style>