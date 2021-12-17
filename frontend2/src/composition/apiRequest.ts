import axios from "axios";
import {ref} from "vue";
import sleep from "../utils/sleep";

const apiUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/api/"

export default function useApiGetRequest(path: string) {
    const isLoading = ref(false)
    const response = ref()

    const doRequest = async () => {
        isLoading.value = true
        await sleep(1000)
        return axios.post(apiUrl + path)
            .then(r => {
                console.log(r)
                response.value = r
            })
            .catch(error => {
                console.log(error)
            })
            .finally(() => isLoading.value = false)
    }

    return {
        isLoading, doRequest, response
    }
}
