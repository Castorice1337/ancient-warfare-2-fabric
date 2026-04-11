package com.columbina.content.research

import com.google.gson.JsonObject

object ImportedResearchRegistry {
    private var bootstrapped = false
    private var goals: Map<String, ImportedResearchGoal> = emptyMap()
    private var recipes: Map<String, JsonObject> = emptyMap()

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        goals = LegacyResearchLoader.loadGoals()
        recipes = LegacyResearchLoader.loadSampleRecipePayloads()
        bootstrapped = true
    }

    fun allGoals(): Collection<ImportedResearchGoal> = goals.values

    fun getGoal(id: String): ImportedResearchGoal? = goals[id]

    fun hasGoal(id: String): Boolean = id in goals

    fun getRecipe(path: String): JsonObject? = recipes[path]
}
