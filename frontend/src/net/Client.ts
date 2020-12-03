import router from "@/router";

export default class Client {

    public static instance: Client;

    /**
     * ид текущей сессии используемый для запросов серверу
     */
    public ssid?: string;

    /**
     * была ли хоть одна попытка входа?
     */
    public wasLoginTry: boolean = false;

    /**
     * надо ли сделать автоматическую попытку входа при загрузке логин экрана
     */
    public needAutologin: boolean = false;

    /**
     * последняя ошибка зарегистрированная
     */
    public lastError?: string = undefined

    public characterId?: number = undefined;


    constructor() {
        this.ssid = localStorage.getItem("ssid") || undefined;
    }

    public isLogged(): boolean {
        return this.ssid !== undefined;
    }

    public networkError(e: string) {
        console.warn("networkError " + e)
        this.lastError = e;
        this.ssid = undefined
        router.push({name: 'Login'});
    }

    public sucessLogin(ssid: string) {
        Client.instance.ssid = ssid;
        localStorage.setItem("ssid", ssid);
        router.push({name: "Characters"});
    }

    public logout() {
        Client.instance.ssid = undefined;
        localStorage.removeItem("ssid");
        router.push({name: "Login"})
    }
}