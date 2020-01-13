import {ApiRequest} from "./ApiRequest";
import * as _ from "lodash";
import {showError} from "../error";

enum State {
    Idle,
    Connecting,
    Connected,
    ReconnectWait,
    Reconnecting
}

/**
 * базовая реализация вебсокет коннектов
 */
export default class Net {

    public static instance: Net;

    /**
     * сокет
     */
    private socket?: WebSocket;

    /**
     * текущее состояние
     * @type {State.Idle}
     */
    private state: State = State.Idle;

    /**
     * url для открытия коннекта
     */
    public url: string;

    /**
     * последний ид для отправки сообщений серверу
     */
    private lastId: number = 0;

    /**
     * время между попытками реконнекта
     * @type {number}
     */
    private reconnectTime: number = 4000;

    /**
     * обработчик отключения сети (вызывается только если очередь запросов была пуста, иначе там reject)
     */
    public onDisconnect?: Callback;

    /**
     * таймер для отправки пингов
     */
    private pingTimer?: number;

    /**
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     * @type {{}}
     */
    private requests: { [id: number]: Request } = {};

    /**
     * обработка открытия сокета
     * @param {Event} _ev
     */
    private onopen(_ev: Event) {
        console.warn("ws connected");

        if (this.state !== State.Connecting && this.state !== State.Reconnecting) {
            throw Error("wrong state on open");
        }

        let oldState = this.state;
        this.state = State.Connected;

        this.sendRequests();
    }

    /**
     * обработка закрытия сокета
     * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
     * @param {CloseEvent} ev
     */
    private onclose(ev: CloseEvent) {
        console.log(ev);
        console.warn("ws close [" + ev.code + "] " + this.state);

        let oldState = this.state;
        this.state = State.Idle;
        this.socket = undefined;
        clearTimeout(this.pingTimer);
        this.pingTimer = undefined;

        switch (oldState) {
            case State.Reconnecting:
                this.reconnectTry();
                break;

            case State.Connecting:
                for (let k in this.requests) {
                    let r = this.requests[k];
                    r.reject("Connection is not available");
                }

                if (this.onDisconnect !== undefined) {
                    this.onDisconnect();
                }
                break;

            case State.Connected:
                // произошел обрыв в подключенном состоянии
                // надо сделать реконнект
                if (ev.code === 1006 && !ev.wasClean) {
                    this.reconnectTry();
                } else {
                    if (this.onDisconnect !== undefined) {
                        this.onDisconnect();
                    }
                }
                break;
        }
    }

    /**
     * получение сообщений от сервера в сокете
     * @param {MessageEvent} ev
     */
    private onmessage(ev: MessageEvent) {
        if (typeof ev.data === "string") {
            if (ev.data === "ping") {
                this.pingTimer = setTimeout(() => {
                    this.socket!.send("ping");
                }, 15000);
            } else {
                let d: Response = JSON.parse(ev.data);
                console.log(_.cloneDeep(d.d));

                // пришло сообщение в общий канал (не ответ на запрос серверу)
                if (d.id === 0 && d.c !== undefined) {
                    this.onChannelMessage(d.c, d.d);
                } else {
                    // ищем в мапе по ид запроса
                    let r: Request = this.requests[d.id];
                    if (r !== undefined) {
                        // удалим из мапы
                        delete this.requests[d.id];

                        // ответ успешен?
                        if (d.s === 1) {
                            r.resolve(d.d);
                        } else {
                            console.error(d.e);
                            // иначе была ошибка, прокинем ее
                            r.reject(d.e);
                        }
                    }
                }
            }
        } else {
            console.warn("unknown data type: " + (typeof ev.data));
        }
    }

    /**
     * подключиться
     */
    private connect() {
        // есть должна быть не активна для старта
        if (this.state !== State.Idle) {
            console.log("ws connect [" + this.state + "] unable");
            return;
        }
        console.warn("ws connecting...");

        this.lastId = 0;
        this.requests = {};

        this.createSocket();

        this.state = State.Connecting;
    }

    public disconnect() {
        this.socket = undefined;
        this.state = State.Idle;
        this.requests = {};
    }

    /**
     * переподключится к серверу
     */
    private reconnect() {
        console.warn("reconnecting...");

        this.createSocket();

        this.state = State.Reconnecting;
    }

    /**
     * обобщенный код переподключения
     */
    private reconnectTry() {
        console.warn("reconnect try");

        if (this.pingTimer !== undefined) {
            clearTimeout(this.pingTimer);
            this.pingTimer = undefined;
        }

        // по всем текущим отправленным запросам вызовем reject
        for (let k in this.requests) {
            let r = this.requests[k];
            r.reject("network is disconnected");
        }
        this.requests = {};
        this.state = State.Idle;
        this.socket = undefined;

        if (this.onDisconnect !== undefined) {
            this.onDisconnect();
        }
    }

    /**
     * послать запрос на сервер
     * @param {string} target
     * @param {ApiRequest} req
     * @returns {Promise<any>}
     */
    public remoteCall(target: string, req?: ApiRequest): Promise<any> {
        return new Promise((resolve, reject) => {
            // не подключены
            if (this.state === State.Idle) {
                // подключаемся
                this.connect();

                // и пихаем в очередь запросов новый запрос
                this.lastId++;
                this.requests[this.lastId] = {
                    resolve,
                    reject,
                    target,
                    req,
                };
            } else if (this.state === State.Connected) {
                const id = ++this.lastId;
                let data = {
                    id,
                    t: target,
                    d: req
                };
                this.socketSend(data);

                this.requests[id] = {
                    resolve,
                    reject,
                    target,
                    req,
                };
            } else if (this.state === State.Reconnecting || this.state === State.Connecting) {
                console.log("call when disconnected");

                this.lastId++;
                this.requests[this.lastId] = {
                    resolve,
                    reject,
                    target,
                    req,
                };
            } else {
                reject("ws wrong state");
            }
        });
    }

    public gameCall(target: string, req?: ApiRequest): Promise<any> {
        return new Promise((resolve, reject) => {
            this.remoteCall(target, req)
                .then((d) => {
                    resolve(d);
                })
                .catch((e) => {
                    this.disconnect();
                    showError(e);
                    reject(e);
                })
        });
    }

    private sendRequests(): void {
        // идем по всем запросам в очереди и по новой отправляем
        for (let k in this.requests) {
            let r = this.requests[k];

            let data = {
                id: k,
                t: r.target,
                d: r.req
            };

            this.socketSend(data);
        }
    }

    /**
     * приходит сообщение от сервера в определенный канал
     * @param {string} _channel
     * @param _data
     */
    protected onChannelMessage(_channel: string, _data: any) {
    }

    private createSocket() {
        this.socket = new WebSocket(this.url);

        this.socket.onopen = this.onopen.bind(this);
        this.socket.onclose = this.onclose.bind(this);
        this.socket.onmessage = this.onmessage.bind(this);
    }

    private socketSend(data: any): void {
        let d = JSON.stringify(data);
        console.log(_.cloneDeep(data));
        this.socket!.send(d);
    }
}

interface Request {
    resolve: (value?: any) => void;
    reject: (reason?: any) => void;
    target: string;
    req: ApiRequest;
}

interface Response {
    s: number;
    id: number;
    c?: string;
    d: any;
    e?: any;
}