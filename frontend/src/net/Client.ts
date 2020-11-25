export default class Client {

    public static instance: Client;

    /**
     * ид текущей сессии используемый для запросов серверу
     */
    public ssid?: string;

    public wasLoginTry: boolean = false;

    public character?: any;
}