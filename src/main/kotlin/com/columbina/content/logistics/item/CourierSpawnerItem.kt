package com.columbina.content.logistics.item

import com.columbina.content.logistics.entity.CourierEntity
import com.columbina.runtime.init.ColumbinaEntities
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext

class CourierSpawnerItem(properties: Properties) : Item(properties.stacksTo(1)) {
    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        if (level !is ServerLevel) {
            return InteractionResult.SUCCESS
        }

        val player = context.player ?: return InteractionResult.PASS
        val pos = context.clickedPos.relative(context.clickedFace)
        val courier = ColumbinaEntities.COURIER.create(level, EntitySpawnReason.SPAWN_ITEM_USE)
        if (courier is CourierEntity) {
            courier.setPos(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5)
            level.addFreshEntity(courier)
            player.displayClientMessage(Component.literal("Spawned courier"), true)
        }
        if (!player.abilities.instabuild) {
            context.itemInHand.shrink(1)
        }
        return InteractionResult.SUCCESS
    }
}
