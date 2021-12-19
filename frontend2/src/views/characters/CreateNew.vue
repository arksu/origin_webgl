<template>
  <div class="padding-all">
    <div class="form-container">
      <div class="login-panel">
        <form @submit.prevent="submit" action="#">
          <input v-focus id="name" type="text" placeholder="Name" required v-model="name">

          <button type="button" class="login-button padding" v-bind:disabled="isLoading" :onclick="back">back</button>
          <submit-button class="padding" :loading="isLoading" caption="create"/>
        </form>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent, ref} from 'vue'
import SubmitButton from "../../components/SubmitButton.vue";
import {useApi} from "../../composition/useApi";
import router from "../../router";
import {RouteNames} from "../../router/routeNames";

export default defineComponent({
  components: {SubmitButton},
  setup() {
    const name = ref('')

    const back = () => {
      router.replace({name: RouteNames.CHARACTERS})
    }

    const request = {
      name: '',
    }

    const {isLoading, data, isSuccess, fetch} = useApi("characters", {
      method: "PUT",
      data: request
    })

    const submit = async () => {
      console.log('submit')
      request.name = name.value
      await fetch()
      if (isSuccess.value) {
        router.push({name: RouteNames.CHARACTERS})
      }
    }

    return {
      name, back, submit, isLoading
    }
  }
})
</script>

<style scoped lang="scss">
.padding {
  margin: 0 10px 0 10px;
}
</style>
