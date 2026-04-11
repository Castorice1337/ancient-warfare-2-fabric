package com.columbina.runtime.init

import com.columbina.content.research.blockentity.ResearchStationBlockEntity
import com.columbina.runtime.ColumbinaIds
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object ColumbinaBlockEntities {
    lateinit var RESEARCH_STATION: BlockEntityType<ResearchStationBlockEntity>
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_STATION = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("research_station"),
            FabricBlockEntityTypeBuilder.create(::ResearchStationBlockEntity, ColumbinaBlocks.RESEARCH_STATION).build(),
        )
    }
}
