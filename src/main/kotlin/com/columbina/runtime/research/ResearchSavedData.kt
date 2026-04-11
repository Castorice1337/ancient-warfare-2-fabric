package com.columbina.runtime.research

import com.columbina.content.research.ImportedResearchRegistry
import com.mojang.serialization.Codec
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.level.saveddata.SavedData
import net.minecraft.world.level.saveddata.SavedDataType

class ResearchSavedData(
    private val entries: MutableMap<String, ResearchEntrySnapshot> = linkedMapOf(),
) : SavedData() {
    companion object {
        const val DATA_ID = "columbina_research_runtime"

        val CODEC: Codec<ResearchSavedData> = Codec.unboundedMap(Codec.STRING, ResearchEntrySnapshot.CODEC)
            .xmap(
                { ResearchSavedData(it.toMutableMap()).apply { cleanInvalidEntries() } },
                { it.entries.toMap() },
            )

        val TYPE = SavedDataType(
            DATA_ID,
            ::ResearchSavedData,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE,
        )

        fun get(level: ServerLevel): ResearchSavedData = level.dataStorage.computeIfAbsent(TYPE)
    }

    fun onPlayerLogin(playerKey: String) {
        if (playerKey !in entries) {
            entries[playerKey] = ResearchEntrySnapshot.EMPTY
            setDirty()
        }
    }

    fun getSnapshot(playerKey: String): ResearchEntrySnapshot = entries[playerKey] ?: ResearchEntrySnapshot.EMPTY

    fun allEntries(): Map<String, ResearchEntrySnapshot> = entries.toMap()

    fun update(playerKey: String, transform: (ResearchEntrySnapshot) -> ResearchEntrySnapshot): ResearchEntrySnapshot {
        val updated = sanitize(transform(getSnapshot(playerKey)))
        entries[playerKey] = updated
        setDirty()
        return updated
    }

    fun replace(playerKey: String, snapshot: ResearchEntrySnapshot): ResearchEntrySnapshot {
        entries[playerKey] = sanitize(snapshot)
        setDirty()
        return entries[playerKey] ?: ResearchEntrySnapshot.EMPTY
    }

    fun clear(playerKey: String) {
        entries.remove(playerKey)
        setDirty()
    }

    fun clearResearch(playerKey: String): ResearchEntrySnapshot = replace(playerKey, ResearchEntrySnapshot.EMPTY)

    fun removeResearch(playerKey: String, research: String): ResearchEntrySnapshot = update(playerKey) { current ->
        current.copy(completedResearch = current.completedResearch.filterNot { it == research })
    }

    fun fillResearch(playerKey: String): ResearchEntrySnapshot = replace(
        playerKey,
        ResearchEntrySnapshot(
            currentResearch = null,
            currentProgress = 0,
            completedResearch = ImportedResearchRegistry.allGoals().map { it.id }.sorted(),
            queuedResearch = emptyList(),
        ),
    )

    fun addResearch(playerKey: String, research: String): ResearchEntrySnapshot = update(playerKey) { current ->
        current.copy(
            currentResearch = if (current.currentResearch == research) null else current.currentResearch,
            currentProgress = if (current.currentResearch == research) 0 else current.currentProgress,
            completedResearch = (current.completedResearch + research).distinct(),
            queuedResearch = current.queuedResearch.filterNot { it == research },
        )
    }

    fun hasPlayerCompleted(playerKey: String, research: String): Boolean = research in getSnapshot(playerKey).completedResearch

    fun getCompletedResearch(playerKey: String): Set<String> = getSnapshot(playerKey).completedResearch.toSet()

    fun getQueuedResearch(playerKey: String): List<String> = getSnapshot(playerKey).queuedResearch

    fun getCurrentResearch(playerKey: String): String? = getSnapshot(playerKey).currentResearch

    fun getResearchProgress(playerKey: String): Int = getSnapshot(playerKey).currentProgress

    fun hasResearchStarted(playerKey: String): Boolean {
        val snapshot = getSnapshot(playerKey)
        return snapshot.currentResearch != null && snapshot.currentProgress >= 0
    }

    fun getResearchableGoals(playerKey: String): Set<String> {
        val snapshot = getSnapshot(playerKey)
        return ImportedResearchRegistry.getResearchableGoals(
            completed = snapshot.completedResearch.toSet(),
            queued = snapshot.queuedResearch,
            inProgress = snapshot.currentResearch,
        )
    }

    fun addQueuedResearch(playerKey: String, goal: String): ResearchEntrySnapshot = update(playerKey) { current ->
        val alreadyKnown = goal == current.currentResearch || goal in current.completedResearch || goal in current.queuedResearch
        val researchable = goal in ImportedResearchRegistry.getResearchableGoals(
            completed = current.completedResearch.toSet(),
            queued = current.queuedResearch,
            inProgress = current.currentResearch,
        )

        if (alreadyKnown || !researchable) {
            current
        } else {
            current.copy(queuedResearch = current.queuedResearch + goal)
        }
    }

    fun removeQueuedResearch(playerKey: String, goal: String): ResearchEntrySnapshot = update(playerKey) { current ->
        if (goal !in current.queuedResearch) {
            return@update current
        }

        val goalsToValidate = mutableListOf<String>()
        val rebuiltQueue = mutableListOf<String>()
        var found = false

        current.queuedResearch.forEach { queued ->
            when {
                !found && queued == goal -> found = true
                found -> goalsToValidate += queued
                else -> rebuiltQueue += queued
            }
        }

        val totalResearch = linkedSetOf<String>()
        totalResearch.addAll(current.completedResearch)
        totalResearch.addAll(rebuiltQueue)
        current.currentResearch?.let(totalResearch::add)

        goalsToValidate.forEach { queued ->
            val dependenciesMet = ImportedResearchRegistry.resolveFullDependencies(queued).all(totalResearch::contains)
            if (dependenciesMet) {
                rebuiltQueue += queued
                totalResearch += queued
            }
        }

        current.copy(queuedResearch = rebuiltQueue)
    }

    fun startResearch(playerKey: String, goal: String): ResearchEntrySnapshot = update(playerKey) { current ->
        val canStart = current.currentResearch == null && goal in current.queuedResearch
        if (!canStart) {
            current
        } else {
            current.copy(
                currentResearch = goal,
                currentProgress = 0,
                queuedResearch = current.queuedResearch.filterNot { it == goal },
            )
        }
    }

    fun finishResearch(playerKey: String, goal: String): ResearchEntrySnapshot = update(playerKey) { current ->
        if (current.currentResearch != goal) {
            current
        } else {
            current.copy(
                currentResearch = null,
                currentProgress = 0,
                completedResearch = (current.completedResearch + goal).distinct(),
            )
        }
    }

    fun setCurrentResearchProgress(playerKey: String, progress: Int): ResearchEntrySnapshot = update(playerKey) { current ->
        current.copy(currentProgress = progress)
    }

    fun addProgress(playerKey: String, amount: Int): Boolean {
        val current = getSnapshot(playerKey)
        val goalId = current.currentResearch ?: return false
        val goal = ImportedResearchRegistry.getGoal(goalId) ?: return false
        val progress = current.currentProgress + amount

        if (progress >= goal.time) {
            finishResearch(playerKey, goalId)
        } else {
            setCurrentResearchProgress(playerKey, progress)
        }

        return true
    }

    private fun cleanInvalidEntries() {
        val sanitized = entries.mapValues { (_, snapshot) -> sanitize(snapshot) }
        entries.clear()
        entries.putAll(sanitized)
    }

    private fun sanitize(snapshot: ResearchEntrySnapshot): ResearchEntrySnapshot {
        val completedResearch = snapshot.completedResearch.filter(ImportedResearchRegistry::researchExists).distinct()
        val queuedResearch = snapshot.queuedResearch.filter(ImportedResearchRegistry::researchExists).distinct()
        val currentResearch = snapshot.currentResearch?.takeIf(ImportedResearchRegistry::researchExists)

        return snapshot.copy(
            currentResearch = currentResearch,
            currentProgress = if (currentResearch == null) 0 else snapshot.currentProgress,
            completedResearch = completedResearch,
            queuedResearch = queuedResearch,
        )
    }
}
