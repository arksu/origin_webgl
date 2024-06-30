import axios, {
  AxiosError,
  type AxiosRequestConfig
} from 'axios'
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'

export const apiUrl = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/api/'

const apiClient = axios.create({
  baseURL: apiUrl,
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
}) => {
  const authStore = useAuthStore()

  const isLoading = ref<boolean>(false)
  const isSuccess = ref<boolean>(false)
  const response = ref<any>(null)
  const error = ref<string | undefined>(undefined)

  const fetch = async () => {
    isLoading.value = true
    isSuccess.value = false
    authStore.lastError = undefined

    const url = apiUrl + path
    console.info('api request ' + config.method + ' [' + path + ']:', config.data)

    try {
      const result = await axios.request({ url, timeout: 3000, timeoutErrorMessage: 'Timeout exceeded', ...config })
      console.info('api response:', result.data)
      response.value = result.data
      isSuccess.value = true
      return response
    } catch (ex: any) {
      console.error(ex)
      isSuccess.value = false
      if (ex instanceof AxiosError) {
        // если есть ответ от сервера
        if (ex.response) {
          if (config.excludeErrorStatuses && ex.response.status in config.excludeErrorStatuses) {
            return ex.response.data
          }
          error.value = ex.response.data ? `[${ex.response.status}] ${ex.response.data}` : `[${ex.response.status}] ${ex.response.statusText}`
        } else {
          error.value = ex.message
        }
      } else {
        error.value = ex.toString()
      }
      authStore.lastError = error.value
    } finally {
      isLoading.value = false
    }
  }

  return { isLoading, fetch, response, error }
}
