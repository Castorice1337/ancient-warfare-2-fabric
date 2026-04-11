package com.columbina.client.runtime

import com.columbina.client.research.ClientResearchState
import com.columbina.client.screen.ResearchStationScreen
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.chunk.ChunkSectionLayer

object ColumbinaClientRuntime {
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true
        ClientResearchState.bootstrap()
        BlockRenderLayerMap.putBlock(ColumbinaBlocks.RESEARCH_STATION, ChunkSectionLayer.CUTOUT)
        MenuScreens.register(ColumbinaScreenHandlers.RESEARCH_STATION, ::ResearchStationScreen)
    }
}
