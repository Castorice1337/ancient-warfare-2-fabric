package com.columbina.content.research

import com.columbina.runtime.ColumbinaIds
import com.columbina.runtime.ColumbinaRuntime
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.io.InputStreamReader
import net.minecraft.resources.Identifier
import java.net.URI
import java.nio.file.FileSystemNotFoundException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.charset.StandardCharsets
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension

object LegacyResearchLoader {
    private val gson = Gson()
    private const val RESEARCH_ROOT = "assets/${ColumbinaIds.LEGACY_NAMESPACE}/registry/research"
    private const val RESEARCH_RECIPE_ROOT = "assets/${ColumbinaIds.LEGACY_NAMESPACE}/research_recipes"
    private const val SOURCE_ROOT = "src/main/resources"

    fun loadGoals(): Map<String, ImportedResearchGoal> {
        return listJsonResources(RESEARCH_ROOT).mapNotNull { resourcePath ->
            val id = Paths.get(resourcePath).nameWithoutExtension
            loadGoal(id)?.let { id to it }
        }.toMap(linkedMapOf())
    }

    fun loadGoal(id: String): ImportedResearchGoal? {
        val resourcePath = "$RESEARCH_ROOT/$id.json"
        val json = readJson(resourcePath) ?: return null

        return ImportedResearchGoal(
            id = json.get("name")?.asString ?: id,
            time = json.get("time")?.asInt ?: 0,
            dependencies = json.getAsJsonArray("dependencies")?.map { it.asString } ?: emptyList(),
            resources = parseRequirements(json.getAsJsonArray("resources")),
            raw = json,
        )
    }

    fun loadRecipePayloads(): Map<String, JsonElement> {
        return listJsonResources(RESEARCH_RECIPE_ROOT).mapNotNull { resourcePath ->
            val relative = resourcePath.removePrefix("$RESEARCH_RECIPE_ROOT/").removeSuffix(".json")
            readElement(resourcePath)?.let { relative to it }
        }.toMap(linkedMapOf())
    }

    private fun readJson(path: String): JsonObject? {
        return readElement(path)?.takeIf(JsonElement::isJsonObject)?.asJsonObject
    }

    private fun readElement(path: String): JsonElement? {
        val stream = javaClass.classLoader.getResourceAsStream(path) ?: run {
            ColumbinaRuntime.logger.warn("Missing legacy research resource {}", path)
            return null
        }

        stream.use { input ->
            InputStreamReader(input, StandardCharsets.UTF_8).use { reader ->
                return gson.fromJson(reader, JsonElement::class.java)
            }
        }
    }

    fun legacyGoalPath(id: String): String = ColumbinaIds.legacyId("registry/research/$id.json").toString()

    fun legacyRecipePath(path: String): String = ColumbinaIds.legacyId("research_recipes/$path.json").toString()

    private fun parseRequirements(resources: JsonArray?): List<ResearchRequirement> {
        if (resources == null) {
            return emptyList()
        }

        return resources.mapNotNull { element ->
            val json = element.asJsonObject
            when (val type = json.get("type")?.asString) {
                "ancientwarfare:item_count" -> {
                    val itemId = Identifier.parse(json.get("item").asString)
                    ItemCountRequirement(itemId, json.get("count").asInt)
                }
                "ancientwarfare:ore_dict_count" -> {
                    OreDictCountRequirement(json.get("ore").asString, json.get("count").asInt)
                }
                null -> {
                    val itemId = json.get("item")?.asString?.let(Identifier::parse)
                    if (itemId != null) {
                        ItemCountRequirement(itemId, json.get("count")?.asInt ?: 1)
                    } else {
                        ColumbinaRuntime.logger.warn("Unsupported research requirement entry {}", json)
                        null
                    }
                }
                else -> {
                    ColumbinaRuntime.logger.warn("Unsupported research requirement type {}", type)
                    null
                }
            }
        }
    }

    private fun listJsonResources(root: String): List<String> {
        val sourcePath = Paths.get(SOURCE_ROOT).resolve(root)

        if (Files.exists(sourcePath)) {
            return Files.walk(sourcePath)
                .filter { it.isRegularFile() && it.extension == "json" }
                .map { sourcePath.relativize(it).invariantSeparatorsPathString }
                .map { "$root/$it" }
                .sorted()
                .toList()
        }

        val rootUrl = javaClass.classLoader.getResource(root) ?: return emptyList()
        val uri = rootUrl.toURI()

        if (uri.scheme == "jar") {
            val raw = uri.toString()
            val bang = raw.indexOf('!')
            val jarUri = URI.create(raw.substring(0, bang))
            val internalPath = raw.substring(bang + 1)
            val fs = try {
                FileSystems.getFileSystem(jarUri)
            } catch (_: FileSystemNotFoundException) {
                FileSystems.newFileSystem(jarUri, emptyMap<String, Any>())
            }
            val jarRoot = fs.getPath(internalPath)
            return Files.walk(jarRoot)
                .filter { it.isRegularFile() && it.extension == "json" }
                .map { jarRoot.relativize(it).invariantSeparatorsPathString }
                .map { "$root/$it" }
                .sorted()
                .toList()
        }

        val path = Paths.get(uri)
        return Files.walk(path)
            .filter { it.isRegularFile() && it.extension == "json" }
            .map { path.relativize(it).invariantSeparatorsPathString }
            .map { "$root/$it" }
            .sorted()
            .toList()
    }
}
