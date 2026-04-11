package com.columbina.content.research

import com.google.gson.JsonObject
import net.minecraft.world.Container

data class ImportedResearchGoal(
    val id: String,
    val time: Int,
    val dependencies: List<String>,
    val resources: List<ResearchRequirement>,
    val raw: JsonObject,
) {
    val translationKey: String = "research.$id"

    fun canResearch(knownResearch: Set<String>): Boolean {
        return ImportedResearchRegistry.resolveFullDependencies(id).all(knownResearch::contains)
    }

    fun tryStart(primary: Container, adjacent: Container? = null, useAdjacentInventory: Boolean = false): Boolean {
        if (resources.all { it.canSatisfy(primary) }) {
            resources.forEach { it.tryConsume(primary) }
            return true
        }

        if (useAdjacentInventory && adjacent != null && resources.all { it.canSatisfy(adjacent) }) {
            resources.forEach { it.tryConsume(adjacent) }
            return true
        }

        return false
    }
}
