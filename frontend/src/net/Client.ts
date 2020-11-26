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

    public character?: any;

    public isLogged(): boolean {
        return this.ssid !== undefined;
    }
}