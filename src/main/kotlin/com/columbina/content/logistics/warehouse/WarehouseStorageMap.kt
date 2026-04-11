package com.columbina.content.logistics.warehouse

class WarehouseStorageMap {
    private val unfilteredStorage = linkedSetOf<WarehouseStorageBlockEntity>()
    private val filteredStorage = linkedSetOf<WarehouseStorageBlockEntity>()
    private val storageMap = linkedMapOf<WarehouseItemKey, LinkedHashSet<WarehouseStorageBlockEntity>>()

    fun addStorageTile(tile: WarehouseStorageBlockEntity) {
        addTileFilters(tile, tile.getFilters())
    }

    fun removeStorageTile(tile: WarehouseStorageBlockEntity) {
        removeTileFilters(tile, tile.getFilters())
    }

    fun updateTileFilters(
        tile: WarehouseStorageBlockEntity,
        oldFilters: List<WarehouseStorageFilter>,
        newFilters: List<WarehouseStorageFilter>,
    ) {
        removeTileFilters(tile, oldFilters)
        addTileFilters(tile, newFilters)
    }

    fun getDestinations(): List<WarehouseStorageBlockEntity> {
        return buildList {
            addAll(filteredStorage)
            addAll(unfilteredStorage)
        }
    }

    fun getDestinations(filterStack: net.minecraft.world.item.ItemStack): List<WarehouseStorageBlockEntity> {
        val key = WarehouseItemKey.fromStack(filterStack) ?: return getDestinations()
        val filtered = storageMap[key].orEmpty()
        return buildList {
            addAll(filtered)
            addAll(unfilteredStorage)
        }
    }

    private fun removeTileFilters(tile: WarehouseStorageBlockEntity, filters: List<WarehouseStorageFilter>) {
        if (filters.isEmpty()) {
            unfilteredStorage.remove(tile)
            return
        }

        filteredStorage.remove(tile)
        filters.forEach { filter ->
            val key = filter.itemId?.let(::WarehouseItemKey) ?: return@forEach
            storageMap[key]?.remove(tile)
        }
    }

    private fun addTileFilters(tile: WarehouseStorageBlockEntity, filters: List<WarehouseStorageFilter>) {
        if (filters.isEmpty()) {
            unfilteredStorage.add(tile)
            return
        }

        filteredStorage.add(tile)
        filters.forEach { filter ->
            val key = filter.itemId?.let(::WarehouseItemKey) ?: return@forEach
            storageMap.computeIfAbsent(key) { linkedSetOf() }.add(tile)
        }
    }
}
