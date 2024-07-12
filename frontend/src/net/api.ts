import axios, {
  AxiosError,
  type AxiosRequestConfig
} from 'axios'
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import router from '@/router'
import { RouteNames } from '@/router/routeNames'

export const apiUrl = window.location.protocol + '//' + window.location.hostname + ':' + window.location.port + '/api/'

const apiClient = axios.create({
  baseURL: apiUrl,
  timeout: 3000,
  timeoutErrorMessage: 'Timeout exceeded'
})

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

export const useApi = (path: string, config: AxiosRequestConfig & {
  excludeErrorStatuses?: Array<number>,
  onErrorRouteName?: string
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
      // await sleep(2000)
      const result = await apiClient.request({ url, ...config })
      console.info('api response:', result.data)
      response.value = result.data
      isSuccess.value = true
      return response
    } catch (ex: any) {
      isSuccess.value = false
      if (ex instanceof AxiosError) {
        // если есть ответ от сервера
        if (ex.response) {
          if (config.excludeErrorStatuses && config.excludeErrorStatuses.includes(ex.response.status)) {
            const response = `[${ex.response.status}] ${ex.response.data}`
            console.error(response)
            authStore.lastError = response
            return response as any
          }
          error.value = ex.response.data ? `[${ex.response.status}] ${ex.response.data}` : `[${ex.response.status}] ${ex.response.statusText}`
        } else {
          error.value = ex.message
        }
      } else {
        error.value = ex.toString()
      }
      console.error(error.value)

      authStore.setError(error.value)
      await router.push({ name: config.onErrorRouteName || RouteNames.LOGIN })
    } finally {
      isLoading.value = false
    }
  }

  return { isLoading, isSuccess, fetch, response, error }
}
