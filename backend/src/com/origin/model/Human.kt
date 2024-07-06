package com.origin.model

import com.origin.ObjectID

abstract class Human(id: ObjectID, pos: ObjectPosition) : MovingObject(id, pos) {

    override suspend fun onEnterGrid(grid: Grid) {
        super.onEnterGrid(grid)
        logger.warn("Activate ${grid.x} ${grid.y} pos=$pos")
        grid.sendAndWait(GridMessage.Activate(this))
    }

    override suspend fun onLeaveGrid(grid: Grid) {
        super.onLeaveGrid(grid)
        logger.warn("Deactivate ${grid.x} ${grid.y}")
        grid.sendAndWait(GridMessage.Deactivate(this))
    }
}