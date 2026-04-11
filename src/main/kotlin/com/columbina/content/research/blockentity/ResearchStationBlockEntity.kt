package com.columbina.content.research.blockentity

import com.columbina.content.research.block.ResearchStationBlock
import com.columbina.content.research.screen.ResearchStationScreenHandler
import com.columbina.runtime.init.ColumbinaBlockEntities
import com.columbina.runtime.research.ResearchRuntimeService
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class ResearchStationBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.RESEARCH_STATION, pos, state), MenuProvider, ExtendedScreenHandlerFactory<BlockPos> {
    private class TrackingContainer(size: Int, private val onChanged: () -> Unit) : SimpleContainer(size) {
        override fun setChanged() {
            super.setChanged()
            onChanged()
        }
    }

    val bookInventory: SimpleContainer = TrackingContainer(1, ::onInventoryChanged)
    val resourceInventory: SimpleContainer = TrackingContainer(9, ::onInventoryChanged)

    var storedEnergy: Int = 0
    var useAdjacentInventory: Boolean = false
    var inventoryDirection: Direction = Direction.NORTH
    var inventorySide: Direction = Direction.NORTH

    val menuData: ContainerData = object : ContainerData {
        override fun get(index: Int): Int {
            return when (index) {
                0 -> storedEnergy
                1 -> if (useAdjacentInventory) 1 else 0
                2 -> inventoryDirection.ordinal
                3 -> inventorySide.ordinal
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> storedEnergy = value
                1 -> useAdjacentInventory = value != 0
                2 -> inventoryDirection = Direction.values()[value.coerceIn(0, Direction.values().lastIndex)]
                3 -> inventorySide = Direction.values()[value.coerceIn(0, Direction.values().lastIndex)]
            }
        }

        override fun getCount(): Int = 4
    }

    override fun getDisplayName(): Component = Component.translatable("guistrings.research.research_queue")

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return ResearchStationScreenHandler(syncId, playerInventory, this)
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = blockPos

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        ContainerHelper.loadAllItems(input.childOrEmpty("bookInventory"), bookInventory.items)
        ContainerHelper.loadAllItems(input.childOrEmpty("resourceInventory"), resourceInventory.items)
        storedEnergy = input.getIntOr("storedEnergy", 0)
        useAdjacentInventory = input.getBooleanOr("useAdjacentInventory", false)
        inventoryDirection = Direction.values()[input.getIntOr("inventoryDirection", Direction.NORTH.ordinal).coerceIn(0, Direction.values().lastIndex)]
        inventorySide = Direction.values()[input.getIntOr("inventorySide", Direction.NORTH.ordinal).coerceIn(0, Direction.values().lastIndex)]
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        ContainerHelper.saveAllItems(output.child("bookInventory"), bookInventory.items)
        ContainerHelper.saveAllItems(output.child("resourceInventory"), resourceInventory.items)
        output.putInt("storedEnergy", storedEnergy)
        output.putBoolean("useAdjacentInventory", useAdjacentInventory)
        output.putInt("inventoryDirection", inventoryDirection.ordinal)
        output.putInt("inventorySide", inventorySide.ordinal)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)

    fun hasBook(): Boolean = !bookInventory.getItem(0).isEmpty

    private fun onInventoryChanged() {
        setChanged()

        val currentLevel = level ?: return
        val currentState = blockState

        if (!currentLevel.isClientSide && currentState.hasProperty(ResearchStationBlock.HAS_BOOK)) {
            currentLevel.setBlock(
                blockPos,
                currentState.setValue(ResearchStationBlock.HAS_BOOK, hasBook()),
                Block.UPDATE_CLIENTS,
            )
        }
    }

    fun tick() {
        val currentLevel = level as? net.minecraft.server.level.ServerLevel ?: return
        val playerKey = currentLevel.players().firstOrNull()?.let(ResearchRuntimeService::playerKey) ?: return
        val snapshot = ResearchRuntimeService.getSnapshot(currentLevel, playerKey)

        if (snapshot.currentResearch != null && storedEnergy < Int.MAX_VALUE) {
            storedEnergy += 1
            setChanged()
        }
    }
}
