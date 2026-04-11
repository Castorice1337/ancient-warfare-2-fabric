package com.columbina.runtime.init

import com.columbina.debug.ResearchSliceDebugCommands
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback

object ColumbinaCommands {
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            ResearchSliceDebugCommands.register(dispatcher)
        }
    }
}
