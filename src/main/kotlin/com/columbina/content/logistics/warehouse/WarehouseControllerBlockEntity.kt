package com.columbina.content.logistics.warehouse

import com.columbina.content.logistics.screen.WarehouseControlScreenHandler
import com.columbina.runtime.init.ColumbinaBlockEntities
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerData
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class WarehouseControllerBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.WAREHOUSE_CONTROL, pos, state), MenuProvider, ExtendedScreenHandlerFactory<BlockPos> {
    companion object {
        private const val DISPLAY_SLOTS = 54
        private const val RESCAN_INTERVAL = 40
    }

    private val storageTiles = linkedSetOf<WarehouseStorageBlockEntity>()
    private val interfaceTiles = linkedSetOf<WarehouseInterfaceBlockEntity>()
    val interfacesToFill = linkedSetOf<WarehouseInterfaceBlockEntity>()
    val interfacesToEmpty = linkedSetOf<WarehouseInterfaceBlockEntity>()
    private val storageMap = WarehouseStorageMap()

    val displayInventory = SimpleContainer(DISPLAY_SLOTS)
    private val cachedItemMap = linkedMapOf<WarehouseItemKey, Int>()
    private var displaySnapshot = ""
    private var ticksSinceScan = 0
    private var initialized = false

    var sortType: WarehouseSortType = WarehouseSortType.NAME
    var sortOrder: WarehouseSortOrder = WarehouseSortOrder.DESCENDING

    private var currentStored: Int = 0
    private var maxStorage: Int = 0

    val menuData: ContainerData = object : ContainerData {
        override fun get(index: Int): Int {
            return when (index) {
                0 -> currentStored
                1 -> maxStorage
                2 -> sortType.ordinal
                3 -> sortOrder.ordinal
                else -> 0
            }
        }

        override fun set(index: Int, value: Int) {
            when (index) {
                0 -> currentStored = value
                1 -> maxStorage = value
                2 -> sortType = WarehouseSortType.entries.getOrElse(value) { WarehouseSortType.NAME }
                3 -> sortOrder = WarehouseSortOrder.entries.getOrElse(value) { WarehouseSortOrder.DESCENDING }
            }
        }

        override fun getCount(): Int = 4
    }

    override fun getDisplayName(): Component = Component.translatable("guistrings.automation.warehouse_control")

    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
        return WarehouseControlScreenHandler(syncId, playerInventory, this)
    }

    override fun getScreenOpeningData(player: ServerPlayer): BlockPos = blockPos

    fun getWorkBoundsMin(): BlockPos = blockPos.offset(-8, 0, -8)

    fun getWorkBoundsMax(): BlockPos = blockPos.offset(8, 2, 8)

    fun tick() {
        val currentLevel = level ?: return
        if (currentLevel.isClientSide) {
            return
        }

        if (!initialized) {
            initialized = true
            scanForInitialTiles()
            refreshCachedState()
        }

        ticksSinceScan++
        if (ticksSinceScan >= RESCAN_INTERVAL) {
            ticksSinceScan = 0
            scanForInitialTiles()
        }

        if (!tryEmptyInterfaces()) {
            tryFillInterfaces()
        }
    }

    fun scanForInitialTiles() {
        val currentLevel = level ?: return
        val min = getWorkBoundsMin()
        val max = getWorkBoundsMax()

        for (x in (min.x - 1)..(max.x + 1)) {
            for (y in min.y..max.y) {
                for (z in (min.z - 1)..(max.z + 1)) {
                    val tile = currentLevel.getBlockEntity(BlockPos(x, y, z))
                    when (tile) {
                        is WarehouseStorageBlockEntity -> {
                            if (tile.getController(currentLevel) == null && tile.isValidController(this)) {
                                addControlledTile(tile)
                            }
                        }
                        is WarehouseInterfaceBlockEntity -> {
                            if (tile.getController(currentLevel) == null && tile.isValidController(this)) {
                                addControlledTile(tile)
                            }
                        }
                    }
                }
            }
        }
    }

    fun addControlledTile(tile: ControlledWarehouseTile) {
        when (tile) {
            is WarehouseStorageBlockEntity -> addStorageTile(tile)
            is WarehouseInterfaceBlockEntity -> addInterfaceTile(tile)
        }
    }

    fun removeControlledTile(tile: ControlledWarehouseTile) {
        when (tile) {
            is WarehouseStorageBlockEntity -> removeStorageTile(tile)
            is WarehouseInterfaceBlockEntity -> removeInterfaceTile(tile)
        }
    }

    private fun addStorageTile(tile: WarehouseStorageBlockEntity) {
        if (storageTiles.add(tile)) {
            tile.setController(this)
            storageMap.addStorageTile(tile)
            refreshCachedState()
        }
    }

    private fun removeStorageTile(tile: WarehouseStorageBlockEntity) {
        if (storageTiles.remove(tile)) {
            storageMap.removeStorageTile(tile)
            tile.setController(null)
            refreshCachedState()
        }
    }

    fun addInterfaceTile(tile: WarehouseInterfaceBlockEntity) {
        if (interfaceTiles.add(tile)) {
            tile.setController(this)
            onInterfaceInventoryChanged(tile)
        }
    }

    fun removeInterfaceTile(tile: WarehouseInterfaceBlockEntity) {
        if (interfaceTiles.remove(tile)) {
            interfacesToFill.remove(tile)
            interfacesToEmpty.remove(tile)
            tile.setController(null)
        }
    }

    fun onInterfaceInventoryChanged(tile: WarehouseInterfaceBlockEntity) {
        interfacesToFill.remove(tile)
        interfacesToEmpty.remove(tile)
        if (tile.getFillRequests().isNotEmpty()) {
            interfacesToFill.add(tile)
        }
        if (tile.getEmptyRequests().isNotEmpty()) {
            interfacesToEmpty.add(tile)
        }
        setChanged()
    }

    fun onStorageFilterChanged(
        tile: WarehouseStorageBlockEntity,
        oldFilters: List<WarehouseStorageFilter>,
        newFilters: List<WarehouseStorageFilter>,
    ) {
        storageMap.updateTileFilters(tile, oldFilters, newFilters)
        refreshCachedState()
    }

    fun getMaxStorage(): Int = storageTiles.sumOf { it.getStorageAdditionSize() }

    fun refreshCachedState() {
        cachedItemMap.clear()
        storageTiles.forEach { it.addItems(cachedItemMap) }
        currentStored = cachedItemMap.values.sum()
        maxStorage = getMaxStorage()
        refreshDisplayInventory()
        notifyClientUpdate()
    }

    private fun refreshDisplayInventory() {
        val sortedStacks = cachedItemMap.entries.mapNotNull { (key, count) ->
            stackFromItemId(key.itemId, count).takeUnless(ItemStack::isEmpty)
        }.sortedWith(compareStacks())

        copyInto(displayInventory, sortedStacks.take(DISPLAY_SLOTS))
        displaySnapshot = sortedStacks.take(DISPLAY_SLOTS).joinToString(";") { "${stackItemId(it).orEmpty()}@${it.count}" }
    }

    private fun compareStacks(): Comparator<ItemStack> {
        val base = Comparator<ItemStack> { left, right ->
            when (sortType) {
                WarehouseSortType.NAME -> left.hoverName.string.compareTo(right.hoverName.string, ignoreCase = true)
                WarehouseSortType.QUANTITY -> left.count.compareTo(right.count)
            }
        }

        return if (sortOrder == WarehouseSortOrder.ASCENDING) base else base.reversed()
    }

    fun cycleSortType() {
        sortType = sortType.next()
        refreshCachedState()
    }

    fun cycleSortOrder() {
        sortOrder = sortOrder.next()
        refreshCachedState()
    }

    fun tryAdd(stack: ItemStack): ItemStack {
        if (stack.isEmpty) {
            return ItemStack.EMPTY
        }

        var remaining = stack.count
        for (tile in storageMap.getDestinations(stack)) {
            val inserted = tile.insertItem(stack, remaining)
            remaining -= inserted
            if (remaining <= 0) {
                break
            }
        }

        val result = stack.copy()
        result.count = remaining
        refreshCachedState()
        return if (remaining <= 0) ItemStack.EMPTY else result
    }

    fun tryGet(filter: ItemStack, amount: Int): ItemStack {
        if (filter.isEmpty || amount <= 0) {
            return ItemStack.EMPTY
        }

        var remaining = amount
        val removed = filter.copy()
        removed.count = 0

        for (tile in storageMap.getDestinations(filter)) {
            val extracted = tile.extractItem(filter, remaining)
            if (extracted > 0) {
                removed.grow(extracted)
                remaining -= extracted
            }
            if (remaining <= 0) {
                break
            }
        }

        if (!removed.isEmpty) {
            refreshCachedState()
        }

        return removed
    }

    fun handleSlotClick(player: Player, filter: ItemStack, shiftClick: Boolean, rightClick: Boolean) {
        if (filter.isEmpty) {
            return
        }

        val available = cachedItemMap[WarehouseItemKey.fromStack(filter)] ?: 0
        if (available <= 0) {
            return
        }

        var requested = filter.maxStackSize
        if (rightClick && requested > 1) {
            requested = if (shiftClick) {
                requested
            } else {
                kotlin.math.ceil(requested / 2.0).toInt()
            }
        }

        val removed = tryGet(filter, minOf(requested, available))
        if (!removed.isEmpty) {
            player.inventory.add(removed)
            player.containerMenu.broadcastChanges()
        }
    }

    private fun tryEmptyInterfaces(): Boolean {
        val tile = interfacesToEmpty.firstOrNull() ?: return false
        for (request in tile.getEmptyRequests()) {
            val stack = tile.inventory.getItem(request.slotNum)
            if (stack.isEmpty) {
                continue
            }
            val destinations = storageMap.getDestinations(stack)
            var toMove = request.count
            for (destination in destinations) {
                val moved = destination.insertItem(stack, minOf(toMove, stack.count))
                if (moved > 0) {
                    tile.inventory.removeItem(request.slotNum, moved)
                    toMove -= moved
                }
                if (toMove <= 0) {
                    break
                }
            }
            if (toMove != request.count) {
                tile.recalcRequests()
                refreshCachedState()
                return true
            }
        }
        return false
    }

    private fun tryFillInterfaces(): Boolean {
        val tile = interfacesToFill.firstOrNull() ?: return false
        for (request in tile.getFillRequests()) {
            val filter = request.requestedItem
            val removed = tryGet(filter, request.requestAmount)
            if (removed.isEmpty) {
                continue
            }

            val inserted = insertStack(tile.inventory, removed)
            if (inserted < removed.count) {
                val remainder = removed.copy()
                remainder.count = removed.count - inserted
                tryAdd(remainder)
            }
            tile.recalcRequests()
            refreshCachedState()
            return true
        }
        return false
    }

    private fun notifyClientUpdate() {
        setChanged()
        val currentLevel = level ?: return
        currentLevel.sendBlockUpdated(blockPos, blockState, blockState, Block.UPDATE_CLIENTS)
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        sortType = WarehouseSortType.entries.getOrElse(input.getIntOr("sortType", WarehouseSortType.NAME.ordinal)) { WarehouseSortType.NAME }
        sortOrder = WarehouseSortOrder.entries.getOrElse(input.getIntOr("sortOrder", WarehouseSortOrder.DESCENDING.ordinal)) { WarehouseSortOrder.DESCENDING }
        displaySnapshot = input.getStringOr("displaySnapshot", "")
        restoreDisplayInventory()
        currentStored = input.getIntOr("currentStored", 0)
        maxStorage = input.getIntOr("maxStorage", 0)
        initialized = false
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putInt("sortType", sortType.ordinal)
        output.putInt("sortOrder", sortOrder.ordinal)
        output.putString("displaySnapshot", displaySnapshot)
        output.putInt("currentStored", currentStored)
        output.putInt("maxStorage", maxStorage)
    }

    private fun restoreDisplayInventory() {
        val stacks = displaySnapshot.split(';').mapNotNull { entry ->
            if (entry.isBlank()) {
                return@mapNotNull null
            }
            val parts = entry.split('@', limit = 2)
            stackFromItemId(parts.firstOrNull(), parts.getOrNull(1)?.toIntOrNull() ?: 1)
        }
        copyInto(displayInventory, stacks)
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)
}
