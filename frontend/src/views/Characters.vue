<template>
  <div class="padding-all">
    <div class="form-container">
      ok
      <ul>
        <li v-for="c in list">
          {{ c.id }} - {{ c.name }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue";
import Net from "@/net/Net";
import Client from "@/net/Client";

type CharacterResponse = {
  id: number
  name: string
}
type Response = {
  list: Array<CharacterResponse>
}

export default defineComponent({
  name: "Characters",
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
    }
  },
  mounted() {
    this.getList();
  }
});
</script>

<style scoped>

</style>