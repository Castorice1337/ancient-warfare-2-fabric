package com.columbina.client.runtime

import com.columbina.client.research.ClientResearchState
import com.columbina.client.screen.ResearchStationScreen
import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.client.gui.screens.MenuScreens

object ColumbinaClientRuntime {
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true
        ClientResearchState.bootstrap()
        MenuScreens.register(ColumbinaScreenHandlers.RESEARCH_STATION, ::ResearchStationScreen)
    }
}
