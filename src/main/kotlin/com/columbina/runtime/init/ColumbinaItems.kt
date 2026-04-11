package com.columbina.runtime.init

import com.columbina.content.research.item.ResearchBookItem
import com.columbina.runtime.ColumbinaIds
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item

object ColumbinaItems {
    lateinit var RESEARCH_BOOK: Item
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_BOOK = Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("research_book"),
            ResearchBookItem(
                Item.Properties().setId(
                    ResourceKey.create(Registries.ITEM, ColumbinaIds.legacyId("research_book")),
                ),
            ),
        )
    }
}
