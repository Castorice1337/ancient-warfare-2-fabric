package com.columbina.client.screen

import com.columbina.content.logistics.screen.WarehouseStockLinkerScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class WarehouseStockLinkerScreen(
    menu: WarehouseStockLinkerScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<WarehouseStockLinkerScreenHandler>(menu, inventory, title) {
    companion object {
        private const val STRIDE = 8
    }

    private val background: Identifier = ColumbinaIds.legacyId("textures/gui/guibackgroundlarge.png")
    private val rows = mutableListOf<List<Button>>()
    private lateinit var addButton: Button

    init {
        imageWidth = 250
        imageHeight = 172
        inventoryLabelY = 80
        titleLabelX = 8
        titleLabelY = 8
    }

    override fun init() {
        super.init()
        rows.clear()
        repeat(4) { index ->
            val y = topPos + 24 + index * 16
            rows += listOf(
                actionButton(index, 1, leftPos + 8, y, "Set", 28),
                actionButton(index, 2, leftPos + 40, y, "Clr", 28),
                actionButton(index, 3, leftPos + 72, y, "+", 16),
                actionButton(index, 4, leftPos + 90, y, "-", 16),
                actionButton(index, 5, leftPos + 108, y, "Eq", 24),
            )
        }
        addButton = addRenderableWidget(
            Button.builder(Component.literal("Add Filter")) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, 6)
            }.bounds(leftPos + 8, topPos + 92, 90, 16).build(),
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
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0x404040, false)
        guiGraphics.drawString(font, Component.literal("Held item sets filter"), 8, 14, 0x404040, false)
        menu.filters().forEachIndexed { index, filter ->
            guiGraphics.drawString(
                font,
                Component.literal("${filter.itemId ?: "<empty>"} ${filter.equalitySignType.name} ${filter.compareValue} (now ${filter.quantity})"),
                140,
                28 + index * 16,
                0x404040,
                false,
            )
        }
    }

    private fun refreshButtons() {
        val filters = menu.filters()
        rows.forEachIndexed { index, buttons ->
            val active = index < filters.size
            buttons.forEach { it.active = active }
        }
        addButton.active = filters.size < 4
    }

    private fun actionButton(index: Int, action: Int, x: Int, y: Int, text: String, width: Int): Button {
        return addRenderableWidget(
            Button.builder(Component.literal(text)) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, index * STRIDE + action)
            }.bounds(x, y, width, 14).build(),
        )
    }
}
