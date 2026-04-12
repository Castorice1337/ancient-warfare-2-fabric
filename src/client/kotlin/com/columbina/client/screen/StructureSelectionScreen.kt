package com.columbina.client.screen

import com.columbina.content.structure.screen.DraftingStationScreenHandler
import com.columbina.content.structure.screen.StructureSelectionScreenHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class StructureSelectionScreen(
    private val draftingMenu: DraftingStationScreenHandler,
    private val parent: DraftingStationScreen,
) : Screen(Component.literal("Select Structure")) {
    private val handler = StructureSelectionScreenHandler(draftingMenu)

    override fun init() {
        super.init()
        val templates = handler.getSurvivalTemplates()
        templates.take(10).forEachIndexed { index, template ->
            addRenderableWidget(
                Button.builder(Component.literal(template)) {
                    handler.selectStructure(template)
                    minecraft?.setScreen(parent)
                }.bounds(width / 2 - 100, 30 + (index * 18), 200, 16).build(),
            )
        }
        addRenderableWidget(
            Button.builder(Component.literal("Back")) {
                minecraft?.setScreen(parent)
            }.bounds(width / 2 - 40, height - 28, 80, 16).build(),
        )
    }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        guiGraphics.drawCenteredString(font, title, width / 2, 10, 0xFFFFFF)
        super.render(guiGraphics, mouseX, mouseY, partialTick)
    }
}
