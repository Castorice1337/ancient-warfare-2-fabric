package com.columbina.client.screen

import com.columbina.content.logistics.screen.RoutingOrderScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class RoutingOrderScreen(
    menu: RoutingOrderScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<RoutingOrderScreenHandler>(menu, inventory, title) {
    companion object {
        private const val VISIBLE_POINTS = 4
        private const val POINT_STRIDE = 16
    }

    private val background: Identifier = ColumbinaIds.legacyId("textures/gui/guibackgroundlarge.png")
    private val pointButtons = mutableListOf<List<Button>>()

    init {
        imageWidth = 320
        imageHeight = 240
        inventoryLabelY = 1000
        titleLabelX = 8
        titleLabelY = 8
    }

    override fun init() {
        super.init()
        pointButtons.clear()

        repeat(VISIBLE_POINTS) { index ->
            val baseY = topPos + 42 + (index * 38)
            val controls = listOf(
                addActionButton(index, 1, leftPos + 8, baseY, "Side"),
                addActionButton(index, 2, leftPos + 54, baseY, "Type"),
                addActionButton(index, 3, leftPos + 128, baseY, "Dmg"),
                addActionButton(index, 4, leftPos + 166, baseY, "Tag"),
                addActionButton(index, 5, leftPos + 204, baseY, "X"),
                addActionButton(index, 6, leftPos + 224, baseY, "^"),
                addActionButton(index, 7, leftPos + 244, baseY, "v"),
                addActionButton(index, 8, leftPos + 264, baseY, "F1"),
                addActionButton(index, 9, leftPos + 264, baseY + 18, "F2"),
                addActionButton(index, 10, leftPos + 264, baseY + 36, "F3"),
            )
            pointButtons += controls
        }
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
        val order = menu.routingOrder()
        guiGraphics.drawString(font, title, 8, 8, 0x404040, false)
        guiGraphics.drawString(font, Component.literal("Use order item on blocks to add route points"), 8, 22, 0x404040, false)
        guiGraphics.drawString(font, Component.literal("Selected hotbar item sets F1/F2/F3"), 8, 32, 0x404040, false)

        order.getEntries().take(VISIBLE_POINTS).forEachIndexed { index, point ->
            val y = 46 + (index * 38)
            guiGraphics.drawString(font, Component.literal("${index + 1}. ${point.target.x}, ${point.target.y}, ${point.target.z}"), 8, y, 0x404040, false)
            guiGraphics.drawString(font, Component.literal("RouteType: ${point.routeType.name}"), 54, y + 12, 0x404040, false)
            val filters = point.filters.take(3).mapNotNull { if (it.itemId.isBlank()) null else "${it.itemId}:${it.count}" }
            guiGraphics.drawString(font, Component.literal("Filters: ${filters.joinToString(" | ").ifBlank { "none" }}"), 8, y + 24, 0x404040, false)
        }
    }

    private fun refreshButtons() {
        val entries = menu.routingOrder().getEntries()
        pointButtons.forEachIndexed { index, buttons ->
            val point = entries.getOrNull(index)
            buttons.forEach { it.active = point != null }
            if (point != null) {
                buttons[0].message = Component.literal(point.blockSide.name.lowercase())
                buttons[1].message = Component.literal(point.routeType.name.lowercase())
                buttons[2].message = Component.literal(if (point.ignoreDamage) "dmg off" else "dmg on")
                buttons[3].message = Component.literal(if (point.ignoreTag) "tag off" else "tag on")
            }
        }
    }

    private fun addActionButton(pointIndex: Int, action: Int, x: Int, y: Int, text: String): Button {
        return addRenderableWidget(
            Button.builder(Component.literal(text)) {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, pointIndex * POINT_STRIDE + action)
            }.bounds(x, y, if (text.length > 2) 34 else 18, 16).build(),
        )
    }
}
