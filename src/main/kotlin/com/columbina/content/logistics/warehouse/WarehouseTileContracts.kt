package com.columbina.content.logistics.warehouse

import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

interface ControlledWarehouseTile {
    var controllerPos: BlockPos?

    fun isValidController(controller: WarehouseControllerBlockEntity): Boolean

    fun setController(controller: WarehouseControllerBlockEntity?) {
        controllerPos = controller?.blockPos
    }

    fun getController(level: Level?): WarehouseControllerBlockEntity? {
        val pos = controllerPos ?: return null
        return level?.getBlockEntity(pos) as? WarehouseControllerBlockEntity
    }
}

interface WarehouseInventoryListener {
    fun onWarehouseInventoryUpdated(controller: WarehouseControllerBlockEntity)
}
