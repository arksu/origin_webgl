import type GameResponseDTO from '@/net/GameResponseDTO'
import type GameRequestDTO from '@/net/GameRequestDTO'
import GameProto from '@/net/GameProto'
import type Render from '@/game/Render'
import type GameData from '@/net/GameData'

enum State {
  Disconnected,
  Connecting,
  Connected,
}

interface Request {
  resolve: (value?: any) => void;
  reject: (reason?: string) => void;
}

export default class GameClient {

  /**
   * состояние клиента сети
   * @private
   */
  private state: State = State.Disconnected

  /**
   * сокет
   */
  private readonly socket: WebSocket

  /**
   * реализация игрового протокола
   */
  private readonly gameProto: GameProto

  private readonly gameData : GameData

  /**
   * каждый запрос имеет свой id, увеличиваем его на 1 при каждом запросе
   */
  private lastId: number = 0

  /**
   * очередь запросов, запоминаем каждый запрос с его ид,
   * при получении ответа удаляем из этого списка по его ид
   */
  private readonly requests: { [id: number]: Request } = {}

  public onDisconnect?: () => void
  public onError?: (m: string) => void
  public onConnect?: () => void

  /**
   * ссылка для коннекта к вебсокет серверу
   * @private
   */
  private static readonly url: string = ('https:' === window.location.protocol ? 'wss:' : 'ws:') + '//'
    + window.location.hostname + ':'
    + window.location.port
    + '/api/game'

  constructor(render : Render) {
    console.warn('ws connecting...')
    this.state = State.Connecting
    this.socket = new WebSocket(GameClient.url)

    this.socket.onopen = this.onopen.bind(this)
    this.socket.onclose = this.onclose.bind(this)
    this.socket.onmessage = this.onmessage.bind(this)

    this.gameData = render.gameData
    this.gameProto = new GameProto(this, render)
  }

  public disconnect() {
    this.state = State.Disconnected
    this.socket.close()
  }

  /**
   * обработка открытия сокета
   */
  private onopen(_: Event) {
    console.warn('ws connected')

    if (this.state !== State.Connecting) {
      throw Error('wrong state [' + this.state + '] websocket on open')
    }

    this.state = State.Connected
    if (this.onConnect !== undefined) {
      this.onConnect()
    }
  }

  /**
   * обработка закрытия сокета
   * https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
   * @param {CloseEvent} ev
   */
  private onclose(ev: CloseEvent) {
    console.warn('ws closed [' + ev.code + '] ' + ev.reason)

    this.state = State.Disconnected

    if (ev.code == 1011 && this.onError !== undefined) {
      this.onError(ev.reason)
    } else if (this.onDisconnect !== undefined) {
      this.onDisconnect()
    }
  }

  /**
   * получение сообщений от сервера в сокете
   * @param {MessageEvent} ev
   */
  private onmessage(ev: MessageEvent) {
    if (typeof ev.data === 'string') {
      const response: GameResponseDTO = JSON.parse(ev.data)
      console.log('%cRECV', 'color: #1BAC19', response)

      if (response.id === 0 && response.c !== undefined) {
        this.gameProto.processMessage(response)
      } else {
        // ищем в мапе по ид запроса
        const request: Request = this.requests[response.id]
        if (request !== undefined) {
          // удалим из мапы
          delete this.requests[response.id]

          console.debug('requests size: ', Object.keys(this.requests).length)

          // ответ успешен?
          if (response.e === undefined) {
            request.resolve(response.d)
          } else {
            console.error(response.e)
            // иначе была ошибка, прокинем ее
            request.reject(response.e)
          }
        }
      }
    } else {
      console.warn('unknown data type: ' + (typeof ev.data))
      console.warn('RECV', ev.data)
    }
  }

  /**
   * отправки пакет на игровой сервер
   * @param target
   * @param data
   */
  public send(target: string, data: any = undefined)  {
    const request: GameRequestDTO = {
      id: ++this.lastId,
      t: target,
      d: data
    }
    return new Promise((resolve, reject) => {
      // отправляем данные в сокет
      this.socketSend(request)

      // запишем запрос в мапу запросов
      // промис завершится, когда придет ответ
      this.requests[request.id] = {
        resolve,
        reject
      }
    })
  }

  /**
   * прямая отправка данных в сокет
   * @param request
   * @private
   */
  private socketSend(request: GameRequestDTO): void {
    if (this.state === State.Connected) {
      const rawData = JSON.stringify(request)
      console.log('%cSEND', 'color: red', request)
      this.socket.send(rawData)
    }
  }
}