package com.columbina.content.logistics.warehouse

import com.columbina.content.logistics.OwnedLogisticsTarget
import com.columbina.content.logistics.screen.WarehouseStockViewerScreenHandler
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

class WarehouseStockViewerBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.WAREHOUSE_STOCK_VIEWER, pos, state),
    ControlledWarehouseTile,
    OwnedLogisticsTarget,
    WarehouseInventoryListener,
    MenuProvider,
    ExtendedScreenHandlerFactory<BlockPos> {
    override var controllerPos: BlockPos? = null
    override var ownerName: String? = null
    override var ownerUuid: String? = null

    private val filters = mutableListOf<WarehouseStockFilter>()

    override fun getDisplayName(): Component = Component.translatable("tile.warehouse_stock_viewer.name")

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return WarehouseStockViewerScreenHandler(syncId, playerInventory, this)
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = blockPos

    fun tick() {
        if (!level!!.isClientSide && controllerPos != null) {
            getController(level)?.addControlledTile(this)
        }
    }

    fun getFilters(): List<WarehouseStockFilter> = filters.toList()

    fun setFilters(newFilters: List<WarehouseStockFilter>) {
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

    override fun onWarehouseInventoryUpdated(controller: WarehouseControllerBlockEntity) {
        recountFilters()
        notifyClientUpdate()
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
        filters.addAll(decodeStockFilters(input.getStringOr("filters", "")))
        val hasController = input.getBooleanOr("hasController", false)
        controllerPos = if (hasController) BlockPos(
            input.getIntOr("controllerX", blockPos.x),
            input.getIntOr("controllerY", blockPos.y),
            input.getIntOr("controllerZ", blockPos.z),
        ) else null
        ownerName = input.getStringOr("ownerName", "").ifBlank { null }
        ownerUuid = input.getStringOr("ownerUuid", "").ifBlank { null }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putString("filters", encodeStockFilters(filters))
        output.putBoolean("hasController", controllerPos != null)
        controllerPos?.let {
            output.putInt("controllerX", it.x)
            output.putInt("controllerY", it.y)
            output.putInt("controllerZ", it.z)
        }
        output.putString("ownerName", ownerName.orEmpty())
        output.putString("ownerUuid", ownerUuid.orEmpty())
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)
}
