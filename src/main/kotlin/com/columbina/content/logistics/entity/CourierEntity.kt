package com.columbina.content.logistics.entity

import com.columbina.content.logistics.courier.CourierPersistence
import com.columbina.content.logistics.courier.CourierTransferController
import com.columbina.content.logistics.item.RoutingOrderItem
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class CourierEntity(
    entityType: EntityType<out PathfinderMob>,
    level: Level,
) : PathfinderMob(entityType, level) {
    companion object {
        fun createAttributes(): AttributeSupplier.Builder {
            return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
        }
    }

    val backpackInventory = SimpleContainer(27)
    val transferController = CourierTransferController(this)
    var ordersStack: ItemStack = ItemStack.EMPTY
        private set

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
    }

    override fun aiStep() {
        super.aiStep()
        transferController.tick()
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val held = player.getItemInHand(hand)
        if (held.item is RoutingOrderItem) {
            ordersStack = held.copy()
            transferController.onOrdersChanged()
            if (!level().isClientSide) {
                player.displayClientMessage(Component.literal("Courier orders updated"), true)
            }
            return InteractionResult.SUCCESS
        }
        return super.mobInteract(player, hand)
    }

    override fun addAdditionalSaveData(valueOutput: ValueOutput) {
        super.addAdditionalSaveData(valueOutput)
        CourierPersistence.writeRouteState(valueOutput, transferController)
        CourierPersistence.writeOrderStack(valueOutput, ordersStack)
        CourierPersistence.writeBackpack(valueOutput, backpackInventory)
    }

    override fun readAdditionalSaveData(valueInput: ValueInput) {
        super.readAdditionalSaveData(valueInput)
        ordersStack = CourierPersistence.readOrderStack(valueInput)
        for (slot in 0 until backpackInventory.containerSize) {
            backpackInventory.setItem(slot, ItemStack.EMPTY)
        }
        CourierPersistence.readBackpack(valueInput, backpackInventory)
        CourierPersistence.readRouteState(valueInput, transferController)
        transferController.onOrdersChanged()
    }
}
