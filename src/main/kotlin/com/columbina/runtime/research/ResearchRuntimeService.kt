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

    fun onPlayerLogin(player: ServerPlayer) {
        ResearchSavedData.get(player.level()).onPlayerLogin(playerKey(player))
        sendInit(player)
    }

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

    fun clearResearch(level: ServerLevel, playerKey: String) {
        ResearchSavedData.get(level).clearResearch(playerKey)
        broadcastSnapshot(level, playerKey)
    }

    fun removeResearch(level: ServerLevel, playerKey: String, research: String) {
        ResearchSavedData.get(level).removeResearch(playerKey, research)
        broadcastSnapshot(level, playerKey)
    }

    fun fillResearch(level: ServerLevel, playerKey: String) {
        ResearchSavedData.get(level).fillResearch(playerKey)
        broadcastSnapshot(level, playerKey)
    }

    fun addResearch(level: ServerLevel, playerKey: String, research: String) {
        ResearchSavedData.get(level).addResearch(playerKey, research)
        broadcastQueueUpdate(level, playerKey, research, add = true, live = true, snapshot = getSnapshot(level, playerKey))
    }

    fun hasPlayerCompleted(level: ServerLevel, playerKey: String, research: String): Boolean {
        return ResearchSavedData.get(level).hasPlayerCompleted(playerKey, research)
    }

    fun addResearchFromNotes(level: ServerLevel, playerKey: String, research: String): Boolean {
        if (hasPlayerCompleted(level, playerKey, research)) {
            return false
        }

        addResearch(level, playerKey, research)
        return true
    }

    fun addProgressFromNotes(level: ServerLevel, playerKey: String, research: String): Boolean {
        val goal = com.columbina.content.research.ImportedResearchRegistry.getGoal(research) ?: return false
        return ResearchSavedData.get(level).addProgress(playerKey, goal.time / 4)
    }

    fun getCompletedResearchFor(level: ServerLevel, playerKey: String): Set<String> = ResearchSavedData.get(level).getCompletedResearch(playerKey)

    fun getResearchQueueFor(level: ServerLevel, playerKey: String): List<String> = ResearchSavedData.get(level).getQueuedResearch(playerKey)

    fun getResearchableGoals(level: ServerLevel, playerKey: String): Set<String> = ResearchSavedData.get(level).getResearchableGoals(playerKey)

    fun getCurrentGoal(level: ServerLevel, playerKey: String): String? = ResearchSavedData.get(level).getCurrentResearch(playerKey)

    fun getProgress(level: ServerLevel, playerKey: String): Int = ResearchSavedData.get(level).getResearchProgress(playerKey)

    fun queueGoal(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).addQueuedResearch(playerKey, goal)

        broadcastQueueUpdate(level, playerKey, goal, add = true, live = false, snapshot = snapshot)
        return snapshot
    }

    fun removeQueuedGoal(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).removeQueuedResearch(playerKey, goal)

        broadcastQueueUpdate(level, playerKey, goal, add = false, live = false, snapshot = snapshot)
        return snapshot
    }

    fun startResearch(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).startResearch(playerKey, goal)

        broadcastStart(level, playerKey, goal, started = true, snapshot = snapshot)
        return snapshot
    }

    fun finishResearch(level: ServerLevel, playerKey: String, goal: String): ResearchEntrySnapshot {
        val snapshot = ResearchSavedData.get(level).finishResearch(playerKey, goal)

        broadcastStart(level, playerKey, goal, started = false, snapshot = snapshot)
        return snapshot
    }

    fun setProgress(level: ServerLevel, playerKey: String, progress: Int): ResearchEntrySnapshot {
        return ResearchSavedData.get(level).setCurrentResearchProgress(playerKey, progress)
    }

    fun addProgress(level: ServerLevel, playerKey: String, amount: Int): Boolean {
        val before = getSnapshot(level, playerKey)
        val result = ResearchSavedData.get(level).addProgress(playerKey, amount)
        val after = getSnapshot(level, playerKey)

        if (!result) {
            return false
        }

        if (before.currentResearch != null && after.currentResearch == null) {
            broadcastStart(level, playerKey, before.currentResearch, started = false, snapshot = after)
        } else {
            broadcastSnapshot(level, playerKey)
        }

        return true
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
