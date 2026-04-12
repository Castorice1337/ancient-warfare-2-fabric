package com.columbina.content.research.blockentity

import com.columbina.content.logistics.courier.DirectionalContainerView
import com.columbina.content.research.ImportedResearchRegistry
import com.columbina.content.research.block.ResearchStationBlock
import com.columbina.content.research.item.ResearchBookItem
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
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class ResearchStationBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.RESEARCH_STATION, pos, state), MenuProvider, ExtendedScreenHandlerFactory<BlockPos>, WorldlyContainer {
    companion object {
        private const val DEFAULT_MAX_ENERGY = 1600
        private const val DEFAULT_MAX_INPUT = 100
        private const val ENERGY_PER_RESEARCH_UNIT = 1
        private const val ENERGY_PER_WORK_UNIT = 50
        private const val START_CHECK_DELAY_MAX = 40
    }

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
    var maxEnergy: Int = DEFAULT_MAX_ENERGY
    var maxInput: Int = DEFAULT_MAX_INPUT
    var ownerName: String? = null
    var ownerUuid: String? = null

    private var startCheckDelay: Int = 0

    val menuData: ContainerData = object : ContainerData {
        override fun get(index: Int): Int {
            return when (index) {
                0 -> storedEnergy
                1 -> if (useAdjacentInventory) 1 else 0
                2 -> inventoryDirection.ordinal
                3 -> inventorySide.ordinal
                4 -> maxEnergy
                5 -> maxInput
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> storedEnergy = value
                1 -> useAdjacentInventory = value != 0
                2 -> inventoryDirection = Direction.values()[value.coerceIn(0, Direction.values().lastIndex)]
                3 -> inventorySide = Direction.values()[value.coerceIn(0, Direction.values().lastIndex)]
                4 -> maxEnergy = value
                5 -> maxInput = value
            }
        }

        override fun getCount(): Int = 6
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
        maxEnergy = input.getIntOr("maxEnergy", DEFAULT_MAX_ENERGY)
        maxInput = input.getIntOr("maxInput", DEFAULT_MAX_INPUT)
        startCheckDelay = input.getIntOr("startCheckDelay", 0)
        ownerName = input.getStringOr("ownerName", "").ifBlank { null }
        ownerUuid = input.getStringOr("ownerUuid", "").ifBlank { null }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        ContainerHelper.saveAllItems(output.child("bookInventory"), bookInventory.items)
        ContainerHelper.saveAllItems(output.child("resourceInventory"), resourceInventory.items)
        output.putInt("storedEnergy", storedEnergy)
        output.putBoolean("useAdjacentInventory", useAdjacentInventory)
        output.putInt("inventoryDirection", inventoryDirection.ordinal)
        output.putInt("inventorySide", inventorySide.ordinal)
        output.putInt("maxEnergy", maxEnergy)
        output.putInt("maxInput", maxInput)
        output.putInt("startCheckDelay", startCheckDelay)
        output.putString("ownerName", ownerName.orEmpty())
        output.putString("ownerUuid", ownerUuid.orEmpty())
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)

    fun getCrafterName(): String? = ResearchBookItem.getResearcherName(bookInventory.getItem(0))

    fun hasBook(): Boolean = getCrafterName() != null

    fun setOwner(player: Player) {
        ownerName = player.name.string
        ownerUuid = player.uuid.toString()
        setChanged()
    }

    fun canUse(player: Player): Boolean {
        if (ownerUuid.isNullOrBlank() && ownerName.isNullOrBlank()) {
            return true
        }
        return player.uuid.toString() == ownerUuid || player.name.string == ownerName
    }

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
        val crafterName = getCrafterName() ?: return
        val currentGoal = ResearchRuntimeService.getCurrentGoal(currentLevel, crafterName)

        if (currentGoal != null && storedEnergy >= ENERGY_PER_RESEARCH_UNIT) {
            workTick(currentLevel, crafterName, currentGoal, 1)
        } else if (currentGoal == null) {
            startCheckDelay--
            if (startCheckDelay <= 0) {
                tryStartNextResearch(currentLevel, crafterName)
            }
        }
    }

    private fun workTick(currentLevel: net.minecraft.server.level.ServerLevel, crafterName: String, goalId: String, tickCount: Int) {
        val goal = ImportedResearchRegistry.getGoal(goalId) ?: return
        val progress = ResearchRuntimeService.getProgress(currentLevel, crafterName) + tickCount

        if (progress >= goal.time) {
            ResearchRuntimeService.finishResearch(currentLevel, crafterName, goalId)
            tryStartNextResearch(currentLevel, crafterName)
        } else {
            ResearchRuntimeService.setProgress(currentLevel, crafterName, progress)
        }

        storedEnergy = (storedEnergy - ENERGY_PER_RESEARCH_UNIT).coerceAtLeast(0)
        setChanged()
    }

    private fun tryStartNextResearch(currentLevel: net.minecraft.server.level.ServerLevel, crafterName: String) {
        val queue = ResearchRuntimeService.getResearchQueueFor(currentLevel, crafterName)
        val goalId = queue.firstOrNull() ?: run {
            startCheckDelay = START_CHECK_DELAY_MAX
            return
        }
        val goal = ImportedResearchRegistry.getGoal(goalId) ?: run {
            startCheckDelay = START_CHECK_DELAY_MAX
            return
        }

        val started = goal.tryStart(
            primary = resourceInventory,
            adjacent = adjacentInventory(),
            useAdjacentInventory = useAdjacentInventory,
        )

        if (started) {
            ResearchRuntimeService.startResearch(currentLevel, crafterName, goalId)
        }

        startCheckDelay = START_CHECK_DELAY_MAX
        setChanged()
    }

    private fun adjacentInventory(): Container? {
        val currentLevel = level ?: return null
        val adjacentPos = blockPos.relative(inventoryDirection)
        val blockEntity = currentLevel.getBlockEntity(adjacentPos)
        return when (blockEntity) {
            is WorldlyContainer -> DirectionalContainerView(blockEntity, inventorySide)
            is Container -> blockEntity
            else -> null
        }
    }

    fun addEnergy(amount: Int) {
        storedEnergy = (storedEnergy + amount.coerceAtMost(maxInput)).coerceAtMost(maxEnergy)
        setChanged()
    }

    fun addEnergyFromWorker(workEffectiveness: Double) {
        addEnergy((ENERGY_PER_WORK_UNIT * workEffectiveness).toInt())
    }

    fun addEnergyFromPlayer() {
        addEnergy(ENERGY_PER_WORK_UNIT)
    }

    override fun getSlotsForFace(side: Direction): IntArray = IntArray(resourceInventory.containerSize) { it }

    override fun canPlaceItemThroughFace(slot: Int, stack: ItemStack, side: Direction?): Boolean = true

    override fun canTakeItemThroughFace(slot: Int, stack: ItemStack, side: Direction): Boolean = true

    override fun getContainerSize(): Int = resourceInventory.containerSize

    override fun isEmpty(): Boolean = resourceInventory.isEmpty

    override fun getItem(slot: Int): ItemStack = resourceInventory.getItem(slot)

    override fun removeItem(slot: Int, amount: Int): ItemStack = resourceInventory.removeItem(slot, amount)

    override fun removeItemNoUpdate(slot: Int): ItemStack = resourceInventory.removeItemNoUpdate(slot)

    override fun setItem(slot: Int, stack: ItemStack) = resourceInventory.setItem(slot, stack)

    override fun stillValid(player: Player): Boolean = resourceInventory.stillValid(player)

    override fun clearContent() = resourceInventory.clearContent()

    override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean = resourceInventory.canPlaceItem(slot, stack)

    // TODO Phase 4/5: restore real torque parity instead of only energy parity on the modern runtime host.
    fun addTorqueInput(amount: Int): Int {
        val accepted = amount.coerceAtMost(maxInput)
        addEnergy(accepted)
        return accepted
    }
}
