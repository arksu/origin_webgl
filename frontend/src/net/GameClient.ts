import {
    ActionProgressData,
    ContextMenuData, CreatureSay, HandData, InventoryUpdate,
    MapGridData,
    ObjectDel,
    ObjectMoved,
    ObjectStopped,
    ServerPacket,
    StatusUpdate, TimeUpdate
} from "./packets";
import GameData from "./GameData";
import Render from "../game/Render";
import MoveController from "../game/MoveController";
import {ChatItem, useGameStore} from "../store/game";

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

export default class GameClient {
    public static instance?: GameClient

    /**
     * создаем хранилище игровых данных каждый раз при создании клиента сети
     */
    public readonly _data: GameData = new GameData()

    /**
     * возвращаем данные только если есть инстанс игрового клиента
     */
    public static get data(): GameData {
        if (GameClient.instance == null) {
            throw new Error('no GameClient.instance on request GameData')
        } else {
            return GameClient.instance._data
        }
    }

    /**
     * ссылка для коннекта к вебсокет серверу
     * @private
     */
    private static readonly url: string = ("https:" === window.location.protocol ? "wss:" : "ws:") + "//"
        + window.location.hostname + ":"
        + window.location.port
        + "/api/game";

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
     * очередь запросов, запоминаем каждый запрос с его ид,
     * при получении ответа удаляем из этого списка по его ид
     */
    private requests: { [id: number]: Request } = {};

    /**
     * каждый запрос имеет свой id, увеличиваем его на 1 при каждом запросе
     */
    private lastId: number = 0;

    /**
     * обработчик отключения сети (вызывается только если очередь запросов была пуста, иначе там reject)
     */
    public onDisconnect?: () => void;
    public onError?: (m: string) => void
    public onConnect?: () => void;

    public static createNew(): GameClient {
        if (GameClient.instance !== undefined) {
            GameClient.instance.disconnect()
        }
        GameClient.instance = new GameClient();
        return GameClient.instance
    }

    /**
     * создать запрос на игровой сервер
     * @param target к чему именно обращаемся на сервере
     * @param data данные
     */
    public static async remoteCall(target: string, data?: any): Promise<any> {
        // проверим что сеть вообще создана
        if (this.instance == undefined) {
            throw Error("net instance is undefined");
        }

        // формируем запрос на сервер
        let id = ++this.instance.lastId;
        let request = {
            id: id,
            t: target,
            d: data
        };

        return new Promise((resolve, reject) => {
            // отправляем данные в сокет
            this.instance!!.socketSend(request);

            // запишем запрос в мапу запросов
            // промис завершится, когда придет ответ
            this.instance!!.requests[id] = {
                resolve,
                reject
            };
        });
    }

    private constructor() {
        this.connect()
    }

    /**
     * подключиться
     */
    private connect() {
        // сеть должна быть не активна для старта
        if (this.state !== State.Disconnected) {
            console.error("ws connect [" + this.state + "] wrong state");
            return;
        }

        this.createSocket();
    }

    public disconnect() {
        this.state = State.Disconnected;
        this.socket?.close();
        this.socket = undefined;
        GameClient.instance = undefined
    }

    /**
     * создать вебсокет
     * @private
     */
    private createSocket() {
        console.warn("ws connecting...");
        this.state = State.Connecting;
        this.socket = new WebSocket(GameClient.url);

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

        if (ev.code == 1011 && this.onError !== undefined) {
            this.onError(ev.reason)
        } else if (this.onDisconnect !== undefined) {
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
            // console.log("%cRECV", 'color: #1BAC19', _.cloneDeep(response));
            console.log("%cRECV", 'color: #1BAC19', response);

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
     * прямая отправка данных в сокет
     * @param data
     * @private
     */
    private socketSend(data: any): void {
        let d = JSON.stringify(data);
        // console.log("%cSEND", 'color: red', _.cloneDeep(data));
        console.log("%cSEND", 'color: red', data);
        this.socket!.send(d);
    }

    /**
     * приходит сообщение от сервера в определенный канал
     * @param {string} channel канал данных
     * @param data
     */
    protected onChannelMessage(channel: string, data: any) {
        const gameData = GameClient.data
        const store = useGameStore()

        switch (channel) {
            case ServerPacket.MAP_DATA: {
                const p = (<MapGridData>data)
                const key = p.x + "_" + p.y;
                console.log('map', key)
                switch (p.a) {
                    case 0 : // delete
                        delete gameData.map[key]
                        Render.instance?.deleteGrid(p.x, p.y)
                        break
                    case 1: // add
                        gameData.map[key] = {
                            x: p.x,
                            y: p.y,
                            tiles: p.tiles,
                            isChanged: false
                        };
                        break
                    case 2: // change
                        gameData.map[key].tiles = p.tiles;
                        gameData.map[key].isChanged = true
                        Render.instance?.addGrid(p.x, p.y)
                        break
                }
                break;
            }
            case ServerPacket.MAP_CONFIRMED : {
                for (let mapKey in gameData.map) {
                    const s = mapKey.split("_");
                    const x: number = +s[0];
                    const y: number = +s[1];
                    Render.instance?.addGrid(x, y)
                }
                break
            }
            case ServerPacket.OBJECT_ADD: {
                const old = gameData.objects[data.id]
                gameData.objects[data.id] = data
                if (old !== undefined) {
                    gameData.objects[data.id].moveController = old.moveController
                    gameData.objects[data.id].view = old.view
                }
                Render.instance?.onObjectAdd(gameData.objects[data.id])
                Render.instance?.updateMapScalePos()
                break;
            }
            case ServerPacket.OBJECT_DELETE: {
                const obj = gameData.objects[(<ObjectDel>data).id];
                obj.moveController?.stop()
                Render.instance?.onObjectDelete(obj)
                delete gameData.objects[(<ObjectDel>data).id];
                break;
            }
            case ServerPacket.OBJECT_MOVE : {
                const obj = gameData.objects[data.id];
                if (obj !== undefined) {
                    if (obj.moveController === undefined) {
                        obj.moveController = new MoveController(obj, (<ObjectMoved>data))
                    } else {
                        obj.moveController.applyData((<ObjectMoved>data))
                    }
                }
                break;
            }
            case ServerPacket.OBJECT_STOP : {
                const obj = gameData.objects[data.id];
                if (obj !== undefined) {
                    if (obj.moveController !== undefined) {
                        obj.moveController.serverStop(<ObjectStopped>data)
                    } else {
                        console.warn("stopped: set pos")
                        obj.x = data.x
                        obj.y = data.y
                        Render.instance?.onObjectMoved(obj)
                    }
                }

                Render.instance?.updateMapScalePos()
                break;
            }
            case ServerPacket.CONTEXT_MENU : {
                const cm = <ContextMenuData>data
                const obj = gameData.objects[cm.id];
                if (obj !== undefined) {
                    cm.obj = obj
                    Render.instance?.makeContextMenu(cm)
                } else {
                    Render.instance?.makeContextMenu(undefined)
                }
                break
            }
            case ServerPacket.STATUS_UPDATE : {
                const statusUpdate = <StatusUpdate>data
                // если пришел апдейт статуса моего персонажа
                if (statusUpdate.id == gameData.selectedCharacterId) {
                    for (let i = 0; i < statusUpdate.list.length; i++) {
                        const key = statusUpdate.list[i].i
                        const value = statusUpdate.list[i].v
                        /*
                         const val CUR_SHP = 0
                         const val CUR_HHP = 1
                         const val MAX_HP = 2
                         const val CUR_STAMINA = 3
                         const val MAX_STAMINA = 4
                         const val CUR_ENERGY = 5
                         const val MAX_ENERGY = 6
                         */
                        switch (key) {
                            case 0:
                                store.CUR_SHP = value
                                break
                            case 1 :
                                store.CUR_HHP = value
                                break
                            case 2:
                                store.MAX_HP = value
                                break
                            case 3:
                                store.CUR_STAMINA = value
                                break
                            case 4:
                                store.MAX_STAMINA = value
                                break
                            case 5:
                                store.CUR_ENERGY = value
                                break
                            case 6:
                                store.MAX_ENERGY = value
                                break
                            default:
                                throw new Error("unknown status parameter " + key)
                        }
                    }
                }
                break
            }
            case ServerPacket.CREATURE_SAY : {
                const creatureSay = <CreatureSay>data
                const obj = gameData.objects[creatureSay.id]
                // канал в который пришло сообщение
                let c = creatureSay.c == 0xff ? "System" : ((obj !== undefined && obj.a !== undefined) ? obj.a.n : "unknown")
                let item: ChatItem = {
                    title: c,
                    text: creatureSay.t,
                    channel: creatureSay.c
                }

                // сохраним сообщение в сторе
                const len = store.chatHistory.push(item)
                // ограничим максимальную длину истории чата
                const MAX_LEN = 10
                if (len > MAX_LEN) {
                    store.chatHistory.splice(0, len - MAX_LEN,)
                }
                break;
            }
            case ServerPacket.ACTION_PROGRESS : {
                const ap = <ActionProgressData>data
                store.actionProgress.total = ap.t
                store.actionProgress.current = ap.c
                break
            }
            case ServerPacket.INVENTORY_UPDATE : {
                const inv = <InventoryUpdate>data
                store.setInventory(inv)
                break
            }
            case ServerPacket.INVENTORY_CLOSE : {
                store.closeInventory(data.id)
                break
            }
            case ServerPacket.PLAYER_HAND : {
                const handData = <HandData>data
                store.hand = handData.icon !== undefined ? handData : undefined
                break
            }
            case ServerPacket.TIME_UPDATE : {
                const timeUpdate = <TimeUpdate>data
                store.time = timeUpdate
                console.log('timeUpdate h:', timeUpdate.h, 'm: ', timeUpdate.m, "day", timeUpdate.d, 'month', timeUpdate.mm)
            }
        }
    }
}
