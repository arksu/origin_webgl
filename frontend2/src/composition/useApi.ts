import axios, {AxiosError, AxiosRequestConfig, AxiosResponse} from "axios";
import {ref} from "vue";
import sleep from "../utils/sleep";
import {useMainStore} from "../store";

export const apiUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/api/"

export const useApi = (path: string, config: AxiosRequestConfig & { skip?: boolean } = {}) => {
    const store = useMainStore()

    const isLoading = ref<boolean>(false)
    const error = ref<any>(null)
    const response = ref<null | AxiosResponse>(null)
    const data = ref<any>(null)

    const fetch = async () => {
        isLoading.value = true
        try {
            // TODO remove
            await sleep(600)

            const url = apiUrl + path
            console.info("api request " + config.method + " [" + url + "]", config.data)
            const result = await axios.request({url, ...config})
            response.value = result
            data.value = result.data
            console.info("api data:", result.data)
            return data
        } catch (ex: any) {
            if (axios.isAxiosError(ex)) {
                const e = ex as AxiosError
                if (e.response?.status == 403) {
                    error.value = e.response.data
                } else {
                    error.value = "Error: [" + e.response?.status + "] " + e.response?.statusText
                }
                store.lastError = error.value
                console.error(error.value)
            } else {
                error.value = ex
            }
        } finally {
            isLoading.value = false
        }
    }

    if (!config.skip) fetch()

    return {isLoading, error, response, data, fetch}
}
