package com.columbina.content.logistics.screen

import com.columbina.content.logistics.warehouse.WarehouseControllerBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseSortOrder
import com.columbina.content.logistics.warehouse.WarehouseSortType
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ClickType
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class WarehouseControlScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val blockPos: BlockPos,
    private val displayContainer: Container,
    private val controllerData: ContainerData,
) : AbstractContainerMenu(ColumbinaScreenHandlers.WAREHOUSE_CONTROL, syncId) {
    companion object {
        const val BUTTON_SORT_TYPE = 1
        const val BUTTON_SORT_ORDER = 2
        private const val DISPLAY_ROWS = 6
        private const val DISPLAY_COLUMNS = 9
        private const val DISPLAY_SLOT_COUNT = DISPLAY_ROWS * DISPLAY_COLUMNS
    }

    private val access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos)
    private val blockEntity: WarehouseControllerBlockEntity? =
        playerInventory.player.level().getBlockEntity(blockPos) as? WarehouseControllerBlockEntity

    constructor(syncId: Int, playerInventory: Inventory, blockPos: BlockPos) : this(
        syncId,
        playerInventory,
        blockPos,
        (playerInventory.player.level().getBlockEntity(blockPos) as? WarehouseControllerBlockEntity)?.displayInventory ?: SimpleContainer(DISPLAY_SLOT_COUNT),
        (playerInventory.player.level().getBlockEntity(blockPos) as? WarehouseControllerBlockEntity)?.menuData ?: SimpleContainerData(4),
    )

    constructor(syncId: Int, playerInventory: Inventory, blockEntity: WarehouseControllerBlockEntity) : this(
        syncId,
        playerInventory,
        blockEntity.blockPos,
        blockEntity.displayInventory,
        blockEntity.menuData,
    )

    init {
        for (row in 0 until DISPLAY_ROWS) {
            for (column in 0 until DISPLAY_COLUMNS) {
                addSlot(object : Slot(displayContainer, column + (row * DISPLAY_COLUMNS), 8 + (column * 18), 18 + (row * 18)) {
                    override fun mayPlace(stack: ItemStack): Boolean = false
                })
            }
        }

        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(playerInventory, column + (row * 9) + 9, 8 + (column * 18), 140 + (row * 18)))
            }
        }

        for (column in 0 until 9) {
            addSlot(Slot(playerInventory, column, 8 + (column * 18), 198))
        }

        addDataSlots(controllerData)
    }

    val currentStored: Int
        get() = controllerData.get(0)

    val maxStorage: Int
        get() = controllerData.get(1)

    val sortType: WarehouseSortType
        get() = WarehouseSortType.entries.getOrElse(controllerData.get(2)) { WarehouseSortType.NAME }

    val sortOrder: WarehouseSortOrder
        get() = WarehouseSortOrder.entries.getOrElse(controllerData.get(3)) { WarehouseSortOrder.DESCENDING }

    fun refreshFromBlockEntity() {
        val controller = blockEntity ?: return
        for (slot in 0 until DISPLAY_SLOT_COUNT) {
            displayContainer.setItem(slot, controller.displayInventory.getItem(slot).copy())
        }
    }

    override fun broadcastChanges() {
        blockEntity?.let {
            controllerData.set(0, it.menuData.get(0))
            controllerData.set(1, it.menuData.get(1))
            controllerData.set(2, it.menuData.get(2))
            controllerData.set(3, it.menuData.get(3))
        }
        super.broadcastChanges()
    }

    override fun clicked(slotId: Int, button: Int, clickType: ClickType, player: Player) {
        if (slotId in 0 until DISPLAY_SLOT_COUNT) {
            val filter = displayContainer.getItem(slotId)
            if (!filter.isEmpty) {
                blockEntity?.handleSlotClick(player, filter, clickType == ClickType.QUICK_MOVE, button == 1)
                refreshFromBlockEntity()
                broadcastChanges()
            }
            return
        }

        super.clicked(slotId, button, clickType, player)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) {
            return ItemStack.EMPTY
        }

        val stack = slot.item
        val copy = stack.copy()

        if (index < DISPLAY_SLOT_COUNT) {
            blockEntity?.handleSlotClick(player, stack, true, false)
            refreshFromBlockEntity()
            return copy
        }

        val controller = blockEntity ?: return ItemStack.EMPTY
        val remainder = controller.tryAdd(stack.copy())
        val moved = stack.count - remainder.count
        if (moved <= 0) {
            return ItemStack.EMPTY
        }

        if (remainder.isEmpty) {
            slot.setByPlayer(ItemStack.EMPTY)
        } else {
            slot.set(remainder)
        }
        slot.setChanged()
        refreshFromBlockEntity()
        broadcastChanges()
        return copy
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val controller = blockEntity ?: return false
        return when (id) {
            BUTTON_SORT_TYPE -> {
                controller.cycleSortType()
                refreshFromBlockEntity()
                true
            }
            BUTTON_SORT_ORDER -> {
                controller.cycleSortOrder()
                refreshFromBlockEntity()
                true
            }
            else -> super.clickMenuButton(player, id)
        }
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, ColumbinaBlocks.WAREHOUSE_CONTROL)
    }
}
