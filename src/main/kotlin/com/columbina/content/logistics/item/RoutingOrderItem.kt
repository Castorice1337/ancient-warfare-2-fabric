package com.columbina.content.logistics.item

import com.columbina.content.logistics.order.RoutingOrder
import com.columbina.content.logistics.screen.RoutingOrderScreenHandler
import com.columbina.runtime.ColumbinaIds
import com.columbina.runtime.init.ColumbinaItems
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import java.util.function.Consumer

interface RoutingOrderItemAccess {
    companion object {
        private const val ORDER_DATA = "routingOrderData"

        fun getRoutingOrder(stack: ItemStack): RoutingOrder? {
            if (!stack.`is`(ColumbinaItems.ROUTING_ORDER)) {
                return null
            }
            val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            return RoutingOrder.decode(data.getString(ORDER_DATA).orElse(""))
        }

        fun writeRoutingOrder(stack: ItemStack, order: RoutingOrder) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
                tag.putString(ORDER_DATA, order.encode())
            }
        }
    }
}

class RoutingOrderItem(properties: Properties) : Item(properties.stacksTo(1)) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!level.isClientSide && player is ServerPlayer) {
            player.openMenu(
                object : ExtendedScreenHandlerFactory<String> {
                    override fun getDisplayName(): Component = Component.translatable("item.ancientwarfare.routing_order")

                    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): RoutingOrderScreenHandler {
                        return RoutingOrderScreenHandler(syncId, playerInventory, hand.name)
                    }

                    override fun getScreenOpeningData(player: ServerPlayer): String = hand.name
                },
            )
        }
        return InteractionResult.SUCCESS
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val player = context.player ?: return InteractionResult.PASS
        val stack = context.itemInHand
        val order = RoutingOrderItemAccess.getRoutingOrder(stack) ?: RoutingOrder()
        order.addRoutePoint(context.clickedFace, context.clickedPos)
        RoutingOrderItemAccess.writeRoutingOrder(stack, order)

        if (!context.level.isClientSide) {
            player.displayClientMessage(Component.literal("Added route point ${context.clickedPos}"), true)
        }

        return InteractionResult.SUCCESS
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: TooltipDisplay,
        tooltipAdder: Consumer<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag)
        val order = RoutingOrderItemAccess.getRoutingOrder(stack) ?: RoutingOrder()
        tooltipAdder.accept(Component.literal("Route points: ${order.size()}"))
        tooltipAdder.accept(Component.literal("Right click to edit, use on block to add point"))
    }
}
