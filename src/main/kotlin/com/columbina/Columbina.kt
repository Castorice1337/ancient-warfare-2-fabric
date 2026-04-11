package com.columbina

import com.columbina.runtime.ColumbinaRuntime
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object Columbina : ModInitializer {
    private val logger = LoggerFactory.getLogger("columbina")

	override fun onInitialize() {
		ColumbinaRuntime.bootstrapCommon()
		logger.info("Bootstrapped Columbina runtime with columbina-owned glue and ancientwarfare imported-resource ids")
	}
}
