package com.columbina.content.logistics.screen

import com.columbina.content.logistics.warehouse.WarehouseInterfaceBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseInterfaceFilter
import com.columbina.content.logistics.warehouse.stackItemId
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class WarehouseInterfaceScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val blockPos: BlockPos,
    private val interfaceInventory: Container,
) : AbstractContainerMenu(ColumbinaScreenHandlers.WAREHOUSE_INTERFACE, syncId) {
    companion object {
        private const val FILTER_ACTION_STRIDE = 8
        private const val ACTION_SET_FILTER = 1
        private const val ACTION_CLEAR_FILTER = 2
        private const val ACTION_INC_FILTER = 3
        private const val ACTION_DEC_FILTER = 4
        private const val ACTION_INC_FILTER_FAST = 5
        private const val ACTION_DEC_FILTER_FAST = 6
        const val ACTION_ADD_FILTER = 200
    }

    private val access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos)
    private val blockEntity: WarehouseInterfaceBlockEntity? =
        playerInventory.player.level().getBlockEntity(blockPos) as? WarehouseInterfaceBlockEntity

    constructor(syncId: Int, playerInventory: Inventory, blockPos: BlockPos) : this(
        syncId,
        playerInventory,
        blockPos,
        (playerInventory.player.level().getBlockEntity(blockPos) as? WarehouseInterfaceBlockEntity)?.inventory ?: SimpleContainer(9),
    )

    constructor(syncId: Int, playerInventory: Inventory, blockEntity: WarehouseInterfaceBlockEntity) : this(
        syncId,
        playerInventory,
        blockEntity.blockPos,
        blockEntity.inventory,
    )

    init {
        for (row in 0 until 3) {
            for (column in 0 until 3) {
                addSlot(Slot(interfaceInventory, column + (row * 3), 98 + (column * 18), 98 + (row * 18)))
            }
        }

        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(playerInventory, column + (row * 9) + 9, 8 + (column * 18), 156 + (row * 18)))
            }
        }

        for (column in 0 until 9) {
            addSlot(Slot(playerInventory, column, 8 + (column * 18), 214))
        }
    }

    fun filters(): List<WarehouseInterfaceFilter> = blockEntity?.getFilters() ?: emptyList()

    private fun updateFilters(transform: (MutableList<WarehouseInterfaceFilter>) -> Unit): Boolean {
        val blockEntity = blockEntity ?: return false
        val filters = blockEntity.getFilters().toMutableList()
        transform(filters)
        blockEntity.setFilters(filters)
        return true
    }

    private fun selectedFilterItem(): ItemStack {
        val player = playerInventory.player
        return sequenceOf(player.mainHandItem, player.offhandItem).firstOrNull { !it.isEmpty }?.copy() ?: ItemStack.EMPTY
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        if (id == ACTION_ADD_FILTER) {
            return updateFilters { filters ->
                if (filters.size < 9) {
                    filters += WarehouseInterfaceFilter(filterQuantity = 64)
                }
            }
        }

        val filterIndex = id / FILTER_ACTION_STRIDE
        val action = id % FILTER_ACTION_STRIDE

        return when (action) {
            ACTION_SET_FILTER -> updateFilters { filters ->
                if (filterIndex !in filters.indices) return@updateFilters
                val stack = selectedFilterItem()
                filters[filterIndex] = WarehouseInterfaceFilter(stackItemId(stack), if (stack.isEmpty) 0 else stack.count.coerceAtLeast(1))
            }
            ACTION_CLEAR_FILTER -> updateFilters { filters ->
                if (filterIndex !in filters.indices) return@updateFilters
                filters.removeAt(filterIndex)
            }
            ACTION_INC_FILTER -> changeFilterQuantity(filterIndex, 1)
            ACTION_DEC_FILTER -> changeFilterQuantity(filterIndex, -1)
            ACTION_INC_FILTER_FAST -> changeFilterQuantity(filterIndex, 16)
            ACTION_DEC_FILTER_FAST -> changeFilterQuantity(filterIndex, -16)
            else -> super.clickMenuButton(player, id)
        }
    }

    private fun changeFilterQuantity(filterIndex: Int, delta: Int): Boolean {
        return updateFilters { filters ->
            if (filterIndex !in filters.indices) return@updateFilters
            val filter = filters[filterIndex]
            filter.filterQuantity = (filter.filterQuantity + delta).coerceIn(0, 999)
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) {
            return ItemStack.EMPTY
        }

        val stack = slot.item
        val copy = stack.copy()
        return if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size, false)) {
                ItemStack.EMPTY
            } else {
                if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
                copy
            }
        } else {
            if (!moveItemStackTo(stack, 0, 9, false)) {
                ItemStack.EMPTY
            } else {
                if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
                copy
            }
        }
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, ColumbinaBlocks.WAREHOUSE_INTERFACE)
    }
}
