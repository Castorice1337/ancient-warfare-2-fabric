package com.columbina.content.logistics.warehouse

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack

enum class WarehouseSortType {
    NAME,
    QUANTITY,
    ;

    fun next(): WarehouseSortType = entries[(ordinal + 1) % entries.size]
}

enum class WarehouseSortOrder {
    ASCENDING,
    DESCENDING,
    ;

    fun next(): WarehouseSortOrder = if (this == ASCENDING) DESCENDING else ASCENDING
}

data class WarehouseItemKey(val itemId: String) {
    companion object {
        fun fromStack(stack: ItemStack): WarehouseItemKey? {
            if (stack.isEmpty) {
                return null
            }

            return WarehouseItemKey(BuiltInRegistries.ITEM.getKey(stack.item).toString())
        }
    }
}

class WarehouseStorageFilter(var itemId: String? = null) {
    constructor(stack: ItemStack) : this(stackItemId(stack))

    fun matches(stack: ItemStack): Boolean {
        if (itemId == null || stack.isEmpty) {
            return false
        }
        return stackItemId(stack) == itemId
    }

    fun allows(stack: ItemStack): Boolean = matches(stack)

    fun encode(): String = itemId.orEmpty()

    companion object {
        fun decode(encoded: String): WarehouseStorageFilter = WarehouseStorageFilter(encoded.ifBlank { null })
    }
}

class WarehouseInterfaceFilter(
    var itemId: String? = null,
    var filterQuantity: Int = 0,
) {
    constructor(stack: ItemStack, quantity: Int) : this(stackItemId(stack), quantity)

    fun apply(stack: ItemStack): Boolean {
        if (itemId == null || stack.isEmpty) {
            return false
        }
        return stackItemId(stack) == itemId
    }

    fun createFilterStack(): ItemStack = stackFromItemId(itemId, filterQuantity)

    fun encode(): String = "${itemId.orEmpty()}|$filterQuantity"

    companion object {
        fun decode(encoded: String): WarehouseInterfaceFilter {
            val parts = encoded.split('|', limit = 2)
            return WarehouseInterfaceFilter(
                itemId = parts.getOrNull(0)?.ifBlank { null },
                filterQuantity = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            )
        }
    }
}

fun stackItemId(stack: ItemStack): String? = WarehouseItemKey.fromStack(stack)?.itemId

fun stackFromItemId(itemId: String?, count: Int = 1): ItemStack {
    if (itemId.isNullOrBlank()) {
        return ItemStack.EMPTY
    }

    val item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId))
    return ItemStack(item, count.coerceAtLeast(1))
}

fun countOf(container: Container, itemId: String): Int {
    var total = 0
    for (slot in 0 until container.containerSize) {
        val stack = container.getItem(slot)
        if (stackItemId(stack) == itemId) {
            total += stack.count
        }
    }
    return total
}

fun findFirstNonEmpty(container: Container): ItemStack? {
    for (slot in 0 until container.containerSize) {
        val stack = container.getItem(slot)
        if (!stack.isEmpty) {
            return stack
        }
    }
    return null
}

fun insertStack(container: Container, incoming: ItemStack): Int {
    if (incoming.isEmpty) {
        return 0
    }

    var remaining = incoming.count
    val working = incoming.copy()

    for (slot in 0 until container.containerSize) {
        val existing = container.getItem(slot)
        if (existing.isEmpty || !ItemStack.isSameItemSameComponents(existing, working)) {
            continue
        }

        val limit = minOf(container.getMaxStackSize(existing), existing.maxStackSize)
        val space = (limit - existing.count).coerceAtLeast(0)
        if (space <= 0) {
            continue
        }

        val inserted = minOf(space, remaining)
        existing.grow(inserted)
        container.setItem(slot, existing)
        remaining -= inserted
        if (remaining <= 0) {
            container.setChanged()
            return incoming.count
        }
    }

    for (slot in 0 until container.containerSize) {
        val existing = container.getItem(slot)
        if (!existing.isEmpty) {
            continue
        }

        val inserted = minOf(working.maxStackSize, remaining)
        val placed = working.copy()
        placed.count = inserted
        container.setItem(slot, placed)
        remaining -= inserted
        if (remaining <= 0) {
            container.setChanged()
            return incoming.count
        }
    }

    if (remaining != incoming.count) {
        container.setChanged()
    }

    return incoming.count - remaining
}

fun removeMatching(container: Container, filter: ItemStack, amount: Int): ItemStack {
    if (filter.isEmpty || amount <= 0) {
        return ItemStack.EMPTY
    }

    val removed = filter.copy()
    removed.count = 0
    var remaining = amount

    for (slot in 0 until container.containerSize) {
        val existing = container.getItem(slot)
        if (existing.isEmpty || !ItemStack.isSameItemSameComponents(existing, filter)) {
            continue
        }

        val taken = minOf(existing.count, remaining)
        if (taken <= 0) {
            continue
        }

        val extracted = container.removeItem(slot, taken)
        removed.grow(extracted.count)
        remaining -= extracted.count
        if (remaining <= 0) {
            container.setChanged()
            return removed
        }
    }

    if (!removed.isEmpty) {
        container.setChanged()
    }

    return removed
}

fun encodeStorageFilters(filters: List<WarehouseStorageFilter>): String = filters.joinToString(";") { it.encode() }

fun decodeStorageFilters(encoded: String): List<WarehouseStorageFilter> {
    if (encoded.isBlank()) {
        return emptyList()
    }
    return encoded.split(';').filter { it.isNotBlank() }.map(WarehouseStorageFilter::decode)
}

fun encodeInterfaceFilters(filters: List<WarehouseInterfaceFilter>): String = filters.joinToString(";") { it.encode() }

fun decodeInterfaceFilters(encoded: String): List<WarehouseInterfaceFilter> {
    if (encoded.isBlank()) {
        return emptyList()
    }
    return encoded.split(';').filter { it.isNotBlank() }.map(WarehouseInterfaceFilter::decode)
}

fun copyInto(target: SimpleContainer, stacks: List<ItemStack>) {
    for (slot in 0 until target.containerSize) {
        target.setItem(slot, stacks.getOrNull(slot)?.copy() ?: ItemStack.EMPTY)
    }
    target.setChanged()
}
