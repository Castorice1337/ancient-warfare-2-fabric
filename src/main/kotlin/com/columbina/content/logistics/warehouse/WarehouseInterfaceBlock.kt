package com.columbina.content.logistics.warehouse

import com.columbina.runtime.init.ColumbinaBlockEntities
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class WarehouseInterfaceBlock(properties: BlockBehaviour.Properties) : BaseEntityBlock(properties) {
    companion object {
        val CODEC: MapCodec<WarehouseInterfaceBlock> = simpleCodec(::WarehouseInterfaceBlock)
    }

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = WarehouseInterfaceBlockEntity(pos, state)

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        val player = placer as? Player ?: return
        (level.getBlockEntity(pos) as? WarehouseInterfaceBlockEntity)?.setOwner(player)
    }

    override fun getMenuProvider(state: BlockState, level: Level, pos: BlockPos): MenuProvider? {
        return level.getBlockEntity(pos) as? MenuProvider
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult,
    ): InteractionResult {
        val provider = getMenuProvider(state, level, pos)
        if (provider != null && player is ServerPlayer) {
            player.openMenu(provider)
        }
        return InteractionResult.SUCCESS
    }

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>,
    ): BlockEntityTicker<T>? = createTickerHelper(blockEntityType, ColumbinaBlockEntities.WAREHOUSE_INTERFACE) { _, _, _, blockEntity ->
        blockEntity.tick()
    }
}
