package com.origin.entity

import com.google.gson.annotations.SerializedName
import com.origin.utils.DbObject
import java.sql.Timestamp
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * игровой персонаж игрока
 */
@Entity
@Table(name = "characters")
class Character : DbObject() {
    @Id
    @Column(name = "id", columnDefinition = "INT(11) UNSIGNED NOT NULL AUTO_INCREMENT")
    @SerializedName("id")
    var id = 0

    /**
     * ид аккаунта к которому привязан персонаж
     */
    @Column(name = "accountId", columnDefinition = "INT(11) UNSIGNED NOT NULL", nullable = false)
    @Transient
    var accountId = 0

    /**
     * имя персонажа (выводим на головой в игровом клиенте)
     */
    @Column(name = "name", columnDefinition = "VARCHAR(16) NOT NULL", nullable = false)
    @SerializedName("name")
    var name: String? = null

    /**
     * на каком континенте находится игрок, либо ид дома (инстанса, локации)
     */
    @Column(name = "region", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    @SerializedName("region")
    var region = 0

    /**
     * координаты в игровых еденицах внутри континента (из этого расчитываем супергрид и грид)
     */
    @Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    @SerializedName("x")
    var x = 0

    @Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    @SerializedName("y")
    var y = 0

    /**
     * уровень (слой) глубины где находится игрок
     */
    @Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    @SerializedName("level")
    var level = 0

    /**
     * когда был создан персонаж
     */
    @Column(name = "createTime", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    @Transient
    val createTime: Timestamp? = null

    @Column(name = "onlineTime", columnDefinition = "BIGINT UNSIGNED NOT NULL DEFAULT 0")
    var onlineTime: Long = 0
        private set
}