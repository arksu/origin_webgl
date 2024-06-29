import axios, {AxiosError} from "axios";
import {ref} from "vue";
// import {useMainStore} from "../store/main";
import router from "@/router";
import {RouteNames} from '@/router/routeNames';

export const apiUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port + "/api/"

export const useApi = (path: string, config:  {
    skip?: boolean,
    authorized?: boolean,
    logoutOnError?: boolean
} = {}) => {

}
