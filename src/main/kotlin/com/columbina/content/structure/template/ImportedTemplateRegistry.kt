package com.columbina.content.structure.template

object ImportedTemplateRegistry {
    private var bootstrapped = false
    private var templates: Map<String, ImportedStructureTemplate> = emptyMap()

    fun bootstrap() {
        if (bootstrapped) {
            return
        }
        templates = LegacyTemplateLoader.loadTemplates()
        bootstrapped = true
    }

    fun reload() {
        templates = LegacyTemplateLoader.loadTemplates()
        bootstrapped = true
    }

    fun allTemplates(): Collection<ImportedStructureTemplate> = templates.values

    fun getTemplate(name: String): ImportedStructureTemplate? = templates[name]

    fun templateExists(name: String): Boolean = name in templates

    fun getSurvivalTemplates(): Set<String> = templates.values.filter { it.isSurvival }.mapTo(linkedSetOf()) { it.name }

    fun getSurvivalTemplateObjects(): List<ImportedStructureTemplate> {
        return templates.values.filter { it.isSurvival }.sortedBy { it.name.lowercase() }
    }
}
