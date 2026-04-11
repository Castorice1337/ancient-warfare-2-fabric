package com.columbina.client.screen

import com.columbina.content.logistics.screen.WarehouseControlScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class WarehouseControlScreen(
    menu: WarehouseControlScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<WarehouseControlScreenHandler>(menu, inventory, title) {
    private val background: Identifier = ColumbinaIds.legacyId("textures/gui/guibackgroundlarge.png")
    private lateinit var sortTypeButton: Button
    private lateinit var sortOrderButton: Button

    init {
        imageWidth = 178
        imageHeight = 240
        inventoryLabelY = 128
        titleLabelX = 8
        titleLabelY = 6
    }

    override fun init() {
        super.init()
        sortTypeButton = addRenderableWidget(
            Button.builder(Component.empty()) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, WarehouseControlScreenHandler.BUTTON_SORT_TYPE)
            }.bounds(leftPos + 8, topPos + 118, 82, 16).build(),
        )
        sortOrderButton = addRenderableWidget(
            Button.builder(Component.empty()) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, WarehouseControlScreenHandler.BUTTON_SORT_ORDER)
            }.bounds(leftPos + 94, topPos + 118, 76, 16).build(),
        )
        refreshButtons()
    }

    override fun containerTick() {
        super.containerTick()
        menu.refreshFromBlockEntity()
        refreshButtons()
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.fill(0, 0, width, height, 0x60000000)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, background, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false)
        guiGraphics.drawString(font, Component.literal("Stored: ${menu.currentStored} / ${menu.maxStorage}"), 8, 106, 0x404040, false)
    }

    private fun refreshButtons() {
        sortTypeButton.message = Component.literal("Sort: ${menu.sortType.name.lowercase()}")
        sortOrderButton.message = Component.literal("Order: ${menu.sortOrder.name.lowercase()}")
    }
}
