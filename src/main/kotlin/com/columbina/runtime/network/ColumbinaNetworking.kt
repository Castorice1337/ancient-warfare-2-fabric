package com.columbina.runtime.network

import com.columbina.runtime.research.ResearchRuntimeService
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

object ColumbinaNetworking {
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        PayloadTypeRegistry.playS2C().register(ResearchInitPayload.TYPE, ResearchInitPayload.STREAM_CODEC)
        PayloadTypeRegistry.playS2C().register(ResearchStartPayload.TYPE, ResearchStartPayload.STREAM_CODEC)
        PayloadTypeRegistry.playS2C().register(ResearchUpdatePayload.TYPE, ResearchUpdatePayload.STREAM_CODEC)

        ServerPlayConnectionEvents.JOIN.register { handler, _, _ ->
            ResearchRuntimeService.onPlayerLogin(handler.player)
        }
    }
}
