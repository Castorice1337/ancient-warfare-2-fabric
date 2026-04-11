package com.columbina.runtime.research

import com.columbina.runtime.network.ResearchInitPayload
import com.columbina.runtime.network.ResearchStartPayload
import com.columbina.runtime.network.ResearchUpdatePayload
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

object ResearchRuntimeService {
    // TODO Phase 10: review UUID-backed identity before multiplayer hardening.
    fun playerKey(player: ServerPlayer): String = player.name.string

    fun getSnapshot(level: ServerLevel, playerKey: String): ResearchEntrySnapshot = ResearchSavedData.get(level).getSnapshot(playerKey)

    fun sendInit(player: ServerPlayer) {
        ServerPlayNetworking.send(
            player,
            ResearchInitPayload(
                playerKey = playerKey(player),
                snapshot = getSnapshot(player.level(), playerKey(player)),
            ),
        )
    }

    fun sendInit(level: ServerLevel, playerKey: String, player: ServerPlayer) {
        ServerPlayNetworking.send(
            player,
            ResearchInitPayload(
                playerKey = playerKey,
                snapshot = getSnapshot(level, playerKey),
            ),
        )
    }

    fun queueGoal(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).update(playerKey) { current ->
            if (goal in current.queuedResearch || goal == current.currentResearch || goal in current.completedResearch) {
                current
            } else {
                current.copy(queuedResearch = current.queuedResearch + goal)
            }
        }

        broadcastQueueUpdate(level, playerKey, goal, add = true, live = false, snapshot = snapshot)
        return snapshot
    }

    fun removeQueuedGoal(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).update(playerKey) { current ->
            current.copy(queuedResearch = current.queuedResearch.filterNot { it == goal })
        }

        broadcastQueueUpdate(level, playerKey, goal, add = false, live = false, snapshot = snapshot)
        return snapshot
    }

    fun startResearch(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).update(playerKey) { current ->
            current.copy(
                currentResearch = goal,
                currentProgress = 0,
                queuedResearch = current.queuedResearch.filterNot { it == goal },
            )
        }

        broadcastStart(level, playerKey, goal, started = true, snapshot = snapshot)
        return snapshot
    }

    fun finishResearch(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).update(playerKey) { current ->
            current.copy(
                currentResearch = null,
                currentProgress = 0,
                completedResearch = (current.completedResearch + goal).distinct(),
            )
        }

        broadcastStart(level, playerKey, goal, started = false, snapshot = snapshot)
        return snapshot
    }

    fun setProgress(level: ServerLevel, playerKey: String, progress: Int): ResearchEntrySnapshot {
        return ResearchSavedData.get(level).update(playerKey) { current ->
            current.copy(currentProgress = progress)
        }
    }

    fun clear(level: ServerLevel, playerKey: String) {
        ResearchSavedData.get(level).clear(playerKey)
        broadcastSnapshot(level, playerKey)
    }

    fun replaceSnapshot(level: ServerLevel, playerKey: String, snapshot: ResearchEntrySnapshot): ResearchEntrySnapshot {
        val updated = ResearchSavedData.get(level).replace(playerKey, snapshot)
        broadcastSnapshot(level, playerKey)
        return updated
    }

    private fun broadcastSnapshot(level: ServerLevel, playerKey: String) {
        val payload = ResearchInitPayload(playerKey, getSnapshot(level, playerKey))
        level.server.playerList.players.forEach { ServerPlayNetworking.send(it, payload) }
    }

    fun broadcastStart(level: ServerLevel, playerKey: String, goal: String, started: Boolean, snapshot: ResearchEntrySnapshot) {
        val payload = ResearchStartPayload(
            playerKey = playerKey,
            goal = goal,
            started = started,
            snapshot = snapshot,
        )

        level.server.playerList.players.forEach { ServerPlayNetworking.send(it, payload) }
    }

    fun broadcastQueueUpdate(level: ServerLevel, playerKey: String, goal: String, add: Boolean, live: Boolean, snapshot: ResearchEntrySnapshot) {
        val payload = ResearchUpdatePayload(
            playerKey = playerKey,
            goal = goal,
            add = add,
            live = live,
            snapshot = snapshot,
        )

        level.server.playerList.players.forEach { ServerPlayNetworking.send(it, payload) }
    }
}
