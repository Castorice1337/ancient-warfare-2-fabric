package com.columbina.content.logistics.screen

import com.columbina.content.logistics.item.RoutingOrderItemAccess
import com.columbina.content.logistics.order.RoutingOrder
import com.columbina.runtime.init.ColumbinaItems
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class RoutingOrderScreenHandler(
    syncId: Int,
    private val playerInventory: Inventory,
    val handName: String,
) : AbstractContainerMenu(ColumbinaScreenHandlers.ROUTING_ORDER, syncId) {
    companion object {
        private const val POINT_STRIDE = 16
        private const val ACTION_BLOCK_SIDE = 1
        private const val ACTION_ROUTE_TYPE = 2
        private const val ACTION_IGNORE_DAMAGE = 3
        private const val ACTION_IGNORE_TAG = 4
        private const val ACTION_REMOVE = 5
        private const val ACTION_MOVE_UP = 6
        private const val ACTION_MOVE_DOWN = 7
        private const val ACTION_FILTER_0 = 8
        private const val ACTION_FILTER_1 = 9
        private const val ACTION_FILTER_2 = 10
    }

    val hand: InteractionHand = InteractionHand.valueOf(handName)

    fun routingOrder(): RoutingOrder = RoutingOrderItemAccess.getRoutingOrder(orderStack()) ?: RoutingOrder()

    private fun orderStack() = playerInventory.player.getItemInHand(hand)

    private fun mutateOrder(transform: (RoutingOrder) -> Unit): Boolean {
        val stack = orderStack()
        if (stack.isEmpty) {
            return false
        }
        val order = routingOrder()
        transform(order)
        RoutingOrderItemAccess.writeRoutingOrder(stack, order)
        playerInventory.player.inventory.setChanged()
        return true
    }

    fun changeRouteType(index: Int): Boolean = mutateOrder { it.changeRouteType(index, false) }

    fun changeBlockSide(index: Int): Boolean = mutateOrder { it.changeBlockSide(index) }

    fun toggleIgnoreDamage(index: Int): Boolean = mutateOrder { it.toggleIgnoreDamage(index) }

    fun toggleIgnoreTag(index: Int): Boolean = mutateOrder { it.toggleIgnoreTag(index) }

    fun removePoint(index: Int): Boolean = mutateOrder { it.remove(index) }

    fun movePointUp(index: Int): Boolean = mutateOrder { it.increment(index) }

    fun movePointDown(index: Int): Boolean = mutateOrder { it.decrement(index) }

    fun setFilterFromSelectedItem(index: Int, filterIndex: Int): Boolean = mutateOrder { order ->
        val player = playerInventory.player
        val selected = sequenceOf(player.mainHandItem, player.offhandItem)
            .firstOrNull { !it.isEmpty && !it.`is`(ColumbinaItems.ROUTING_ORDER) }
            ?: ItemStack.EMPTY
        if (selected.isEmpty) {
            order.setFilter(index, filterIndex, null)
        } else {
            order.setFilter(index, filterIndex, com.columbina.content.logistics.warehouse.stackItemId(selected), selected.count)
        }
    }

    override fun clickMenuButton(player: Player, id: Int): Boolean {
        val pointIndex = id / POINT_STRIDE
        val action = id % POINT_STRIDE

        return when (action) {
            ACTION_BLOCK_SIDE -> changeBlockSide(pointIndex)
            ACTION_ROUTE_TYPE -> changeRouteType(pointIndex)
            ACTION_IGNORE_DAMAGE -> toggleIgnoreDamage(pointIndex)
            ACTION_IGNORE_TAG -> toggleIgnoreTag(pointIndex)
            ACTION_REMOVE -> removePoint(pointIndex)
            ACTION_MOVE_UP -> movePointUp(pointIndex)
            ACTION_MOVE_DOWN -> movePointDown(pointIndex)
            ACTION_FILTER_0 -> setFilterFromSelectedItem(pointIndex, 0)
            ACTION_FILTER_1 -> setFilterFromSelectedItem(pointIndex, 1)
            ACTION_FILTER_2 -> setFilterFromSelectedItem(pointIndex, 2)
            else -> super.clickMenuButton(player, id)
        }
    }

    override fun quickMoveStack(player: Player, i: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean = true
}
