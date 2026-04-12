package com.columbina.runtime.init

import com.columbina.content.structure.item.StructureBuilderItem
import com.columbina.content.logistics.item.CourierSpawnerItem
import com.columbina.content.logistics.item.RoutingOrderItem
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
    lateinit var ROUTING_ORDER: Item
        private set
    lateinit var TRADE_ORDER: Item
        private set
    lateinit var SPAWNER_COURIER: Item
        private set
    lateinit var STRUCTURE_BUILDER: Item
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

        ROUTING_ORDER = Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("routing_order"),
            RoutingOrderItem(itemProperties("routing_order", 1)),
        )
        TRADE_ORDER = Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("trade_order"),
            Item(itemProperties("trade_order", 1)),
        )
        SPAWNER_COURIER = Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("spawner_courier"),
            CourierSpawnerItem(itemProperties("spawner_courier", 1)),
        )
        STRUCTURE_BUILDER = Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("structure_builder"),
            StructureBuilderItem(itemProperties("structure_builder", 1)),
        )
    }

    private fun itemProperties(path: String, stackSize: Int): Item.Properties {
        return Item.Properties()
            .stacksTo(stackSize)
            .setId(ResourceKey.create(Registries.ITEM, ColumbinaIds.legacyId(path)))
    }
}
