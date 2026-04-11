package com.columbina.runtime.init

import com.columbina.content.research.screen.ResearchStationScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.inventory.MenuType

object ColumbinaScreenHandlers {
    lateinit var RESEARCH_STATION: MenuType<ResearchStationScreenHandler>
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_STATION = Registry.register(
            BuiltInRegistries.MENU,
            ColumbinaIds.columbinaId("research_station"),
            ExtendedScreenHandlerType(::ResearchStationScreenHandler, BlockPos.STREAM_CODEC),
        )
    }
}
