package com.columbina.client.screen

import com.columbina.content.logistics.screen.WarehouseInterfaceScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class WarehouseInterfaceScreen(
    menu: WarehouseInterfaceScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<WarehouseInterfaceScreenHandler>(menu, inventory, title) {
    companion object {
        private const val FILTER_STRIDE = 8
        private const val VISIBLE_FILTERS = 6
    }

    private val background: Identifier = ColumbinaIds.legacyId("textures/gui/guibackgroundlarge.png")
    private val rows = mutableListOf<List<Button>>()
    private lateinit var addFilterButton: Button

    init {
        imageWidth = 178
        imageHeight = 240
        inventoryLabelY = 146
        titleLabelX = 8
        titleLabelY = 8
    }

    override fun init() {
        super.init()
        rows.clear()

        repeat(VISIBLE_FILTERS) { index ->
            val y = topPos + 20 + (index * 16)
            val row = listOf(
                addButton(index, 1, leftPos + 8, y, "Set", 28),
                addButton(index, 2, leftPos + 40, y, "Clr", 28),
                addButton(index, 3, leftPos + 72, y, "+", 16),
                addButton(index, 4, leftPos + 90, y, "-", 16),
                addButton(index, 5, leftPos + 108, y, "++", 24),
                addButton(index, 6, leftPos + 134, y, "--", 24),
            )
            rows += row
        }

        addFilterButton = addRenderableWidget(
            Button.builder(Component.literal("Add Filter")) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, WarehouseInterfaceScreenHandler.ACTION_ADD_FILTER)
            }.bounds(leftPos + 8, topPos + 118, 90, 16).build(),
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
        guiGraphics.drawString(font, Component.literal("Selected held item sets filter"), 8, 106, 0x404040, false)

        menu.filters().take(VISIBLE_FILTERS).forEachIndexed { index, filter ->
            val y = 24 + (index * 16)
            val label = if (filter.itemId.isNullOrBlank()) "<empty>" else "${filter.itemId} x${filter.filterQuantity}"
            guiGraphics.drawString(font, Component.literal(label), 8, y, 0x404040, false)
        }
    }

    private fun refreshButtons() {
        val filters = menu.filters()
        rows.forEachIndexed { index, buttons ->
            val active = index < filters.size
            buttons.forEach { it.active = active }
        }
        addFilterButton.active = filters.size < 9
    }

    private fun addButton(index: Int, action: Int, x: Int, y: Int, text: String, width: Int): Button {
        return addRenderableWidget(
            Button.builder(Component.literal(text)) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, index * FILTER_STRIDE + action)
            }.bounds(x, y, width, 14).build(),
        )
    }
}
