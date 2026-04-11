package com.columbina.runtime.init

import com.columbina.content.research.block.ResearchStationBlock
import com.columbina.runtime.ColumbinaIds
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour

object ColumbinaBlocks {
    lateinit var RESEARCH_STATION: ResearchStationBlock
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_STATION = Registry.register(
            BuiltInRegistries.BLOCK,
            ColumbinaIds.legacyId("research_station"),
            ResearchStationBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("research_station"))),
            ),
        )

        Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("research_station"),
            BlockItem(
                RESEARCH_STATION,
                Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ColumbinaIds.legacyId("research_station"))),
            ),
        )
    }
}
