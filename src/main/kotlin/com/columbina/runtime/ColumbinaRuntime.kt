package com.columbina.runtime

import com.columbina.content.research.ImportedResearchRegistry
import com.columbina.runtime.init.ColumbinaBlockEntities
import com.columbina.runtime.init.ColumbinaBlocks
import com.columbina.runtime.init.ColumbinaCommands
import com.columbina.runtime.init.ColumbinaScreenHandlers
import com.columbina.runtime.network.ColumbinaNetworking
import com.columbina.runtime.persistence.ColumbinaSavedData
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ColumbinaRuntime {
    val logger: Logger = LoggerFactory.getLogger(ColumbinaIds.MOD_ID)

    private var bootstrapped = false

    fun bootstrapCommon() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        ImportedResearchRegistry.bootstrap()
        ColumbinaBlocks.bootstrap()
        ColumbinaBlockEntities.bootstrap()
        ColumbinaScreenHandlers.bootstrap()
        ColumbinaNetworking.bootstrap()
        ColumbinaSavedData.bootstrap()
        ColumbinaCommands.bootstrap()
    }
}
