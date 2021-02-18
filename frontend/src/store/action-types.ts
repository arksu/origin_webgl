export enum ActionTypes {
    // произошел успешный вход через API
    SUCCESS_LOGIN = 'SUCCESS_LOGIN',
    // разлогинится и вернуться на экран входа
    LOGOUT = 'LOGOUT',
    // получить и удалить из стейта последнюю ошибку
    NETWORK_ERROR = 'NETWORK_ERROR'
}