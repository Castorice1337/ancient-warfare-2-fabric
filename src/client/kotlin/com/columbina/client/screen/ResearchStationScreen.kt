package com.columbina.client.screen

import com.columbina.client.research.ClientResearchState
import com.columbina.content.research.ImportedResearchRegistry
import com.columbina.content.research.screen.ResearchStationScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class ResearchStationScreen(
    menu: ResearchStationScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<ResearchStationScreenHandler>(menu, inventory, title) {
    private val background = ColumbinaIds.columbinaId("textures/gui/guibackgroundlarge.png")
    private val widgets1 = ColumbinaIds.columbinaId("textures/gui/guibuttons1.png")
    private val widgets2 = ColumbinaIds.columbinaId("textures/gui/guibuttons2.png")
    private val queueAddButtons = mutableListOf<Button>()
    private val queueRemoveButtons = mutableListOf<Button>()
    private lateinit var adjacentButton: Button
    private lateinit var directionButton: Button
    private lateinit var sideButton: Button

    init {
        imageWidth = 178
        imageHeight = 240
        inventoryLabelY = 146
        titleLabelX = 8
        titleLabelY = 8
    }

    override fun init() {
        super.init()
        queueAddButtons.clear()
        queueRemoveButtons.clear()

        adjacentButton = addRenderableWidget(
            Button.builder(Component.literal("Adj"), {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, ResearchStationScreenHandler.BUTTON_TOGGLE_ADJACENT)
            }).bounds(leftPos + 8, topPos + 142, 42, 16).build(),
        )
        directionButton = addRenderableWidget(
            Button.builder(Component.literal("Dir"), {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, ResearchStationScreenHandler.BUTTON_CYCLE_DIRECTION)
            }).bounds(leftPos + 54, topPos + 142, 52, 16).build(),
        )
        sideButton = addRenderableWidget(
            Button.builder(Component.literal("Side"), {
                minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, ResearchStationScreenHandler.BUTTON_CYCLE_SIDE)
            }).bounds(leftPos + 110, topPos + 142, 52, 16).build(),
        )

        repeat(ResearchStationScreenHandler.VISIBLE_QUEUE_ACTIONS) { index ->
            val addButton = addRenderableWidget(
                Button.builder(Component.literal("+"), {
                    minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, ResearchStationScreenHandler.BUTTON_QUEUE_ADD_BASE + index)
                }).bounds(leftPos + 8, topPos + 92 + (index * 16), 74, 14).build(),
            )
            val removeButton = addRenderableWidget(
                Button.builder(Component.literal("-"), {
                    minecraft?.gameMode?.handleInventoryButtonClick(menu.containerId, ResearchStationScreenHandler.BUTTON_QUEUE_REMOVE_BASE + index)
                }).bounds(leftPos + 94, topPos + 92 + (index * 16), 74, 14).build(),
            )
            queueAddButtons += addButton
            queueRemoveButtons += removeButton
        }

        refreshButtons()
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.fill(0, 0, width, height, 0x60000000)
        blitQuartered(guiGraphics, background, 0, 0, 256, 240, leftPos, topPos, imageWidth, imageHeight)

        menu.slots.forEach { slot ->
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, widgets1, leftPos + slot.x - 1, topPos + slot.y - 1, 152f, 120f, 18, 18, 256, 256)
        }

        val snapshot = ClientResearchState.snapshotFor(menu.playerKey)
        val progress = ((snapshot?.currentProgress ?: menu.storedEnergy).coerceAtLeast(0) / 200.0f).coerceIn(0f, 1f)
        val fillWidth = ((imageWidth - 70 - 8 - 6) * progress).toInt()

        blitQuartered(guiGraphics, widgets2, 0, 0, 256, 40, leftPos + 70, topPos + 56, imageWidth - 70 - 8, 12)
        if (fillWidth > 0) {
            blitQuartered(guiGraphics, widgets1, 152, 234, 104, 10, leftPos + 73, topPos + 59, fillWidth, 6)
        }
    }

    override fun containerTick() {
        super.containerTick()
        refreshButtons()
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val snapshot = ClientResearchState.snapshotFor(menu.playerKey)
        val currentGoal = snapshot?.currentResearch ?: ImportedResearchRegistry.allGoals().firstOrNull()?.id ?: Component.translatable("guistrings.research.no_research").string
        val queuedResearch = snapshot?.queuedResearch ?: emptyList()
        val queuedCount = queuedResearch.size
        val researcherName = menu.playerKey
        val researchable = ImportedResearchRegistry.getResearchableGoals(
            completed = snapshot?.completedResearch?.toSet() ?: emptySet(),
            queued = queuedResearch,
            inProgress = snapshot?.currentResearch,
        ).sorted()

        guiGraphics.drawString(font, Component.literal(researcherName), 8, 8, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.current_goal").append(": "), 8, 44, 0x404040, false)
        guiGraphics.drawString(font, Component.literal(currentGoal), 80, 44, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.progress"), 8, 57, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.learnable_research"), 8, 82, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.queued_research"), 94, 82, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.queued_research").append(": $queuedCount"), 8, 76, 0x404040, false)

        researchable.take(ResearchStationScreenHandler.VISIBLE_QUEUE_ACTIONS).forEachIndexed { index, goal ->
            guiGraphics.drawString(font, Component.literal(goal), 24, 96 + (index * 16), 0x404040, false)
        }
        queuedResearch.take(ResearchStationScreenHandler.VISIBLE_QUEUE_ACTIONS).forEachIndexed { index, goal ->
            guiGraphics.drawString(font, Component.literal(goal), 110, 96 + (index * 16), 0x404040, false)
        }
    }

    private fun refreshButtons() {
        val snapshot = ClientResearchState.snapshotFor(menu.playerKey)
        val researchable = ImportedResearchRegistry.getResearchableGoals(
            completed = snapshot?.completedResearch?.toSet() ?: emptySet(),
            queued = snapshot?.queuedResearch ?: emptyList(),
            inProgress = snapshot?.currentResearch,
        ).sorted()
        val queued = snapshot?.queuedResearch ?: emptyList()

        adjacentButton.message = Component.literal(if (menu.useAdjacentInventory) "Adj: On" else "Adj: Off")
        directionButton.message = Component.literal("Dir: ${Direction.values()[menu.inventoryDirectionOrdinal].name.lowercase()}")
        sideButton.message = Component.literal("Side: ${Direction.values()[menu.inventorySideOrdinal].name.lowercase()}")

        queueAddButtons.forEachIndexed { index, button ->
            val goal = researchable.getOrNull(index)
            button.active = goal != null
            button.message = Component.literal(goal?.let { "+ $it" } ?: "+")
        }

        queueRemoveButtons.forEachIndexed { index, button ->
            val goal = queued.getOrNull(index)
            button.active = goal != null
            button.message = Component.literal(goal?.let { "- $it" } ?: "-")
        }
    }

    private fun blitQuartered(
        guiGraphics: GuiGraphics,
        texture: Identifier,
        textureU: Int,
        textureV: Int,
        textureWidthUsed: Int,
        textureHeightUsed: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
    ) {
        val halfWidth = width / 2
        val halfHeight = height / 2
        val halfTextureWidth = textureWidthUsed / 2
        val halfTextureHeight = textureHeightUsed / 2

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, textureU.toFloat(), textureV.toFloat(), halfWidth, halfHeight, 256, 256)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + halfWidth, y, (textureU + textureWidthUsed - halfTextureWidth).toFloat(), textureV.toFloat(), width - halfWidth, halfHeight, 256, 256)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y + halfHeight, textureU.toFloat(), (textureV + textureHeightUsed - halfTextureHeight).toFloat(), halfWidth, height - halfHeight, 256, 256)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + halfWidth, y + halfHeight, (textureU + textureWidthUsed - halfTextureWidth).toFloat(), (textureV + textureHeightUsed - halfTextureHeight).toFloat(), width - halfWidth, height - halfHeight, 256, 256)
    }
}
