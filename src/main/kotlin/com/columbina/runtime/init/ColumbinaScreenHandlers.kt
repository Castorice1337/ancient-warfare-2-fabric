package com.columbina.runtime.init

import com.columbina.content.logistics.screen.RoutingOrderScreenHandler
import com.columbina.content.logistics.screen.WarehouseControlScreenHandler
import com.columbina.content.research.screen.ResearchBookScreenHandler
import com.columbina.content.research.screen.ResearchStationScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.inventory.MenuType

object ColumbinaScreenHandlers {
    lateinit var RESEARCH_BOOK: MenuType<ResearchBookScreenHandler>
        private set
    lateinit var WAREHOUSE_CONTROL: MenuType<WarehouseControlScreenHandler>
        private set
    lateinit var ROUTING_ORDER: MenuType<RoutingOrderScreenHandler>
        private set

    lateinit var RESEARCH_STATION: MenuType<ResearchStationScreenHandler>
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_BOOK = Registry.register(
            BuiltInRegistries.MENU,
            ColumbinaIds.columbinaId("research_book"),
            ExtendedScreenHandlerType(::ResearchBookScreenHandler, ByteBufCodecs.STRING_UTF8),
        )
        WAREHOUSE_CONTROL = Registry.register(
            BuiltInRegistries.MENU,
            ColumbinaIds.columbinaId("warehouse_control"),
            ExtendedScreenHandlerType(::WarehouseControlScreenHandler, BlockPos.STREAM_CODEC),
        )
        ROUTING_ORDER = Registry.register(
            BuiltInRegistries.MENU,
            ColumbinaIds.columbinaId("routing_order"),
            ExtendedScreenHandlerType(::RoutingOrderScreenHandler, ByteBufCodecs.STRING_UTF8),
        )

        RESEARCH_STATION = Registry.register(
            BuiltInRegistries.MENU,
            ColumbinaIds.columbinaId("research_station"),
            ExtendedScreenHandlerType(::ResearchStationScreenHandler, BlockPos.STREAM_CODEC),
        )
    }
}
