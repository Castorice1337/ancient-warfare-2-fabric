package com.columbina.content.research

import com.google.gson.JsonElement

object ImportedResearchRegistry {
    private var bootstrapped = false
    private var goals: Map<String, ImportedResearchGoal> = emptyMap()
    private var recipes: Map<String, JsonElement> = emptyMap()

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        goals = LegacyResearchLoader.loadGoals()
        recipes = LegacyResearchLoader.loadRecipePayloads()
        bootstrapped = true
    }

    fun allGoals(): Collection<ImportedResearchGoal> = goals.values

    fun allRecipePayloads(): Map<String, JsonElement> = recipes.toMap()

    fun getGoal(id: String): ImportedResearchGoal? = goals[id]

    fun hasGoal(id: String): Boolean = id in goals

    fun getRecipe(path: String): JsonElement? = recipes[path]

    fun getRecipeResearch(path: String): String? {
        val recipe = recipes[path]?.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return null
        return recipe.get("research")?.asString
    }

    fun getRecipeResultItem(path: String): String? {
        val recipe = recipes[path]?.takeIf(JsonElement::isJsonObject)?.asJsonObject ?: return null
        return recipe.getAsJsonObject("result")?.get("item")?.asString
    }

    fun recipesForResearch(goalId: String): List<String> {
        return recipes.entries
            .filter { entry -> getRecipeResearch(entry.key) == goalId }
            .map { it.key }
            .sorted()
    }

    fun researchExists(id: String): Boolean = id in goals

    fun getResearchableGoals(completed: Set<String>, queued: List<String>, inProgress: String?): Set<String> {
        val totalKnowledge = linkedSetOf<String>()
        totalKnowledge.addAll(completed)
        totalKnowledge.addAll(queued)
        if (inProgress != null) {
            totalKnowledge.add(inProgress)
        }

        return goals.values
            .filterNot { it.id in totalKnowledge }
            .filter { goal -> resolveFullDependencies(goal.id).all { dependency -> dependency in totalKnowledge } }
            .mapTo(linkedSetOf()) { it.id }
    }

    fun resolveFullDependencies(goalId: String): Set<String> {
        val found = linkedSetOf<String>()
        val open = ArrayDeque<String>()
        val first = goals[goalId] ?: return emptySet()

        open.addAll(first.dependencies)

        while (open.isNotEmpty()) {
            val dependency = open.removeFirst()

            if (!found.add(dependency)) {
                continue
            }

            val nested = goals[dependency] ?: continue
            nested.dependencies
                .filterNot(found::contains)
                .forEach(open::addLast)
        }

        return found
    }
}
