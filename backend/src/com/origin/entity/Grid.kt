package com.origin.entity

import com.origin.utils.DbObject
import org.jpark.TableExtended
import java.sql.Blob
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

/**
 * игровой "чанк" (регион), базовый кусок карты
 * при больших объемах мира надо бить таблицу на партиции
 * по instance, суб партиции по x, y и тд
 */
@Entity
@Table(name = "grids")
@TableExtended(creationSuffix = "engine=MyISAM")
class Grid : DbObject() {
    /**
     * на каком континенте находится грид, либо ид дома (инстанса, локации)
     */
    @Id
    @Column(name = "region", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    var region = 0

    /**
     * координаты грида в мире (какой по счету грид, НЕ в игровых единицах)
     * разбиение таблицы (partitions) делаем на основе RANGE(x) и субпартициях на основе RANGE(y)
     */
    @Id
    @Column(name = "x", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    var x = 0

    @Id
    @Column(name = "y", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    var y = 0

    @Id
    @Column(name = "level", columnDefinition = "INT(11) UNSIGNED NOT NULL")
    var level = 0

    /**
     * сырые данные тайлов в виде массива байт, по 2 байта на 1 тайл
     */
    @Column(name = "tiles", columnDefinition = "BLOB NOT NULL", nullable = false)
    var tilesBlob: Blob? = null
}