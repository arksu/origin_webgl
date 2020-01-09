import {ApiRequest} from "./ApiRequest";
import * as _ from "lodash";

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
     * время ожидания ответа сервера на запрос, после превышения считаем что коннект оборвался
     */
    private requestTimeout: number = 1000;

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
                        // убьем таймер ожидания ответа
                        clearTimeout(r.waitTimer);

                        // ответ успешен?
                        if (d.s === 1) {
                            r.resolve(d.d);
                        } else {
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
     * @param {number} timeout
     * @returns {Promise<any>}
     */
    public remoteCall(target: string, req: ApiRequest, timeout?: number): Promise<any> {
        // если не передали в параметре - берем дефолтный таймаут
        let t = timeout === undefined ? this.requestTimeout : timeout;

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
                    waitTimer: undefined,
                    target,
                    req,
                    timeout: t
                };
            } else if (this.state === State.Connected) {
                const id = ++this.lastId;
                let data = {
                    id,
                    t: target,
                    d: req
                };
                this.socketSend(data);

                // таймер на ожидание ответа сервера
                let waitTimer: number = t > 0 ? setTimeout(() => {
                    this.reconnectTry();
                }, t) : undefined;

                this.requests[id] = {
                    resolve,
                    reject,
                    waitTimer,
                    target,
                    req,
                    timeout: t
                };
            } else if (this.state === State.Reconnecting || this.state === State.Connecting) {
                console.log("call when disconnected");

                this.lastId++;
                this.requests[this.lastId] = {
                    resolve,
                    reject,
                    waitTimer: undefined,
                    target,
                    req,
                    timeout
                };
            } else {
                reject("ws wrong state");
            }
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

            // таймер на ожидание ответа сервера
            if (r.timeout !== undefined && r.timeout > 0) {
                r.waitTimer = setTimeout(() => {
                    this.reconnectTry();
                    // r.reject("timeout");
                }, r.timeout);
            } else {
                r.waitTimer = undefined;
            }
            console.log("resend");
            console.log(_.cloneDeep(data));
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
        this.socket!.send(d);
    }
}

interface Request {
    resolve: (value?: any) => void;
    reject: (reason?: any) => void;
    waitTimer?: number;
    timeout: number;
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