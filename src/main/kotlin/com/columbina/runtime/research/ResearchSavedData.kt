package com.columbina.runtime.research

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
                { ResearchSavedData(it.toMutableMap()) },
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

    fun getSnapshot(playerKey: String): ResearchEntrySnapshot = entries[playerKey] ?: ResearchEntrySnapshot.EMPTY

    fun allEntries(): Map<String, ResearchEntrySnapshot> = entries.toMap()

    fun update(playerKey: String, transform: (ResearchEntrySnapshot) -> ResearchEntrySnapshot): ResearchEntrySnapshot {
        val updated = transform(getSnapshot(playerKey))
        entries[playerKey] = updated
        setDirty()
        return updated
    }

    fun replace(playerKey: String, snapshot: ResearchEntrySnapshot): ResearchEntrySnapshot {
        entries[playerKey] = snapshot
        setDirty()
        return snapshot
    }

    fun clear(playerKey: String) {
        entries.remove(playerKey)
        setDirty()
    }
}
