package com.columbina.runtime.init

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.minecraft.world.item.CreativeModeTabs

object ColumbinaCreativeTabs {
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register { entries ->
            entries.accept(ColumbinaBlocks.RESEARCH_STATION)
            entries.accept(ColumbinaBlocks.WAREHOUSE_CONTROL)
            entries.accept(ColumbinaBlocks.WAREHOUSE_STORAGE_SMALL)
            entries.accept(ColumbinaBlocks.WAREHOUSE_STORAGE_MEDIUM)
            entries.accept(ColumbinaBlocks.WAREHOUSE_STORAGE_LARGE)
            entries.accept(ColumbinaBlocks.WAREHOUSE_INTERFACE)
            entries.accept(ColumbinaBlocks.WAREHOUSE_STOCK_VIEWER)
            entries.accept(ColumbinaBlocks.WAREHOUSE_STOCK_LINKER)
        }

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register { entries ->
            entries.accept(ColumbinaItems.RESEARCH_BOOK)
            entries.accept(ColumbinaItems.ROUTING_ORDER)
            entries.accept(ColumbinaItems.TRADE_ORDER)
        }

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register { entries ->
            entries.accept(ColumbinaItems.SPAWNER_COURIER)
        }
    }
}
