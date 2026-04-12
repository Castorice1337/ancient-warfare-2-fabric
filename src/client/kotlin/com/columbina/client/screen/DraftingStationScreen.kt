package com.columbina.client.screen

import com.columbina.content.structure.screen.DraftingStationScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class DraftingStationScreen(
    menu: DraftingStationScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<DraftingStationScreenHandler>(menu, inventory, title) {
    private val background: Identifier = ColumbinaIds.legacyId("textures/gui/guibackgroundlarge.png")
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var selectionButton: Button

    init {
        imageWidth = 400
        imageHeight = 180
        inventoryLabelY = 64
        titleLabelX = 8
        titleLabelY = 6
    }

    override fun init() {
        super.init()
        selectionButton = addRenderableWidget(
            Button.builder(Component.literal("Select Structure")) {
                minecraft.setScreen(StructureSelectionScreen(menu, this))
            }.bounds(leftPos + 8, topPos + 20, 120, 16).build(),
        )
        startButton = addRenderableWidget(
            Button.builder(Component.literal("Start")) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, DraftingStationScreenHandler.BUTTON_START)
            }.bounds(leftPos + 8, topPos + 40, 52, 16).build(),
        )
        stopButton = addRenderableWidget(
            Button.builder(Component.literal("Stop")) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, DraftingStationScreenHandler.BUTTON_STOP)
            }.bounds(leftPos + 64, topPos + 40, 52, 16).build(),
        )
        refreshButtons()
    }

    override fun containerTick() {
        super.containerTick()
        refreshButtons()
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.fill(0, 0, width, height, 0x60000000)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(font, title, 8, 6, 0x404040, false)
        guiGraphics.drawString(font, Component.literal("Selected: ${menu.structureName ?: "none"}"), 8, 60, 0x404040, false)
        guiGraphics.drawString(font, Component.literal("Progress: ${menu.remainingTime} / ${menu.totalTime}"), 8, 72, 0x404040, false)

        menu.neededResources.take(6).forEachIndexed { index, resource ->
            guiGraphics.drawString(font, Component.literal("${resource.itemId} x${resource.count}"), 176, 20 + (index * 10), 0x404040, false)
        }
    }

    private fun refreshButtons() {
        startButton.active = menu.structureName != null && !menu.isStarted
        stopButton.active = menu.isStarted
    }
}
