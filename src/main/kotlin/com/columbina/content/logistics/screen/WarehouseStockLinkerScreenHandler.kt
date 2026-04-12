package com.columbina.content.logistics.screen

import com.columbina.content.logistics.warehouse.WarehouseLinkFilter
import com.columbina.content.logistics.warehouse.WarehouseStockLinkerBlockEntity
import com.columbina.content.logistics.warehouse.stackItemId
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class WarehouseStockLinkerScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val blockPos: BlockPos,
) : AbstractContainerMenu(ColumbinaScreenHandlers.WAREHOUSE_STOCK_LINKER, syncId) {
    companion object {
        private const val ACTION_STRIDE = 8
        private const val ACTION_SET = 1
        private const val ACTION_CLEAR = 2
        private const val ACTION_INC = 3
        private const val ACTION_DEC = 4
        private const val ACTION_EQ = 5
        private const val ACTION_ADD = 6
    }

    private val access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos)
    private val blockEntity = playerInventory.player.level().getBlockEntity(blockPos) as? WarehouseStockLinkerBlockEntity

    constructor(syncId: Int, playerInventory: Inventory, blockEntity: WarehouseStockLinkerBlockEntity) : this(syncId, playerInventory, blockEntity.blockPos)

    init {
        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(playerInventory, column + (row * 9) + 9, 8 + (column * 18), 98 + (row * 18)))
            }
        }

        for (column in 0 until 9) {
            addSlot(Slot(playerInventory, column, 8 + (column * 18), 156))
        }
    }

    fun filters(): List<WarehouseLinkFilter> = blockEntity?.getFilters() ?: emptyList()

    private fun updateFilters(transform: (MutableList<WarehouseLinkFilter>) -> Unit): Boolean {
        val blockEntity = blockEntity ?: return false
        val filters = blockEntity.getFilters().toMutableList()
        transform(filters)
        blockEntity.setFilters(filters)
        return true
    }

    private fun selectedHeldItemId(): String? {
        val player = playerInventory.player
        val stack = sequenceOf(player.mainHandItem, player.offhandItem).firstOrNull { !it.isEmpty } ?: ItemStack.EMPTY
        return stackItemId(stack)
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        if (id == ACTION_ADD) {
            return updateFilters { filters ->
                if (filters.size < 4) {
                    filters += WarehouseLinkFilter()
                }
            }
        }

        val index = id / ACTION_STRIDE
        val action = id % ACTION_STRIDE
        return when (action) {
            ACTION_SET -> updateFilters { filters ->
                if (index !in filters.indices) return@updateFilters
                filters[index].itemId = selectedHeldItemId()
            }
            ACTION_CLEAR -> updateFilters { filters ->
                if (index !in filters.indices) return@updateFilters
                filters.removeAt(index)
            }
            ACTION_INC -> updateFilters { filters ->
                if (index !in filters.indices) return@updateFilters
                filters[index].compareValue += 1
            }
            ACTION_DEC -> updateFilters { filters ->
                if (index !in filters.indices) return@updateFilters
                filters[index].compareValue = (filters[index].compareValue - 1).coerceAtLeast(0)
            }
            ACTION_EQ -> updateFilters { filters ->
                if (index !in filters.indices) return@updateFilters
                filters[index].equalitySignType = filters[index].equalitySignType.next()
            }
            else -> super.clickMenuButton(player, id)
        }
    }

    override fun quickMoveStack(player: Player, i: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean = stillValid(access, player, ColumbinaBlocks.WAREHOUSE_STOCK_LINKER)
}
