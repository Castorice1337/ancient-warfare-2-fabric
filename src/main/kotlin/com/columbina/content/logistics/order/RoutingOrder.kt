package com.columbina.content.logistics.order

import com.columbina.content.logistics.warehouse.insertStack
import com.columbina.content.logistics.warehouse.stackFromItemId
import com.columbina.content.logistics.warehouse.stackItemId
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import kotlin.math.ceil

class RoutingOrder(
    private val points: MutableList<RoutePoint> = mutableListOf(),
) {
    data class RouteFilter(
        var itemId: String = "",
        var count: Int = 1,
    ) {
        fun isEmpty(): Boolean = itemId.isBlank()
    }

    data class RoutePoint(
        var target: BlockPos = BlockPos.ZERO,
        var blockSide: Direction = Direction.DOWN,
        var routeType: RouteType = RouteType.FILL_TARGET_TO,
        var ignoreDamage: Boolean = false,
        var ignoreTag: Boolean = false,
        val filters: MutableList<RouteFilter> = MutableList(12) { RouteFilter() },
    )

    enum class RouteType(val translationKey: String) {
        FILL_TARGET_TO("route.fill.upto"),
        FILL_COURIER_TO("route.take.upto"),
        DEPOSIT_ALL_OF("route.deposit.match"),
        WITHDRAW_ALL_OF("route.withdraw.match"),
        DEPOSIT_ALL_EXCEPT("route.deposit.no_match"),
        WITHDRAW_ALL_EXCEPT("route.withdraw.no_match"),
        DEPOSIT_RATIO("route.deposit.ratio"),
        WITHDRAW_RATIO("route.withdraw.ratio"),
        DEPOSIT_EXACT("route.deposit.exact"),
        WITHDRAW_EXACT("route.withdraw.exact"),
        FILL_MINIMUM("route.fill.minimum"),
        TAKE_MINIMUM("route.take.minimum"),
        ;

        fun next(): RouteType = entries[(ordinal + 1) % entries.size]

        fun previous(): RouteType = entries[(ordinal + entries.size - 1) % entries.size]
    }

    fun size(): Int = points.size

    fun isEmpty(): Boolean = points.isEmpty()

    fun get(index: Int): RoutePoint = points[index]

    fun getEntries(): List<RoutePoint> = points

    fun addRoutePoint(side: Direction, pos: BlockPos) {
        points += RoutePoint(target = pos, blockSide = side)
    }

    fun remove(index: Int) {
        if (index in points.indices) {
            points.removeAt(index)
        }
    }

    fun increment(index: Int) {
        if (index !in points.indices || index <= 0) {
            return
        }
        val point = points.removeAt(index)
        points.add(index - 1, point)
    }

    fun decrement(index: Int) {
        if (index !in points.indices || index >= points.lastIndex) {
            return
        }
        val point = points.removeAt(index)
        points.add(index + 1, point)
    }

    fun changeRouteType(index: Int, reverse: Boolean = false) {
        if (index !in points.indices) {
            return
        }
        points[index].routeType = if (reverse) points[index].routeType.previous() else points[index].routeType.next()
    }

    fun changeBlockSide(index: Int) {
        if (index !in points.indices) {
            return
        }
        val point = points[index]
        point.blockSide = Direction.entries[(point.blockSide.ordinal + 1) % Direction.entries.size]
    }

    fun toggleIgnoreDamage(index: Int) {
        if (index !in points.indices) {
            return
        }
        points[index].ignoreDamage = !points[index].ignoreDamage
    }

    fun toggleIgnoreTag(index: Int) {
        if (index !in points.indices) {
            return
        }
        points[index].ignoreTag = !points[index].ignoreTag
    }

    fun setFilter(index: Int, filterIndex: Int, itemId: String?, count: Int = 1) {
        val point = points.getOrNull(index) ?: return
        if (filterIndex !in point.filters.indices) {
            return
        }
        point.filters[filterIndex] = RouteFilter(itemId.orEmpty(), count.coerceAtLeast(1))
    }

    fun handleRouteAction(point: RoutePoint, courierInventory: Container, targetInventory: Container): Int {
        return when (point.routeType) {
            RouteType.FILL_COURIER_TO -> fillTo(targetInventory, courierInventory, point)
            RouteType.FILL_TARGET_TO -> fillTo(courierInventory, targetInventory, point)
            RouteType.DEPOSIT_ALL_EXCEPT -> transferAllExcept(courierInventory, targetInventory, point)
            RouteType.DEPOSIT_ALL_OF -> transferAllMatching(courierInventory, targetInventory, point)
            RouteType.WITHDRAW_ALL_EXCEPT -> transferAllExcept(targetInventory, courierInventory, point)
            RouteType.WITHDRAW_ALL_OF -> transferAllMatching(targetInventory, courierInventory, point)
            RouteType.DEPOSIT_RATIO -> transferRatio(courierInventory, targetInventory, point)
            RouteType.WITHDRAW_RATIO -> transferRatio(targetInventory, courierInventory, point)
            RouteType.DEPOSIT_EXACT -> transferExact(courierInventory, targetInventory, point)
            RouteType.WITHDRAW_EXACT -> transferExact(targetInventory, courierInventory, point)
            RouteType.FILL_MINIMUM -> fillMinimum(courierInventory, targetInventory, point)
            RouteType.TAKE_MINIMUM -> fillMinimum(targetInventory, courierInventory, point)
        }
    }

    fun encode(): String {
        return points.joinToString(";") { point ->
            val filters = point.filters.joinToString(",") { filter -> "${filter.itemId}@${filter.count}" }
            listOf(
                point.target.x.toString(),
                point.target.y.toString(),
                point.target.z.toString(),
                point.blockSide.name,
                point.routeType.name,
                point.ignoreDamage.toString(),
                point.ignoreTag.toString(),
                filters,
            ).joinToString("|")
        }
    }

    companion object {
        fun decode(encoded: String): RoutingOrder {
            if (encoded.isBlank()) {
                return RoutingOrder()
            }

            val points = encoded.split(';').mapNotNull { pointString ->
                if (pointString.isBlank()) {
                    return@mapNotNull null
                }
                val parts = pointString.split('|', limit = 8)
                val x = parts.getOrNull(0)?.toIntOrNull() ?: return@mapNotNull null
                val y = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                val z = parts.getOrNull(2)?.toIntOrNull() ?: return@mapNotNull null
                val side = parts.getOrNull(3)?.let(Direction::valueOf) ?: Direction.DOWN
                val routeType = parts.getOrNull(4)?.let(RouteType::valueOf) ?: RouteType.FILL_TARGET_TO
                val ignoreDamage = parts.getOrNull(5)?.toBooleanStrictOrNull() ?: false
                val ignoreTag = parts.getOrNull(6)?.toBooleanStrictOrNull() ?: false
                val filters = MutableList(12) { RouteFilter() }
                parts.getOrNull(7)?.split(',')?.take(12)?.forEachIndexed { index, filterString ->
                    val filterParts = filterString.split('@', limit = 2)
                    filters[index] = RouteFilter(
                        itemId = filterParts.getOrNull(0).orEmpty(),
                        count = filterParts.getOrNull(1)?.toIntOrNull() ?: 1,
                    )
                }
                RoutePoint(BlockPos(x, y, z), side, routeType, ignoreDamage, ignoreTag, filters)
            }
            return RoutingOrder(points.toMutableList())
        }
    }

    private fun fillTo(from: Container, to: Container, point: RoutePoint): Int {
        var movedItems = 0
        activeFilters(point).forEach { filter ->
            val existing = countMatching(to, filter, point)
            val toMove = (filter.count - existing).coerceAtLeast(0)
            if (toMove > 0) {
                movedItems += transferMatching(from, to, filter, point, toMove)
            }
        }
        return movedStacks(movedItems)
    }

    private fun transferAllMatching(from: Container, to: Container, point: RoutePoint): Int {
        val filters = activeFilters(point)
        var movedItems = 0
        for (slot in 0 until from.containerSize) {
            val stack = from.getItem(slot)
            if (stack.isEmpty || (filters.isNotEmpty() && filters.none { matches(stack, it, point) })) {
                continue
            }

            val moved = transferSlotStack(from, to, slot, stack.count)
            movedItems += moved
        }
        return movedStacks(movedItems)
    }

    private fun transferAllExcept(from: Container, to: Container, point: RoutePoint): Int {
        val filters = activeFilters(point)
        var movedItems = 0
        for (slot in 0 until from.containerSize) {
            val stack = from.getItem(slot)
            if (stack.isEmpty) {
                continue
            }
            if (filters.any { matches(stack, it, point) }) {
                continue
            }

            movedItems += transferSlotStack(from, to, slot, stack.count)
        }
        return movedStacks(movedItems)
    }

    private fun transferRatio(from: Container, to: Container, point: RoutePoint): Int {
        var movedItems = 0
        activeFilters(point).forEach { filter ->
            val available = countMatching(from, filter, point)
            if (available <= 0 || filter.count <= 0) {
                return@forEach
            }
            val toMove = (available * (1f / filter.count)).toInt().coerceAtLeast(0)
            movedItems += transferMatching(from, to, filter, point, toMove)
        }
        return movedStacks(movedItems)
    }

    private fun transferExact(from: Container, to: Container, point: RoutePoint): Int {
        var movedItems = 0
        activeFilters(point).forEach { filter ->
            val available = countMatching(from, filter, point)
            if (available < filter.count) {
                return@forEach
            }
            movedItems += transferMatching(from, to, filter, point, filter.count)
        }
        return movedStacks(movedItems)
    }

    private fun fillMinimum(from: Container, to: Container, point: RoutePoint): Int {
        var movedItems = 0
        activeFilters(point).forEach { filter ->
            val available = countMatching(from, filter, point)
            val existing = countMatching(to, filter, point)
            val needed = (filter.count - existing).coerceAtLeast(0)
            if (needed > 0 && available >= needed) {
                movedItems += transferMatching(from, to, filter, point, needed)
            }
        }
        return movedStacks(movedItems)
    }

    private fun activeFilters(point: RoutePoint): List<RouteFilter> = point.filters.filterNot(RouteFilter::isEmpty)

    private fun countMatching(container: Container, filter: RouteFilter, point: RoutePoint): Int {
        var total = 0
        for (slot in 0 until container.containerSize) {
            val stack = container.getItem(slot)
            if (matches(stack, filter, point)) {
                total += stack.count
            }
        }
        return total
    }

    private fun transferMatching(
        from: Container,
        to: Container,
        filter: RouteFilter,
        point: RoutePoint,
        amount: Int,
    ): Int {
        var remaining = amount
        var moved = 0

        for (slot in 0 until from.containerSize) {
            val stack = from.getItem(slot)
            if (!matches(stack, filter, point)) {
                continue
            }

            moved += transferSlotStack(from, to, slot, remaining)
            remaining = amount - moved
            if (remaining <= 0) {
                break
            }
        }

        return moved
    }

    private fun transferSlotStack(from: Container, to: Container, slot: Int, limit: Int): Int {
        val stack = from.getItem(slot)
        if (stack.isEmpty || limit <= 0) {
            return 0
        }

        val moving = stack.copy()
        moving.count = minOf(limit, stack.count)
        val inserted = insertStack(to, moving)
        if (inserted <= 0) {
            return 0
        }

        from.removeItem(slot, inserted)
        from.setChanged()
        to.setChanged()
        return inserted
    }

    private fun matches(stack: ItemStack, filter: RouteFilter, point: RoutePoint): Boolean {
        if (stack.isEmpty || filter.isEmpty()) {
            return false
        }

        return when {
            point.ignoreTag || point.ignoreDamage -> stackItemId(stack) == filter.itemId
            else -> net.minecraft.world.item.ItemStack.isSameItemSameComponents(stack, stackFromItemId(filter.itemId, 1))
        }
    }

    private fun movedStacks(itemCount: Int): Int {
        if (itemCount <= 0) {
            return 0
        }
        return ceil(itemCount / 64.0).toInt()
    }
}
