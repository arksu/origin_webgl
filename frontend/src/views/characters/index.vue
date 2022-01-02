<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        Characters<br>

        <spinner v-if="isLoading"/>
        <row v-else v-for="c in list" :id="c.id" :name="c.name" :busy="busy" @onDeleted="deleteItem" @onSelect="onSelect" @onEnter="onEnter"></row>

        <submit-button caption="logout" :onClick="logout"></submit-button>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, onMounted, ref} from 'vue'
import Row from "./Row.vue";
import SubmitButton from "../../components/SubmitButton.vue";
import Test from "../Test.vue";
import Spinner from "../../components/Spinner.vue";
import {useMainStore} from "../../store/main";
import {useApi} from "../../composition/useApi";

type CharactersResponse = {
  id: number,
  name: string,
}

export default defineComponent({
  name: "CharactersList",
  components: {Row, SubmitButton, Test, Spinner},
  setup() {
    const store = useMainStore()
    const list = ref<CharactersResponse[]>([])
    const busy = ref(false)

    const onSelect = () => {
      busy.value = true
    }

    const onEnter = () => {
      busy.value = false
    }

    const {isLoading, data, fetch} = useApi("characters", {
      method: "GET",
    })

    const deleteItem = (id: number) => {
      list.value = list.value.filter(c => c.id != id)
      const len = 5 - list.value.length
      for (let i = 0; i < len; i++) {
        list.value.push({id: 0, name: 'Create New'})
      }
    }

    onMounted(async () => {
      await fetch()
      list.value = data.value.list
      const len = 5 - list.value.length
      for (let i = 0; i < len; i++) {
        list.value.push({id: 0, name: 'Create New'})
      }
    })

    const logout = () => {
      store.logout()
    }

    return {list, isLoading, logout, deleteItem, busy, onSelect, onEnter}
  }
})
</script>
