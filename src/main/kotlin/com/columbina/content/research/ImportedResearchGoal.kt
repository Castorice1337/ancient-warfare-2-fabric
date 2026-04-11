package com.columbina.content.research

import com.google.gson.JsonObject

data class ImportedResearchGoal(
    val id: String,
    val time: Int,
    val dependencies: List<String>,
    val raw: JsonObject,
) {
    val translationKey: String = "research.$id"
}
