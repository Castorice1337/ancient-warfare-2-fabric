package com.columbina.content.logistics.warehouse

import com.columbina.content.logistics.OwnedLogisticsTarget
import com.columbina.content.logistics.screen.WarehouseStockLinkerScreenHandler
import com.columbina.runtime.init.ColumbinaBlockEntities
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class WarehouseStockLinkerBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.WAREHOUSE_STOCK_LINKER, pos, state),
    ControlledWarehouseTile,
    OwnedLogisticsTarget,
    WarehouseInventoryListener,
    MenuProvider,
    ExtendedScreenHandlerFactory<BlockPos> {
    override var controllerPos: BlockPos? = null
    override var ownerName: String? = null
    override var ownerUuid: String? = null

    private val filters = mutableListOf<WarehouseLinkFilter>()
    private var currentEquality = false
    private var searchCooldown = 0
    private var updateCooldown = 0

    override fun getDisplayName(): Component = Component.translatable("tile.warehouse_stock_linker.name")

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return WarehouseStockLinkerScreenHandler(syncId, playerInventory, this)
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = blockPos

    fun tick() {
        val currentLevel = level ?: return
        if (currentLevel.isClientSide) {
            return
        }

        if (searchCooldown > 0) {
            searchCooldown--
        }
        if (controllerPos == null && searchCooldown <= 0) {
            searchCooldown = 40
            scanForController()
        }
        if (updateCooldown > 0) {
            updateCooldown--
        }
        if (updateCooldown <= 0) {
            val equality = getEqualityHandle()
            if (equality != currentEquality) {
                currentEquality = equality
                currentLevel.setBlock(blockPos, blockState.setValue(WarehouseStockLinkerBlock.ACTIVE, equality), Block.UPDATE_CLIENTS)
            }
            updateCooldown = 20
        }
    }

    fun getFilters(): List<WarehouseLinkFilter> = filters.toList()

    fun setFilters(newFilters: List<WarehouseLinkFilter>) {
        filters.clear()
        filters.addAll(newFilters)
        recountFilters()
        notifyClientUpdate()
    }

    private fun recountFilters() {
        val controller = getController(level)
        filters.forEach { filter ->
            filter.quantity = if (controller == null || filter.itemId.isNullOrBlank()) {
                0
            } else {
                controller.getCountOf(stackFromItemId(filter.itemId, 1))
            }
        }
    }

    fun getEqualityHandle(): Boolean {
        return filters.any { filter ->
            when (filter.equalitySignType) {
                WarehouseLinkFilter.EqualitySignType.EQUAL_TO -> filter.compareValue != 0 && filter.quantity == filter.compareValue
                WarehouseLinkFilter.EqualitySignType.GREATER_THAN -> filter.compareValue != 0 && filter.quantity > filter.compareValue
                WarehouseLinkFilter.EqualitySignType.LESS_THAN -> filter.compareValue != 0 && filter.quantity < filter.compareValue
                WarehouseLinkFilter.EqualitySignType.GREATER_THAN_OR_EQUAL_TO -> filter.compareValue != 0 && filter.quantity >= filter.compareValue
                WarehouseLinkFilter.EqualitySignType.LESS_THAN_OR_EQUAL_TO -> filter.compareValue != 0 && filter.quantity <= filter.compareValue
            }
        }
    }

    override fun onWarehouseInventoryUpdated(controller: WarehouseControllerBlockEntity) {
        recountFilters()
        notifyClientUpdate()
    }

    private fun scanForController() {
        val currentLevel = level ?: return
        val nearby = BlockPos.betweenClosed(blockPos.offset(-16, -4, -16), blockPos.offset(16, 4, 16))
        for (pos in nearby) {
            val blockEntity = currentLevel.getBlockEntity(pos)
            if (blockEntity is WarehouseControllerBlockEntity && isValidController(blockEntity)) {
                blockEntity.addControlledTile(this)
                break
            }
        }
    }

    override fun isValidController(controller: WarehouseControllerBlockEntity): Boolean {
        val min = controller.getWorkBoundsMin().offset(-1, 0, -1)
        val max = controller.getWorkBoundsMax().offset(1, 0, 1)
        return blockPos.x in min.x..max.x && blockPos.y in min.y..max.y && blockPos.z in min.z..max.z
    }

    private fun notifyClientUpdate() {
        setChanged()
        val currentLevel = level ?: return
        currentLevel.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        filters.clear()
        filters.addAll(decodeLinkFilters(input.getStringOr("filters", "")))
        val hasController = input.getBooleanOr("hasController", false)
        controllerPos = if (hasController) BlockPos(
            input.getIntOr("controllerX", blockPos.x),
            input.getIntOr("controllerY", blockPos.y),
            input.getIntOr("controllerZ", blockPos.z),
        ) else null
        ownerName = input.getStringOr("ownerName", "").ifBlank { null }
        ownerUuid = input.getStringOr("ownerUuid", "").ifBlank { null }
        currentEquality = input.getBooleanOr("currentEquality", false)
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putString("filters", encodeLinkFilters(filters))
        output.putBoolean("hasController", controllerPos != null)
        controllerPos?.let {
            output.putInt("controllerX", it.x)
            output.putInt("controllerY", it.y)
            output.putInt("controllerZ", it.z)
        }
        output.putString("ownerName", ownerName.orEmpty())
        output.putString("ownerUuid", ownerUuid.orEmpty())
        output.putBoolean("currentEquality", currentEquality)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)
}
