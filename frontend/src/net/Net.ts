import _ from "lodash";

enum State {
    Disconnected,
    Connecting,
    Connected,
}

interface Request {
    resolve: (value?: any) => void;
    reject: (reason?: any) => void;
}

interface Response {
    id: number;

    /**
     * error if request is not success
     */
    e?: any;

    /**
     * channel
     */
    c?: string;

    /**
     * data
     */
    d: any;
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

    public onConnect?: Callback;

    /**
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     * @type {{}}
     */
    private requests: { [id: number]: Request } = {};

    private lastId: number = 0;

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
        Net.instance = undefined
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
        if (this.onConnect !== undefined) {
            this.onConnect();
        }
    }

    /**
     * обработка закрытия сокета
     * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
     * @param {CloseEvent} ev
     */
    private onclose(ev: CloseEvent) {
        console.warn("ws closed [" + ev.code + "] " + ev.reason);

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
        if (typeof ev.data === "string") {
            let response: Response = JSON.parse(ev.data);
            console.log("%cRECV", 'color: #1BAC19', _.cloneDeep(response));

            // пришло сообщение в общий канал (не ответ на запрос серверу)
            if (response.id === 0 && response.c !== undefined) {
                this.onChannelMessage(response.c, response.d);
            } else {
                // ищем в мапе по ид запроса
                let request: Request = this.requests[response.id];
                if (request !== undefined) {
                    // удалим из мапы
                    delete this.requests[response.id];

                    // ответ успешен?
                    if (response.e === undefined) {
                        request.resolve(response.d);
                    } else {
                        console.error(response.e);
                        // иначе была ошибка, прокинем ее
                        request.reject(response.e);
                    }
                }
            }
        } else {
            console.warn("unknown data type: " + (typeof ev.data));
            console.warn("RECV", ev.data)
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
        console.log("%cSEND", 'color: red', _.cloneDeep(data));
        this.socket!.send(d);
    }

    public static async remoteCall(target: string, req?: any): Promise<any> {
        if (this.instance == undefined) {
            throw Error("net instance is undefined");
        }

        // формируем запрос на севрер
        let id = ++this.instance.lastId;
        let data = {
            id: id,
            t: target,
            d: req
        };

        return await new Promise((resolve, reject) => {
            // отправляем данные в сокет
            this.instance!!.socketSend(data);

            // запишем запрос в мапу запросов
            // промис завершится когда придет ответ
            this.instance!!.requests[id] = {
                resolve,
                reject
            };
        });
    }
}