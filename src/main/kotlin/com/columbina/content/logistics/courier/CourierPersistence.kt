package com.columbina.content.logistics.courier

import com.columbina.content.logistics.order.RoutingOrder
import com.columbina.content.logistics.item.RoutingOrderItemAccess
import com.columbina.runtime.init.ColumbinaItems
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.SimpleContainer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

object CourierPersistence {
    fun readRouteState(input: ValueInput, controller: CourierTransferController) {
        controller.routeIndex = input.getIntOr("routeIndex", 0)
        controller.ticksAtSite = input.getIntOr("ticksAtSite", 0)
        controller.ticksToWork = input.getIntOr("ticksToWork", 0)
    }

    fun writeRouteState(output: ValueOutput, controller: CourierTransferController) {
        output.putInt("routeIndex", controller.routeIndex)
        output.putInt("ticksAtSite", controller.ticksAtSite)
        output.putInt("ticksToWork", controller.ticksToWork)
    }

    fun writeOrderStack(output: ValueOutput, ordersStack: ItemStack) {
        val order = RoutingOrderItemAccess.getRoutingOrder(ordersStack)
        if (order != null) {
            output.putString("ordersData", order.encode())
        }
    }

    fun readOrderStack(input: ValueInput): ItemStack {
        val encoded = input.getStringOr("ordersData", "")
        if (encoded.isBlank()) {
            return ItemStack.EMPTY
        }
        return ItemStack(ColumbinaItems.ROUTING_ORDER).also {
            RoutingOrderItemAccess.writeRoutingOrder(it, RoutingOrder.decode(encoded))
        }
    }

    fun writeBackpack(output: ValueOutput, backpack: SimpleContainer) {
        val encoded = buildList {
            for (slot in 0 until backpack.containerSize) {
                val stack = backpack.getItem(slot)
                if (!stack.isEmpty) {
                    add("$slot:${BuiltInRegistries.ITEM.getKey(stack.item)}@${stack.count}")
                }
            }
        }.joinToString(";")
        output.putString("backpack", encoded)
    }

    fun readBackpack(input: ValueInput, backpack: SimpleContainer) {
        val encoded = input.getStringOr("backpack", "")
        if (encoded.isBlank()) {
            return
        }
        encoded.split(';').forEach { entry ->
            if (entry.isBlank()) {
                return@forEach
            }
            val slotAndRest = entry.split(':', limit = 2)
            val slot = slotAndRest.getOrNull(0)?.toIntOrNull() ?: return@forEach
            val itemAndCount = slotAndRest.getOrNull(1)?.split('@', limit = 2) ?: return@forEach
            val item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemAndCount[0]))
            val count = itemAndCount.getOrNull(1)?.toIntOrNull() ?: 1
            if (slot in 0 until backpack.containerSize) {
                backpack.setItem(slot, ItemStack(item, count))
            }
        }
    }
}
