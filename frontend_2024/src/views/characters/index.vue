<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue'
import SubmitButton from '@/components/SubmitButton.vue'
import Spinner from '@/components/Spinner.vue'
import { useApi } from '@/net/useApi'
import { useAuthStore } from '@/stores/authStore'
import Row from '@/views/characters/Row.vue'

type CharacterResponseDTO = {
  id: number,
  name: string,
}

export default defineComponent({
  name: 'CharactersList',
  components: { Row, SubmitButton, Spinner },
  setup() {
    const authStore = useAuthStore()
    const list = ref<CharacterResponseDTO[]>([])
    const busy = ref(false)

    const { isLoading, isSuccess, fetch } = useApi('character', {
      method: 'GET'
    })

    const onSelect = () => {
      busy.value = true
    }

    const onEnter = () => {
      busy.value = false
    }

    const deleteItem = (id: number) => {
      // оставляем только те что НЕ с удаляемым ид
      list.value = list.value.filter(c => c.id != id)
      const len = 5 - list.value.length
      // заполним пустые места слотами для создания перса
      for (let i = 0; i < len; i++) {
        list.value.push({ id: 0, name: 'Create New' })
      }
    }

    onMounted(async () => {
      const response = await fetch()
      if (isSuccess.value) {
        list.value = response.value.list
        const len = 5 - list.value.length
        for (let i = 0; i < len; i++) {
          list.value.push({ id: 0, name: 'Create New' })
        }
      }
    })

    const logout = () => {
      authStore.logout()
    }

    return { isLoading, list, logout, deleteItem, busy, onSelect, onEnter }
  }
})
</script>

<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        Characters<br>

        <spinner v-if="isLoading" />
        <row v-else v-for="c in list" :key="c.id" :id="c.id" :name="c.name" :busy="busy" @onDeleted="deleteItem"
             @onSelect="onSelect" @onEnter="onEnter"></row>

        <submit-button caption="logout" @click="logout"></submit-button>
      </div>
    </div>
  </div>
</template>
