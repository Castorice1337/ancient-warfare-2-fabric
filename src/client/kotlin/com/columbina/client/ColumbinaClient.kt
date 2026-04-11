package com.columbina.client

import com.columbina.client.runtime.ColumbinaClientRuntime
import net.fabricmc.api.ClientModInitializer

object ColumbinaClient : ClientModInitializer {
	override fun onInitializeClient() {
		ColumbinaClientRuntime.bootstrap()
	}
}
