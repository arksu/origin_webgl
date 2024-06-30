import axios, {
  type AxiosRequestConfig,
  type AxiosResponse
} from 'axios'
import { ref } from 'vue'
// import {useMainStore} from "../store/main";
import router from '@/router'
import { RouteNames } from '@/router/routeNames'
import { useAuthStore } from '@/stores/authStore'

export const apiUrl = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/api/'

const apiClient = axios.create({
  baseURL: apiUrl,
  timeout: 1000
})

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

export const useApi = (path: string, config: AxiosRequestConfig & {
  excludeErrorStatuses?: Array<number>
} = {}) => {
  const isLoading = ref<boolean>(false)
  const isSuccess = ref<boolean>(false)
  const response = ref<any>(null)

  const fetch = async () => {
    isLoading.value = true
    isSuccess.value = false

    const url = apiUrl + path
    console.info('api request ' + config.method + ' [' + path + ']:', config.data)

    try {
      const result = await axios.request({ url, ...config })
      response.value = result.data
      console.info('api response:', result.data)
      isSuccess.value = true
      return response
    } finally {
      isLoading.value = false
    }
  }

  return { isLoading, fetch, response }
}
