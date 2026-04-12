package com.columbina.content.structure.screen

import com.columbina.content.structure.blockentity.DraftingStationBlockEntity
import com.columbina.content.structure.template.ImportedTemplateBuildResource
import com.columbina.content.structure.template.ImportedTemplateRegistry
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

class DraftingStationScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val blockPos: BlockPos,
    private val inputSlots: Container,
    private val outputSlot: Container,
) : AbstractContainerMenu(ColumbinaScreenHandlers.DRAFTING_STATION, syncId) {
    companion object {
        const val BUTTON_START = 1
        const val BUTTON_STOP = 2
        const val BUTTON_TEMPLATE_BASE = 100
        const val VISIBLE_TEMPLATES = 8
    }

    private val access = ContainerLevelAccess.create(playerInventory.player.level(), blockPos)
    private val blockEntity: DraftingStationBlockEntity? =
        playerInventory.player.level().getBlockEntity(blockPos) as? DraftingStationBlockEntity

    constructor(syncId: Int, playerInventory: Inventory, blockPos: BlockPos) : this(
        syncId,
        playerInventory,
        blockPos,
        (playerInventory.player.level().getBlockEntity(blockPos) as? DraftingStationBlockEntity)?.inputSlots ?: SimpleContainer(27),
        (playerInventory.player.level().getBlockEntity(blockPos) as? DraftingStationBlockEntity)?.outputSlot ?: SimpleContainer(1),
    )

    constructor(syncId: Int, playerInventory: Inventory, blockEntity: DraftingStationBlockEntity) : this(
        syncId,
        playerInventory,
        blockEntity.blockPos,
        blockEntity.inputSlots,
        blockEntity.outputSlot,
    )

    init {
        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(inputSlots, column + (row * 9), 8 + (column * 18), 78 + (row * 18)))
            }
        }

        addSlot(object : Slot(outputSlot, 0, 80, 24) {
            override fun mayPlace(stack: ItemStack): Boolean = false
        })

        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(Slot(playerInventory, column + (row * 9) + 9, 220 + (column * 18), 78 + (row * 18)))
            }
        }

        for (column in 0 until 9) {
            addSlot(Slot(playerInventory, column, 220 + (column * 18), 136))
        }
    }

    val structureName: String?
        get() = blockEntity?.structureName

    val isStarted: Boolean
        get() = blockEntity?.isStarted ?: false

    val isFinished: Boolean
        get() = blockEntity?.isFinished ?: false

    val remainingTime: Int
        get() = blockEntity?.remainingTime ?: 0

    val totalTime: Int
        get() = blockEntity?.totalTime ?: 0

    val neededResources: List<ImportedTemplateBuildResource>
        get() = blockEntity?.getNeededResources() ?: emptyList()

    fun availableTemplates(): List<String> = ImportedTemplateRegistry.getSurvivalTemplates().sorted().take(VISIBLE_TEMPLATES)

    fun handleStartInput(): Boolean {
        blockEntity?.tryStart()
        return true
    }

    fun handleStopInput(): Boolean {
        blockEntity?.stopCurrentWork()
        return true
    }

    fun selectStructure(structure: String): Boolean {
        blockEntity?.setTemplate(structure)
        return true
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        return when (id) {
            BUTTON_START -> handleStartInput()
            BUTTON_STOP -> handleStopInput()
            in BUTTON_TEMPLATE_BASE until BUTTON_TEMPLATE_BASE + VISIBLE_TEMPLATES -> {
                val index = id - BUTTON_TEMPLATE_BASE
                val template = availableTemplates().getOrNull(index) ?: return false
                selectStructure(template)
            }
            else -> super.clickMenuButton(player, id)
        }
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) {
            return ItemStack.EMPTY
        }

        val stack = slot.item
        val copy = stack.copy()
        val outputIndex = 27
        val playerStart = 28

        when {
            index == outputIndex -> {
                if (!moveItemStackTo(stack, playerStart, slots.size, false)) {
                    return ItemStack.EMPTY
                }
            }
            index < outputIndex -> {
                if (!moveItemStackTo(stack, playerStart, slots.size, false)) {
                    return ItemStack.EMPTY
                }
            }
            else -> {
                if (!moveItemStackTo(stack, 0, outputIndex, false)) {
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

    override fun stillValid(player: Player): Boolean {
        return stillValid(access, player, ColumbinaBlocks.DRAFTING_STATION)
    }
}
