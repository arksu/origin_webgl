import axios, {AxiosError, AxiosRequestConfig, AxiosRequestHeaders, AxiosResponse} from "axios";
import {ref} from "vue";
import sleep from "../utils/sleep";
import {useMainStore} from "../store/main";
import router from "../router";
import {RouteNames} from "../router/routeNames";

export const apiUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/api/"

export const useApi = (path: string, config: AxiosRequestConfig & {
    skip?: boolean,
    authorized?: boolean,
    logoutOnError?: boolean
} = {}) => {
    // зададим значения по умолчанию
    config = {
        // всегда будет отложенный запуск запроса
        skip: true,
        // всегда слать запросы с авторизацией
        authorized: true,
        // всегда будем разлогинивать при любой ошибке
        logoutOnError: true,
        ...config
    }

    const store = useMainStore()

    const isLoading = ref<boolean>(false)
    const isSuccess = ref<boolean>(false)
    const error = ref<any>(null)
    const response = ref<null | AxiosResponse>(null)
    const data = ref<any>(null)

    const logout = () => {
        store.wasAutoLogin = true
        router.push({name: RouteNames.LOGIN})
    }

    const fetch = async () => {
        isLoading.value = true
        try {
            // TODO remove
            await sleep(600)

            const headers: AxiosRequestHeaders = {...config.headers}
            // если надо послать авторизацию в запросе
            if (config.authorized) {
                // проверим авторизованы ли мы вообще
                if (store.ssid == null) {
                    error.value = 'Authorized request without ssid'
                    if (config.logoutOnError) {
                        logout()
                    }
                    return
                } else {
                    headers['Authorization'] = store.ssid
                }
            }
            console.log('headers', headers)

            const url = apiUrl + path
            console.info("api request " + config.method + " [" + url + "]", config.data)
            const result = await axios.request({url, headers, ...config})
            response.value = result
            data.value = result.data
            console.info("api data:", result.data)
            isSuccess.value = true
            return data
        } catch (ex: any) {
            if (axios.isAxiosError(ex)) {
                const e = ex as AxiosError
                // console.log('code', e.code)
                // console.log('message', e.message)
                // console.log('name', e.name)
                // console.log('e.response', e.response)

                // TODO: возможно надо игнорировать 404 ошибку когда делаем GET запрос на получение каких то данных по id в пути запроса.
                //  но при этом надо учитывать что данные таки не получены при обработке ответа

                if (e.response) {
                    // при 403 ответе просто берем ответ сервера
                    // if (e.response.status == 403 || e.response.status == 500) {
                    // если есть в теле ответа причина - возьем ее. иначе statusText с кодом
                    error.value = e.response.data ? `[${e.response.status}] ${e.response.data}` : `Error [${e.response.status}] ${e.response.statusText}`
                    // } else {
                    // в остальных случаях формируем ошибку из кода ответа и статуса
                    // error.value = `Error [${e.response.status}] ${e.response.statusText}`
                    // }
                } else {
                    error.value = e.message
                }
            } else {
                error.value = ex
            }
            store.lastError = error.value
            console.error(error.value)
            if (config.logoutOnError) {
                logout()
            }
        } finally {
            isLoading.value = false
        }
    }

    // если не надо пропускать - только тогда сразу выполним запрос
    if (!config.skip) fetch()

    return {isLoading, error, isSuccess, response, data, fetch}
}
