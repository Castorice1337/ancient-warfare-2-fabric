package com.columbina.content.structure.blockentity

import com.columbina.content.structure.item.StructureBuilderItem
import com.columbina.content.structure.screen.DraftingStationScreenHandler
import com.columbina.content.structure.template.ImportedStructureTemplate
import com.columbina.content.structure.template.ImportedTemplateBuildResource
import com.columbina.content.structure.template.ImportedTemplateRegistry
import com.columbina.runtime.init.ColumbinaBlockEntities
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.ContainerHelper
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class DraftingStationBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.DRAFTING_STATION, pos, state), MenuProvider, ExtendedScreenHandlerFactory<BlockPos> {
    private class TrackingContainer(size: Int, private val onChanged: () -> Unit) : SimpleContainer(size) {
        override fun setChanged() {
            super.setChanged()
            onChanged()
        }
    }

    val inputSlots: SimpleContainer = TrackingContainer(27, ::onInventoryChanged)
    val outputSlot: SimpleContainer = TrackingContainer(1, ::onInventoryChanged)

    var structureName: String? = null
        private set
    var isStarted: Boolean = false
        private set
    var isFinished: Boolean = false
        private set
    var remainingTime: Int = 0
        private set
    var totalTime: Int = 0
        private set

    private val buildResources = mutableListOf<ImportedTemplateBuildResource>()

    fun tick() {
        val currentLevel = level ?: return
        if (currentLevel.isClientSide) {
            return
        }

        val currentTemplateName = structureName
        if (currentTemplateName != null && !ImportedTemplateRegistry.templateExists(currentTemplateName)) {
            stopCurrentWork()
            return
        }

        if (currentTemplateName == null || !isStarted) {
            return
        }

        if (!isFinished && tryRemoveResource()) {
            isFinished = true
        }

        if (isFinished && tryFinish()) {
            stopCurrentWork()
        }
    }

    fun getNeededResources(): List<ImportedTemplateBuildResource> = buildResources.toList()

    fun getTemplate(): ImportedStructureTemplate? = structureName?.let(ImportedTemplateRegistry::getTemplate)

    fun setTemplate(templateName: String) {
        if (isStarted) {
            return
        }

        structureName = null
        buildResources.clear()
        remainingTime = 0
        totalTime = 0
        isFinished = false

        ImportedTemplateRegistry.getTemplate(templateName)?.let { template ->
            if (template.isSurvival) {
                structureName = templateName
            }
            buildResources.addAll(template.getResourceList().map { it.copy() })
            recalcTimes()
        }
        setChanged()
    }

    fun tryStart() {
        if (structureName != null && getTemplate() != null) {
            isStarted = true
            setChanged()
        }
    }

    fun stopCurrentWork() {
        structureName = null
        buildResources.clear()
        remainingTime = 0
        totalTime = 0
        isFinished = false
        isStarted = false
        setChanged()
    }

    override fun getDisplayName(): Component = Component.translatable("tile.drafting_station.name")

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return DraftingStationScreenHandler(syncId, playerInventory, this)
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = blockPos

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        ContainerHelper.loadAllItems(input.childOrEmpty("inputSlots"), inputSlots.items)
        ContainerHelper.loadAllItems(input.childOrEmpty("outputSlot"), outputSlot.items)
        structureName = input.getStringOr("structureName", "").ifBlank { null }
        isStarted = input.getBooleanOr("isStarted", false)
        isFinished = input.getBooleanOr("isFinished", false)
        remainingTime = input.getIntOr("remainingTime", 0)
        totalTime = input.getIntOr("totalTime", 0)
        buildResources.clear()
        input.getStringOr("buildResources", "")
            .split(';')
            .filter(String::isNotBlank)
            .forEach { encoded ->
                val parts = encoded.split('|', limit = 2)
                val itemId = parts.getOrNull(0).orEmpty()
                val count = parts.getOrNull(1)?.toIntOrNull() ?: 0
                if (itemId.isNotBlank() && count > 0) {
                    buildResources += ImportedTemplateBuildResource(itemId, count)
                }
            }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        ContainerHelper.saveAllItems(output.child("inputSlots"), inputSlots.items)
        ContainerHelper.saveAllItems(output.child("outputSlot"), outputSlot.items)
        output.putString("structureName", structureName.orEmpty())
        output.putBoolean("isStarted", isStarted)
        output.putBoolean("isFinished", isFinished)
        output.putInt("remainingTime", remainingTime)
        output.putInt("totalTime", totalTime)
        output.putString("buildResources", buildResources.joinToString(";") { "${it.itemId}|${it.count}" })
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)

    private fun tryRemoveResource(): Boolean {
        for (slot in 0 until inputSlots.containerSize) {
            val inventoryStack = inputSlots.getItem(slot)
            if (inventoryStack.isEmpty) {
                continue
            }
            if (removeBuildResource(inventoryStack)) {
                inventoryStack.shrink(1)
                if (inventoryStack.isEmpty) {
                    inputSlots.setItem(slot, net.minecraft.world.item.ItemStack.EMPTY)
                }
                break
            }
        }
        return buildResources.isEmpty()
    }

    private fun removeBuildResource(inventoryStack: net.minecraft.world.item.ItemStack): Boolean {
        val inventoryItemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inventoryStack.item).toString()
        val index = buildResources.indexOfFirst { it.itemId == inventoryItemId }
        if (index == -1) {
            return false
        }
        val resource = buildResources[index]
        val updated = resource.copy(count = resource.count - 1)
        remainingTime = (remainingTime - 1).coerceAtLeast(0)
        if (updated.count <= 0) {
            buildResources.removeAt(index)
        } else {
            buildResources[index] = updated
        }
        setChanged()
        return true
    }

    private fun tryFinish(): Boolean {
        if (!outputSlot.getItem(0).isEmpty) {
            return false
        }
        val templateName = structureName ?: return false
        outputSlot.setItem(0, StructureBuilderItem.createStack(templateName))
        outputSlot.setChanged()
        return true
    }

    private fun recalcTimes() {
        totalTime = buildResources.sumOf { it.count }
        remainingTime = totalTime
    }

    private fun onInventoryChanged() {
        setChanged()
    }
}
