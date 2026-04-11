package com.columbina.content.research

import com.columbina.runtime.ColumbinaIds
import com.columbina.runtime.ColumbinaRuntime
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object LegacyResearchLoader {
    private val gson = Gson()
    private val sampleGoals = listOf("agriculture")
    private val sampleRecipes = listOf("npc/trade_order")

    fun loadGoals(): Map<String, ImportedResearchGoal> {
        return sampleGoals.mapNotNull { id ->
            loadGoal(id)?.let { id to it }
        }.toMap(linkedMapOf())
    }

    fun loadGoal(id: String): ImportedResearchGoal? {
        val resourcePath = "assets/${ColumbinaIds.LEGACY_NAMESPACE}/registry/research/$id.json"
        val json = readJson(resourcePath) ?: return null

        return ImportedResearchGoal(
            id = json.get("name")?.asString ?: id,
            time = json.get("time")?.asInt ?: 0,
            dependencies = json.getAsJsonArray("dependencies")?.map { it.asString } ?: emptyList(),
            raw = json,
        )
    }

    fun loadSampleRecipePayloads(): Map<String, JsonObject> {
        return sampleRecipes.mapNotNull { path ->
            val resourcePath = "assets/${ColumbinaIds.LEGACY_NAMESPACE}/research_recipes/$path.json"
            readJson(resourcePath)?.let { path to it }
        }.toMap(linkedMapOf())
    }

    private fun readJson(path: String): JsonObject? {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: run {
            ColumbinaRuntime.logger.warn("Missing legacy research resource {}", path)
            return null
        }

        stream.use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                return gson.fromJson(reader, JsonObject::class.java)
            }
        }
    }

    fun legacyGoalPath(id: String): String = ColumbinaIds.legacyId("registry/research/$id.json").toString()

    fun legacyRecipePath(path: String): String = ColumbinaIds.legacyId("research_recipes/$path.json").toString()
}
