import {ActionContext, ActionTree} from 'vuex'
import {ActionTypes} from './action-types'
import {MutationTypes} from "@/store/mutation-types";
import {Mutations} from "@/store/mutations";
import {State} from "@/store/state";
import router from "@/router";

type AugmentedActionContext = {
    commit<K extends keyof Mutations>(
        key: K,
        payload: Parameters<Mutations[K]>[1]
    ): ReturnType<Mutations[K]>
} & Omit<ActionContext<State, State>, 'commit'>

export interface Actions {
    [ActionTypes.SUCCESS_LOGIN](
        {commit}: AugmentedActionContext,
        payload: string
    ): void,

    [ActionTypes.LOGOUT](
        {commit}: AugmentedActionContext
    ): void
}

export const actions: ActionTree<State, State> & Actions = {
    [ActionTypes.SUCCESS_LOGIN]({commit}, payload: string) {
        commit(MutationTypes.SET_SSID, payload)
        router.push({name: "Characters"});
    },
    [ActionTypes.LOGOUT]({commit}) {
        commit(MutationTypes.SET_SSID, undefined)
        router.push({name: "Login"})
    },
}