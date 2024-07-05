package com.origin.move

import com.origin.model.GameObject
import com.origin.model.Grid

data class CheckCollisionModel(
    val list: List<Grid>,
    val locked: ArrayList<Grid>,
    val obj: GameObject,
    val toX: Int,
    val toY: Int,
    val dist: Double,
    val moveType: MoveType,
    val virtual: GameObject?,
    val isMove: Boolean,
)
