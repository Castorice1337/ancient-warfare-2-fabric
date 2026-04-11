package com.columbina.content.research.screen

import com.columbina.content.research.blockentity.ResearchStationBlockEntity
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

class ResearchStationScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val blockPos: BlockPos,
    private val bookInventory: Container,
    private val resourceInventory: Container,
    private val stationData: ContainerData,
) : AbstractContainerMenu(ColumbinaScreenHandlers.RESEARCH_STATION, syncId) {
    private val access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos)
    private val blockEntity: ResearchStationBlockEntity? = playerInventory.player.level().getBlockEntity(blockPos) as? ResearchStationBlockEntity

    val playerKey: String = playerInventory.player.name.string

    constructor(syncId: Int, playerInventory: Inventory, blockPos: BlockPos) : this(
        syncId = syncId,
        playerInventory = playerInventory,
        blockPos = blockPos,
        bookInventory = SimpleContainer(1),
        resourceInventory = SimpleContainer(9),
        stationData = SimpleContainerData(4),
    )

    constructor(syncId: Int, playerInventory: Inventory, blockEntity: ResearchStationBlockEntity) : this(
        syncId = syncId,
        playerInventory = playerInventory,
        blockPos = blockEntity.blockPos,
        bookInventory = blockEntity.bookInventory,
        resourceInventory = blockEntity.resourceInventory,
        stationData = blockEntity.menuData,
    )

    init {
        addSlot(Slot(bookInventory, 0, 8, 18))

        var slotIndex = 0
        for (row in 0 until 3) {
            for (column in 0 until 3) {
                addSlot(Slot(resourceInventory, slotIndex, 62 + (column * 18), 18 + (row * 18)))
                slotIndex++
            }
        }

        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(playerInventory, column + (row * 9) + 9, 8 + (column * 18), 84 + (row * 18)))
            }
        }

        for (column in 0 until 9) {
            addSlot(Slot(playerInventory, column, 8 + (column * 18), 142))
        }

        addDataSlots(stationData)
    }

    val storedEnergy: Int
        get() = stationData.get(0)

    val useAdjacentInventory: Boolean
        get() = stationData.get(1) != 0

    val inventoryDirectionOrdinal: Int
        get() = stationData.get(2)

    val inventorySideOrdinal: Int
        get() = stationData.get(3)

    override fun broadcastChanges() {
        blockEntity?.let {
            stationData.set(0, it.storedEnergy)
            stationData.set(1, if (it.useAdjacentInventory) 1 else 0)
            stationData.set(2, it.inventoryDirection.ordinal)
            stationData.set(3, it.inventorySide.ordinal)
        }

        super.broadcastChanges()
    }

    override fun quickMoveStack(player: net.minecraft.world.entity.player.Player, index: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: net.minecraft.world.entity.player.Player): Boolean {
        return stillValid(access, player, ColumbinaBlocks.RESEARCH_STATION)
    }
}
