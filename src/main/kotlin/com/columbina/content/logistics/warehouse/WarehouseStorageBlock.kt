package com.columbina.content.logistics.warehouse

import com.columbina.runtime.init.ColumbinaBlockEntities
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState

enum class WarehouseStorageTier(val slotCount: Int) {
    SMALL(9),
    MEDIUM(18),
    LARGE(27),
}

class WarehouseStorageBlock(
    val tier: WarehouseStorageTier,
    properties: BlockBehaviour.Properties,
) : BaseEntityBlock(properties) {
    override fun codec(): MapCodec<out BaseEntityBlock> = simpleCodec { props -> WarehouseStorageBlock(tier, props) }

    override fun getRenderShape(state: BlockState): RenderShape = RenderShape.MODEL

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = WarehouseStorageBlockEntity(pos, state, tier)

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        super.setPlacedBy(level, pos, state, placer, stack)
        val player = placer as? Player ?: return
        (level.getBlockEntity(pos) as? WarehouseStorageBlockEntity)?.setOwner(player)
    }

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>,
    ): BlockEntityTicker<T>? = createTickerHelper(blockEntityType, ColumbinaBlockEntities.WAREHOUSE_STORAGE) { _, _, _, blockEntity ->
        blockEntity.tick()
    }
}
