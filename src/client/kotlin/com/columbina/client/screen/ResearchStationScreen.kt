package com.columbina.client.screen

import com.columbina.client.research.ClientResearchState
import com.columbina.content.research.ImportedResearchRegistry
import com.columbina.content.research.screen.ResearchStationScreenHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class ResearchStationScreen(
    menu: ResearchStationScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<ResearchStationScreenHandler>(menu, inventory, title) {
    private val background = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png")
    private val containerRows = 3

    init {
        imageWidth = 176
        imageHeight = 114 + (containerRows * 18)
        inventoryLabelY = imageHeight - 94
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        val x = leftPos
        val y = topPos
        val topSectionHeight = (containerRows * 18) + 17

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, x, y, 0f, 0f, imageWidth, topSectionHeight, 256, 256)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, x, y + topSectionHeight, 0f, 126f, imageWidth, 96, 256, 256)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        super.renderLabels(guiGraphics, mouseX, mouseY)

        val snapshot = ClientResearchState.snapshotFor(menu.playerKey)
        val currentGoal = snapshot?.currentResearch ?: "-"
        val queuedCount = snapshot?.queuedResearch?.size ?: 0
        val importedGoal = ImportedResearchRegistry.allGoals().firstOrNull()?.id ?: "-"

        guiGraphics.drawString(font, Component.translatable("guistrings.research.current_goal"), 8, 6, 0x404040, false)
        guiGraphics.drawString(font, Component.literal(currentGoal), 96, 6, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.progress"), 8, 28, 0x404040, false)
        guiGraphics.drawString(font, Component.literal(menu.storedEnergy.toString()), 96, 28, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.queued_research"), 8, 50, 0x404040, false)
        guiGraphics.drawString(font, Component.literal(queuedCount.toString()), 128, 50, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.learnable_research"), 8, 68, 0x404040, false)
        guiGraphics.drawString(font, Component.literal(importedGoal), 128, 68, 0x404040, false)
    }
}
