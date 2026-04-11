package com.columbina.content.logistics.courier

import com.columbina.content.logistics.entity.CourierEntity
import com.columbina.content.logistics.item.RoutingOrderItemAccess
import com.columbina.content.logistics.order.RoutingOrder
import net.minecraft.world.Container
import net.minecraft.world.WorldlyContainer
import kotlin.math.max

class CourierTransferController(private val courier: CourierEntity) {
    var routeIndex: Int = 0
    var ticksToWork: Int = 0
    var ticksAtSite: Int = 0

    private var initialized = false
    private var currentOrder: RoutingOrder? = null

    fun tick() {
        val level = courier.level()
        if (level.isClientSide) {
            return
        }

        if (!initialized) {
            initialized = true
            currentOrder = RoutingOrderItemAccess.getRoutingOrder(courier.ordersStack)
            if (currentOrder != null && routeIndex >= currentOrder!!.size()) {
                routeIndex = 0
            }
        }

        val order = currentOrder ?: return
        if (order.isEmpty()) {
            return
        }

        val point = order.get(routeIndex)
        val pos = point.target
        val dist = courier.distanceToSqr(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
        if (dist > 4.0) {
            courier.navigation.moveTo(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, 1.0)
            ticksAtSite = 0
            ticksToWork = 0
            return
        }

        courier.navigation.stop()
        workAtSite(order)
    }

    private fun workAtSite(order: RoutingOrder) {
        if (ticksToWork == 0) {
            startWork(order)
            return
        }

        ticksAtSite++
        if (ticksAtSite > ticksToWork) {
            setMoveToNextSite(order)
        }
    }

    fun startWork(order: RoutingOrder? = currentOrder) {
        val currentOrder = order ?: return
        val target = getTargetContainer(order) ?: run {
            setMoveToNextSite(currentOrder)
            return
        }

        ticksAtSite = 0
        val moved = currentOrder.handleRouteAction(currentOrder.get(routeIndex), courier.backpackInventory, target)
        if (moved > 0) {
            ticksToWork = max(10, moved * 10)
            return
        }

        setMoveToNextSite(currentOrder)
    }

    private fun getTargetContainer(order: RoutingOrder): Container? {
        val point = order.get(routeIndex)
        val blockEntity = courier.level().getBlockEntity(point.target)
        return when (blockEntity) {
            is WorldlyContainer -> DirectionalContainerView(blockEntity, point.blockSide)
            is Container -> blockEntity
            else -> null
        }
    }

    fun setMoveToNextSite(order: RoutingOrder? = currentOrder) {
        val currentOrder = order ?: return
        ticksAtSite = 0
        ticksToWork = 0
        routeIndex++
        if (routeIndex >= currentOrder.size()) {
            routeIndex = 0
        }
    }

    fun onOrdersChanged() {
        currentOrder = RoutingOrderItemAccess.getRoutingOrder(courier.ordersStack)
        routeIndex = 0
        ticksAtSite = 0
        ticksToWork = 0
        initialized = true
    }
}
