package com.columbina.content.logistics.warehouse

import com.columbina.content.logistics.OwnedLogisticsTarget
import com.columbina.runtime.init.ColumbinaBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.Container
import net.minecraft.world.ContainerHelper
import net.minecraft.world.SimpleContainer
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class WarehouseStorageBlockEntity(
    pos: BlockPos,
    state: BlockState,
    private val tier: WarehouseStorageTier,
) : BlockEntity(ColumbinaBlockEntities.WAREHOUSE_STORAGE, pos, state), ControlledWarehouseTile, OwnedLogisticsTarget, WorldlyContainer {
    companion object {
        fun fromState(pos: BlockPos, state: BlockState): WarehouseStorageBlockEntity {
            val tier = when (state.block) {
                com.columbina.runtime.init.ColumbinaBlocks.WAREHOUSE_STORAGE_MEDIUM -> WarehouseStorageTier.MEDIUM
                com.columbina.runtime.init.ColumbinaBlocks.WAREHOUSE_STORAGE_LARGE -> WarehouseStorageTier.LARGE
                else -> WarehouseStorageTier.SMALL
            }
            return WarehouseStorageBlockEntity(pos, state, tier)
        }
    }

    private class TrackingContainer(size: Int, private val onChanged: () -> Unit) : SimpleContainer(size) {
        override fun setChanged() {
            super.setChanged()
            onChanged()
        }
    }

    override var controllerPos: BlockPos? = null
    override var ownerName: String? = null
    override var ownerUuid: String? = null

    val inventory: SimpleContainer = TrackingContainer(tier.slotCount, ::onInventoryChanged)
    private val filters = mutableListOf<WarehouseStorageFilter>()
    private var attemptedControllerResolve = false

    fun tick() {
        val currentLevel = level ?: return
        if (!currentLevel.isClientSide && !attemptedControllerResolve) {
            attemptedControllerResolve = true
            getController(currentLevel)?.addControlledTile(this)
        }
    }

    fun getStorageAdditionSize(): Int = inventory.containerSize * 64

    fun getFilters(): List<WarehouseStorageFilter> = filters.toList()

    fun setFilters(newFilters: List<WarehouseStorageFilter>) {
        val old = filters.toList()
        filters.clear()
        filters.addAll(newFilters)
        getController(level)?.onStorageFilterChanged(this, old, filters)
        onInventoryChanged()
    }

    fun addItems(target: MutableMap<WarehouseItemKey, Int>) {
        for (slot in 0 until inventory.containerSize) {
            val stack = inventory.getItem(slot)
            val key = WarehouseItemKey.fromStack(stack) ?: continue
            target[key] = (target[key] ?: 0) + stack.count
        }
    }

    fun getQuantityStored(filter: net.minecraft.world.item.ItemStack): Int {
        val itemId = stackItemId(filter) ?: return 0
        return countOf(inventory, itemId)
    }

    fun getAvailableSpaceFor(filter: net.minecraft.world.item.ItemStack): Int {
        if (filter.isEmpty) {
            return 0
        }

        var remaining = 0
        for (slot in 0 until inventory.containerSize) {
            val stack = inventory.getItem(slot)
            if (stack.isEmpty) {
                remaining += filter.maxStackSize
            } else if (net.minecraft.world.item.ItemStack.isSameItemSameComponents(stack, filter)) {
                remaining += (stack.maxStackSize - stack.count).coerceAtLeast(0)
            }
        }
        return remaining
    }

    fun extractItem(filter: net.minecraft.world.item.ItemStack, amount: Int): Int {
        val removed = removeMatching(inventory, filter, amount)
        if (!removed.isEmpty) {
            onInventoryChanged()
        }
        return removed.count
    }

    fun insertItem(filter: net.minecraft.world.item.ItemStack, amount: Int): Int {
        if (filter.isEmpty || amount <= 0) {
            return 0
        }

        val stack = filter.copy()
        stack.count = amount
        val inserted = insertStack(inventory, stack)
        if (inserted > 0) {
            onInventoryChanged()
        }
        return inserted
    }

    override fun getSlotsForFace(side: net.minecraft.core.Direction): IntArray = IntArray(inventory.containerSize) { it }

    override fun canPlaceItemThroughFace(slot: Int, stack: net.minecraft.world.item.ItemStack, side: net.minecraft.core.Direction?): Boolean = true

    override fun canTakeItemThroughFace(slot: Int, stack: net.minecraft.world.item.ItemStack, side: net.minecraft.core.Direction): Boolean = true

    override fun getContainerSize(): Int = inventory.containerSize

    override fun isEmpty(): Boolean = inventory.isEmpty

    override fun getItem(slot: Int): net.minecraft.world.item.ItemStack = inventory.getItem(slot)

    override fun removeItem(slot: Int, amount: Int): net.minecraft.world.item.ItemStack = inventory.removeItem(slot, amount)

    override fun removeItemNoUpdate(slot: Int): net.minecraft.world.item.ItemStack = inventory.removeItemNoUpdate(slot)

    override fun setItem(slot: Int, stack: net.minecraft.world.item.ItemStack) = inventory.setItem(slot, stack)

    override fun stillValid(player: Player): Boolean = inventory.stillValid(player)

    override fun clearContent() = inventory.clearContent()

    override fun canPlaceItem(slot: Int, stack: net.minecraft.world.item.ItemStack): Boolean = inventory.canPlaceItem(slot, stack)

    fun tryAdd(stack: net.minecraft.world.item.ItemStack): net.minecraft.world.item.ItemStack {
        val inserted = insertItem(stack, stack.count)
        val remainder = stack.copy()
        remainder.shrink(inserted)
        return if (remainder.isEmpty) net.minecraft.world.item.ItemStack.EMPTY else remainder
    }

    private fun onInventoryChanged() {
        setChanged()
        getController(level)?.refreshCachedState()
    }

    override fun isValidController(controller: WarehouseControllerBlockEntity): Boolean {
        val min = controller.getWorkBoundsMin()
        val max = controller.getWorkBoundsMax()
        return blockPos.x in min.x..max.x && blockPos.y in min.y..max.y && blockPos.z in min.z..max.z
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        ContainerHelper.loadAllItems(input.childOrEmpty("inventory"), inventory.items)
        val hasController = input.getBooleanOr("hasController", false)
        if (hasController) {
            controllerPos = BlockPos(
                input.getIntOr("controllerX", blockPos.x),
                input.getIntOr("controllerY", blockPos.y),
                input.getIntOr("controllerZ", blockPos.z),
            )
        } else {
            controllerPos = null
        }
        filters.clear()
        filters.addAll(decodeStorageFilters(input.getStringOr("filters", "")))
        ownerName = input.getStringOr("ownerName", "").ifBlank { null }
        ownerUuid = input.getStringOr("ownerUuid", "").ifBlank { null }
        attemptedControllerResolve = false
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        ContainerHelper.saveAllItems(output.child("inventory"), inventory.items)
        output.putBoolean("hasController", controllerPos != null)
        controllerPos?.let {
            output.putInt("controllerX", it.x)
            output.putInt("controllerY", it.y)
            output.putInt("controllerZ", it.z)
        }
        output.putString("filters", encodeStorageFilters(filters))
        output.putString("ownerName", ownerName.orEmpty())
        output.putString("ownerUuid", ownerUuid.orEmpty())
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)
}
