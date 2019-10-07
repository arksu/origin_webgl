import {ApiRequest} from "./ApiRequest";
import * as _ from "lodash";

enum State {
    Idle,
    Connecting,
    Connected,
    Disconnecting,
    Reconnecting
}

/**
 * базовая реализация вебсокет коннектов
 */
export default class WsNet {

    /**
     * сокет
     */
    socket?: WebSocket;

    /**
     * текущее состояние
     * @type {State.Idle}
     */
    state: State = State.Idle;

    /**
     * url для открытия коннекта
     */
    url: string;

    /**
     * каллбаки для коннекта (успешен или с ошибкой)
     */
    successConnectCallback?: Callback;
    errorConnectCallback: (e: string) => void;

    /**
     * каллбак для явного вызова disconnect извне
     */
    disconnectingCallback?: Callback;

    /**
     * последний ид для отправки сообщений серверу
     */
    lastId: number = 0;

    /**
     * количество попыток реконнекта в случае обрыва связи
     * @type {number}
     */
    reconnectTries: number = 10;

    /**
     * время между попытками реконнекта
     * @type {number}
     */
    reconnectTime: number = 4000;

    /**
     * счетчит попыток переподключения к севреру
     */
    reconnectCounter: number;

    /**
     * обработчик отключения сети (вызывается только если очередь запросов была пуста, иначе там reject)
     */
    onDisconnect?: Callback;

    /**
     * таймер для отправки пингов
     */
    pingTimer?: number;

    /**
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     * @type {{}}
     */
    private requests: { [id: number]: Request } = {};

    /**
     * подключиться
     * @param {Callback} successCallback
     * @param {(e: string) => void} errorCallback
     */
    connect(successCallback: Callback, errorCallback: (e: string) => void) {
        // есть должна быть не активна для старта
        if (this.state !== State.Idle) {
            console.log("ws connect [" + this.state + "] unable");
            return;
        }
        console.warn("ws connect...");

        this.lastId = 0;
        this.requests = {};
        this.reconnectCounter = this.reconnectTries;

        this.socket = new WebSocket(this.url);

        this.socket.onopen = this.onopen.bind(this);
        this.socket.onerror = this.onerror.bind(this);
        this.socket.onclose = this.onclose.bind(this);
        this.socket.onmessage = this.onmessage.bind(this);
        this.successConnectCallback = successCallback;
        this.errorConnectCallback = errorCallback;

        this.state = State.Connecting;
    }

    /**
     * отключиться
     */
    disconnect(disconnectCallback ?: Callback) {
        if (this.state !== State.Idle) {
            this.requests = {};
            this.state = State.Disconnecting;
            this.disconnectingCallback = disconnectCallback;
            this.socket!.close();
        } else {
            disconnectCallback!();
        }
    }

    /**
     * обработка открытия сокета
     * @param {Event} _ev
     */
    private onopen(_ev: Event) {
        console.warn("ws connected");

        if (this.state !== State.Connecting) {
            throw Error("wrong state on open");
        }

        this.state = State.Connected;

        // if (this.successConnectCallback !== undefined) {
        //     this.successConnectCallback();
        // }
    }

    /**
     * обрабтка ошибок сокета
     * @param {Event} ev
     */
    private onerror(ev: Event) {
        // TODO DEBUG DELETE
        console.warn("ws error");
        console.warn(ev);

    }

    /**
     * обработка закрытия сокета
     * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
     * @param {CloseEvent} ev
     */
    private onclose(ev: CloseEvent) {
        console.warn("ws close [" + ev.code + "]");

        let oldState = this.state;
        this.state = State.Idle;
        this.socket = undefined;
        clearTimeout(this.pingTimer);
        this.pingTimer = undefined;

        switch (oldState) {
            case State.Connecting:
                if (this.errorConnectCallback !== undefined) {
                    this.errorConnectCallback("connect error");
                }
                break;
            case State.Connected:
                // произошел обрыв в подключенном состоянии
                // надо сделать реконнект

                if (ev.code === 1006 && !ev.wasClean) {
                    this.reconnectTry();
                }
                break;
            case State.Disconnecting:
                this.disconnectingCallback!();
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
                let d = JSON.parse(ev.data);
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
                        clearTimeout(r.timer);

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
     * обобщенный код переподключения
     */
    private reconnectTry() {
        console.warn("reconnect try[" + (this.reconnectTries - this.reconnectCounter + 1) + " of " + this.reconnectTries + "]");

        // если еще есть попытки реконнекта
        if (this.reconnectCounter > 0) {
            this.reconnectCounter--;
            this.state = State.Reconnecting;

            for (let k in this.requests) {
                let r = this.requests[k];
                clearTimeout(r.timer);
            }

            setTimeout(() => {
                this.reconnect();
            }, this.reconnectTime);
        } else {
            let empty = true;
            // по всем текущим отправленным запросам вызовем reject
            for (let k in this.requests) {
                empty = false;
                let r = this.requests[k];
                r.reject("network is disconnected");
            }
            this.requests = {};
            this.state = State.Idle;
            this.socket = undefined;

            if (empty && this.onDisconnect !== undefined) {
                this.onDisconnect();
            }
        }
    }

    /**
     * переподключится к серверу
     */
    private reconnect() {
        console.warn("reconnecting...");
        this.socket = new WebSocket(this.url);

        this.socket.onopen = this.onopen.bind(this);
        this.socket.onerror = this.onerror.bind(this);
        this.socket.onclose = this.onclose.bind(this);
        this.socket.onmessage = this.onmessage.bind(this);

        this.successConnectCallback = () => {
            console.warn("ws reconnected!");
            let tc = 0;

            // идем по всем запросам в очереди и по новой отправляем
            for (let k in this.requests) {
                let r = this.requests[k];

                let data = {
                    id: k,
                    t: r.target,
                    d: r.req
                };

                setTimeout(() => {
                    // таймер на ожидание ответа сервера
                    r.timer = setTimeout(() => {
                        this.reconnectTry();
                        // r.reject("timeout");
                    }, r.timeout);
                    console.log("resend");
                    console.log(_.cloneDeep(data));
                    this.socketSend(data, +k);
                }, tc);

                tc += 1000;
            }

            this.reconnectCounter = this.reconnectTries;
        };
        this.errorConnectCallback = (_e: string) => {
            this.reconnectTry();
        };
        this.state = State.Connecting;
    }

    /**
     * послать запрос на сервер
     * @param {string} target
     * @param {ApiRequest} req
     * @param {number} timeout
     * @returns {Promise<any>}
     */
    // TODO
    public remoteCall(target: string, req: ApiRequest, timeout: number): Promise<any> {
        return new Promise((resolve, reject) => {
            if (this.state == State.Connected) {
                const id = ++this.lastId;
                let data = {
                    id,
                    t: target,
                    d: req
                };
                this.socketSend(data, id);

                // таймер на ожидание ответа сервера
                let t: number = setTimeout(() => {
                    this.reconnectTry();
                    // reject("timeout");
                }, timeout);

                this.requests[id] = {
                    resolve,
                    reject,
                    timer: t,
                    target,
                    req,
                    timeout
                };
            } else if (this.state === State.Reconnecting || this.state === State.Connecting) {
                console.log("call when disconnected");

                this.lastId++;
                this.requests[this.lastId] = {
                    resolve,
                    reject,
                    timer: undefined,
                    target,
                    req,
                    timeout
                };
            } else {
                reject("ws wrong state");
            }
        });
    }

    /**
     * подключена ли сеть
     * @returns {boolean}
     */
    public isConnected(): boolean {
        return this.state === State.Connected;
    }

    /**
     * приходит сообщение от сервера в определенный канал
     * @param {string} _channel
     * @param _data
     */
    protected onChannelMessage(_channel: string, _data: any) {
    }

    private socketSend(data: any, id: number): void {
        let d = JSON.stringify(data);
        this.socket!.send(d);
    }
}

interface Request {
    resolve: (value?: any) => void;
    reject: (reason?: any) => void;
    timer?: number;
    timeout: number;
    target: string;
    req: ApiRequest;
}