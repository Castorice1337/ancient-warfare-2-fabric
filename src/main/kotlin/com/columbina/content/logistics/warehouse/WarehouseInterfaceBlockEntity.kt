package com.columbina.content.logistics.warehouse

import com.columbina.runtime.init.ColumbinaBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.ContainerHelper
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class WarehouseInterfaceBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.WAREHOUSE_INTERFACE, pos, state), ControlledWarehouseTile {
    data class InterfaceFillRequest(val requestedItem: ItemStack, val requestAmount: Int)
    data class InterfaceEmptyRequest(val slotNum: Int, val count: Int)

    private class TrackingContainer(size: Int, private val onChanged: () -> Unit) : SimpleContainer(size) {
        override fun setChanged() {
            super.setChanged()
            onChanged()
        }
    }

    override var controllerPos: BlockPos? = null

    val inventory: SimpleContainer = TrackingContainer(9, ::onInventoryChanged)
    private val fillRequests = mutableListOf<InterfaceFillRequest>()
    private val emptyRequests = mutableListOf<InterfaceEmptyRequest>()
    private val filters = mutableListOf<WarehouseInterfaceFilter>()
    private var initialized = false

    fun tick() {
        val currentLevel = level ?: return
        if (!currentLevel.isClientSide && !initialized) {
            initialized = true
            recalcRequests()
            getController(currentLevel)?.addControlledTile(this)
        }
    }

    fun getFillRequests(): List<InterfaceFillRequest> = fillRequests.toList()

    fun getEmptyRequests(): List<InterfaceEmptyRequest> = emptyRequests.toList()

    fun getFilters(): List<WarehouseInterfaceFilter> = filters.toList()

    fun setFilters(newFilters: List<WarehouseInterfaceFilter>) {
        filters.clear()
        filters.addAll(newFilters)
        recalcRequests()
    }

    fun recalcRequests() {
        val currentLevel = level ?: return
        if (currentLevel.isClientSide) {
            return
        }

        fillRequests.clear()
        emptyRequests.clear()

        for (slot in 0 until inventory.containerSize) {
            val stack = inventory.getItem(slot)
            if (stack.isEmpty) {
                continue
            }

            if (!matchesFilter(stack)) {
                emptyRequests += InterfaceEmptyRequest(slot, stack.count)
            } else {
                val max = getFilterQuantity(stack)
                val count = countOf(inventory, stackItemId(stack).orEmpty())
                if (count > max) {
                    emptyRequests += InterfaceEmptyRequest(slot, count - max)
                }
            }
        }

        filters.forEach { filter ->
            val itemId = filter.itemId ?: return@forEach
            val count = countOf(inventory, itemId)
            if (count < filter.filterQuantity) {
                fillRequests += InterfaceFillRequest(stackFromItemId(itemId, filter.filterQuantity), filter.filterQuantity - count)
            }
        }

        setChanged()
        getController(currentLevel)?.onInterfaceInventoryChanged(this)
    }

    private fun matchesFilter(stack: ItemStack): Boolean = filters.any { it.apply(stack) }

    private fun getFilterQuantity(stack: ItemStack): Int {
        return filters.filter { it.apply(stack) }.sumOf { it.filterQuantity }
    }

    private fun onInventoryChanged() {
        recalcRequests()
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
        filters.addAll(decodeInterfaceFilters(input.getStringOr("filters", "")))
        initialized = false
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
        output.putString("filters", encodeInterfaceFilters(filters))
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)
}
