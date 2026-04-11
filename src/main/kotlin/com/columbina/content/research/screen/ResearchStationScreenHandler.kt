package com.columbina.content.research.screen

import com.columbina.content.research.item.ResearchBookItem
import com.columbina.content.research.blockentity.ResearchStationBlockEntity
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaScreenHandlers
import com.columbina.runtime.research.ResearchRuntimeService
import net.minecraft.core.BlockPos
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.inventory.SimpleContainerData
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.Direction

class ResearchStationScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val blockPos: BlockPos,
    private val bookInventory: Container,
    private val resourceInventory: Container,
    private val stationData: ContainerData,
) : AbstractContainerMenu(ColumbinaScreenHandlers.RESEARCH_STATION, syncId) {
    companion object {
        const val BUTTON_TOGGLE_ADJACENT = 1
        const val BUTTON_CYCLE_DIRECTION = 2
        const val BUTTON_CYCLE_SIDE = 3
        const val BUTTON_QUEUE_ADD_BASE = 100
        const val BUTTON_QUEUE_REMOVE_BASE = 200
        const val VISIBLE_QUEUE_ACTIONS = 3
    }

    private val access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos)
    private val blockEntity: ResearchStationBlockEntity? = playerInventory.player.level().getBlockEntity(blockPos) as? ResearchStationBlockEntity

    val playerKey: String = playerInventory.player.name.string
    val researcherName: String
        get() = blockEntity?.getCrafterName() ?: playerKey

    val currentGoal: String?
        get() = serverLevel()?.let { ResearchRuntimeService.getCurrentGoal(it, researcherName) }

    val queuedResearch: List<String>
        get() = serverLevel()?.let { ResearchRuntimeService.getResearchQueueFor(it, researcherName) } ?: emptyList()

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
        addSlot(object : Slot(bookInventory, 0, 8, 22) {
            override fun mayPlace(stack: ItemStack): Boolean = ResearchBookItem.getResearcherName(stack) != null
        })

        var slotIndex = 0
        for (row in 0 until 3) {
            for (column in 0 until 3) {
                addSlot(Slot(resourceInventory, slotIndex, 26 + (column * 18), 98 + (row * 18)))
                slotIndex++
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

    private fun serverLevel(): ServerLevel? = playerInventory.player.level() as? ServerLevel

    private fun visibleResearchableGoals(): List<String> {
        val serverLevel = serverLevel() ?: return emptyList()
        return ResearchRuntimeService.getResearchableGoals(serverLevel, researcherName).sorted().take(VISIBLE_QUEUE_ACTIONS)
    }

    private fun visibleQueuedGoals(): List<String> = queuedResearch.take(VISIBLE_QUEUE_ACTIONS)

    override fun broadcastChanges() {
        blockEntity?.let {
            stationData.set(0, it.storedEnergy)
            stationData.set(1, if (it.useAdjacentInventory) 1 else 0)
            stationData.set(2, it.inventoryDirection.ordinal)
            stationData.set(3, it.inventorySide.ordinal)
        }

        super.broadcastChanges()
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) {
            return ItemStack.EMPTY
        }

        val stack = slot.item
        val copy = stack.copy()
        val playerInventoryStart = 10
        val playerInventoryEnd = slots.size

        when (index) {
            0 -> {
                if (!moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false)) {
                    return ItemStack.EMPTY
                }
            }
            in 1..9 -> {
                if (!moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false)) {
                    return ItemStack.EMPTY
                }
            }
            else -> {
                if (ResearchBookItem.getResearcherName(stack) != null) {
                    if (!moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY
                    }
                } else if (!moveItemStackTo(stack, 1, 10, false)) {
                    return ItemStack.EMPTY
                }
            }
        }

        if (stack.isEmpty) {
            slot.setByPlayer(ItemStack.EMPTY)
        } else {
            slot.setChanged()
        }

        return copy
    }

    fun toggleUseAdjacentInventory(): Boolean {
        val station = blockEntity ?: return false
        station.useAdjacentInventory = !station.useAdjacentInventory
        station.setChanged()
        broadcastChanges()
        return true
    }

    fun cycleInventoryDirection(): Boolean {
        val station = blockEntity ?: return false
        val directions = Direction.values()
        station.inventoryDirection = directions[(station.inventoryDirection.ordinal + 1) % directions.size]
        station.setChanged()
        broadcastChanges()
        return true
    }

    fun cycleInventorySide(): Boolean {
        val station = blockEntity ?: return false
        val directions = Direction.values()
        station.inventorySide = directions[(station.inventorySide.ordinal + 1) % directions.size]
        station.setChanged()
        broadcastChanges()
        return true
    }

    fun queueResearch(goal: String): Boolean {
        val serverLevel = serverLevel() ?: return false
        ResearchRuntimeService.queueGoal(serverLevel, researcherName, goal)
        broadcastChanges()
        return true
    }

    fun removeQueuedResearch(goal: String): Boolean {
        val serverLevel = serverLevel() ?: return false
        ResearchRuntimeService.removeQueuedGoal(serverLevel, researcherName, goal)
        broadcastChanges()
        return true
    }

    fun onDirPressed(): Boolean = cycleInventoryDirection()

    fun onSidePressed(): Boolean = cycleInventorySide()

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        return when (id) {
            BUTTON_TOGGLE_ADJACENT -> toggleUseAdjacentInventory()
            BUTTON_CYCLE_DIRECTION -> cycleInventoryDirection()
            BUTTON_CYCLE_SIDE -> cycleInventorySide()
            in BUTTON_QUEUE_ADD_BASE until BUTTON_QUEUE_ADD_BASE + VISIBLE_QUEUE_ACTIONS -> {
                val goals = visibleResearchableGoals()
                val index = id - BUTTON_QUEUE_ADD_BASE
                if (index in goals.indices) queueResearch(goals[index]) else false
            }
            in BUTTON_QUEUE_REMOVE_BASE until BUTTON_QUEUE_REMOVE_BASE + VISIBLE_QUEUE_ACTIONS -> {
                val goals = visibleQueuedGoals()
                val index = id - BUTTON_QUEUE_REMOVE_BASE
                if (index in goals.indices) removeQueuedResearch(goals[index]) else false
            }
            else -> super.clickMenuButton(player, id)
        }
    }

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, ColumbinaBlocks.RESEARCH_STATION)
    }
}
