package com.origin.model.kind

import com.origin.model.GameObject

/**
 * имеет вложенные объекты
 * слоты куда можно установить другие объекты
 * примеры: лодка, повозка
 */
interface Inner {

    /**
     * получить вложенные объекты в виде списка, где индекс определяет слот в который вставлен объект
     */
    fun getInnerObjects(): List<GameObject>?
}