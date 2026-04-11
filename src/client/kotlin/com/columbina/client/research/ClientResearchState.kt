package com.columbina.client.research

import com.columbina.runtime.network.ResearchInitPayload
import com.columbina.runtime.network.ResearchStartPayload
import com.columbina.runtime.network.ResearchUpdatePayload
import com.columbina.runtime.research.ResearchEntrySnapshot
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

object ClientResearchState {
    private val snapshots = mutableMapOf<String, ResearchEntrySnapshot>()
    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        ClientPlayNetworking.registerGlobalReceiver(ResearchInitPayload.TYPE) { payload, _ ->
            snapshots[payload.playerKey] = payload.snapshot
        }
        ClientPlayNetworking.registerGlobalReceiver(ResearchStartPayload.TYPE) { payload, _ ->
            snapshots[payload.playerKey] = payload.snapshot
        }
        ClientPlayNetworking.registerGlobalReceiver(ResearchUpdatePayload.TYPE) { payload, _ ->
            snapshots[payload.playerKey] = payload.snapshot
        }
    }

    fun snapshotFor(playerKey: String?): ResearchEntrySnapshot? = playerKey?.let(snapshots::get)

    fun clear() {
        snapshots.clear()
    }
}
