package com.columbina.content.structure.screen

import com.columbina.content.structure.template.ImportedTemplateRegistry

class StructureSelectionScreenHandler(
    private val draftingMenu: DraftingStationScreenHandler,
) {
    fun getSurvivalTemplates(): List<String> = ImportedTemplateRegistry.getSurvivalTemplates().sorted()

    fun selectStructure(structureName: String) {
        draftingMenu.selectStructure(structureName)
    }
}
