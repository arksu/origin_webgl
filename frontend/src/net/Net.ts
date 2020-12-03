import {ApiRequest} from "@/net/ApiRequest";
import _ from "lodash";

enum State {
    Disconnected,
    Connecting,
    Connected,
}

interface Request {
    resolve: (value?: any) => void;
    reject: (reason?: any) => void;
    target: string;
    req?: ApiRequest;
}

interface Response {
    id: number;

    /**
     * success? 1-true, 0-false
     */
    s: number;

    /**
     * channel
     */
    c?: string;

    /**
     * data
     */
    d: any;

    /**
     * error if not success
     */
    e?: any;
}

export default class Net {
    public static instance?: Net = undefined;

    /**
     * ссылка для коннекта к вебсокет серверу
     * @private
     */
    private url: string;

    /**
     * сокет
     */
    private socket?: WebSocket = undefined;

    /**
     * состояние клиента сети
     * @private
     */
    private state: State = State.Disconnected;

    /**
     * обработчик отключения сети (вызывается только если очередь запросов была пуста, иначе там reject)
     */
    public onDisconnect?: Callback;

    /**
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     * @type {{}}
     */
    private requests: { [id: number]: Request } = {};

    constructor(url: string) {
        this.url = url;
        this.connect();
    }

    /**
     * подключиться
     */
    private connect() {
        // есть должна быть не активна для старта
        if (this.state !== State.Disconnected) {
            console.log("ws connect [" + this.state + "] wrong state");
            return;
        }

        this.createSocket();
    }

    public disconnect() {
        this.state = State.Disconnected;
        this.socket?.close();
        this.socket = undefined;
    }

    /**
     * создать вебсокет
     * @private
     */
    private createSocket() {
        console.warn("ws connecting...");
        this.state = State.Connecting;
        this.socket = new WebSocket(this.url);

        this.socket.onopen = this.onopen.bind(this);
        this.socket.onclose = this.onclose.bind(this);
        this.socket.onmessage = this.onmessage.bind(this);
    }

    /**
     * обработка открытия сокета
     * @param {Event} _ev
     */
    private onopen(_ev: Event) {
        console.warn("ws connected");

        if (this.state !== State.Connecting) {
            throw Error("wrong state websocket on open");
        }

        this.state = State.Connected;

        this.socketSend("dd")
    }

    /**
     * обработка закрытия сокета
     * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
     * @param {CloseEvent} ev
     */
    private onclose(ev: CloseEvent) {
        console.log(ev);
        console.warn("ws close [" + ev.code + "] " + this.state);

        this.state = State.Disconnected;
        this.socket = undefined;

        if (this.onDisconnect !== undefined) {
            this.onDisconnect();
        }
    }

    /**
     * получение сообщений от сервера в сокете
     * @param {MessageEvent} ev
     */
    private onmessage(ev: MessageEvent) {
        console.log(ev.data)
        if (typeof ev.data === "string") {
            // if (ev.data === "ping") {
            //     // this.pingTimer = setTimeout(() => {
            //     //     this.socket!.send("ping");
            //     // }, 15000);
            // } else {
            //     let d: Response = JSON.parse(ev.data);
            //     console.log(_.cloneDeep(d.d));
            //
            //     // пришло сообщение в общий канал (не ответ на запрос серверу)
            //     if (d.id === 0 && d.c !== undefined) {
            //         this.onChannelMessage(d.c, d.d);
            //     } else {
            //         // ищем в мапе по ид запроса
            //         let r: Request = this.requests[d.id];
            //         if (r !== undefined) {
            //             // удалим из мапы
            //             delete this.requests[d.id];
            //
            //             // ответ успешен?
            //             if (d.s === 1) {
            //                 r.resolve(d.d);
            //             } else {
            //                 console.error(d.e);
            //                 // иначе была ошибка, прокинем ее
            //                 r.reject(d.e);
            //             }
            //         }
            //     }
            // }
        } else {
            console.warn("unknown data type: " + (typeof ev.data));
        }
    }

    /**
     * приходит сообщение от сервера в определенный канал
     * @param {string} _channel
     * @param _data
     */
    protected onChannelMessage(_channel: string, _data: any) {
    }

    private socketSend(data: any): void {
        let d = JSON.stringify(data);
        console.log(_.cloneDeep(data));
        this.socket!.send(d);
    }
}