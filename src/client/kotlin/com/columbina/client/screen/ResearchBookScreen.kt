package com.columbina.client.screen

import com.columbina.client.research.ClientResearchState
import com.columbina.content.research.ImportedResearchGoal
import com.columbina.content.research.ImportedResearchRegistry
import com.columbina.content.research.ItemCountRequirement
import com.columbina.content.research.OreDictCountRequirement
import com.columbina.content.research.ResearchRequirement
import com.columbina.content.research.screen.ResearchBookScreenHandler
import com.columbina.runtime.ColumbinaIds
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.player.Inventory

class ResearchBookScreen(
    menu: ResearchBookScreenHandler,
    inventory: Inventory,
    title: Component,
) : AbstractContainerScreen<ResearchBookScreenHandler>(menu, inventory, title) {
    private enum class ViewMode {
        RESEARCH,
        ITEMS,
    }

    private data class RecipeEntry(
        val path: String,
        val requiredResearch: String?,
        val resultItemId: String?,
        val label: String,
    )

    companion object {
        private const val PAGE_SIZE = 12
    }

    private val background = ColumbinaIds.columbinaId("textures/gui/guibackgroundlarge.png")
    private val widgets1 = ColumbinaIds.columbinaId("textures/gui/guibuttons1.png")
    private val widgets2 = ColumbinaIds.columbinaId("textures/gui/guibuttons2.png")

    private var mode = ViewMode.RESEARCH
    private var page = 0
    private var selectedGoalId: String? = null
    private var selectedRecipePath: String? = null

    private lateinit var modeButton: Button
    private lateinit var previousPageButton: Button
    private lateinit var nextPageButton: Button
    private val entryButtons = mutableListOf<Button>()

    init {
        imageWidth = 400
        imageHeight = 240
        inventoryLabelY = 1000
        titleLabelX = 8
        titleLabelY = 8
    }

    override fun init() {
        super.init()
        entryButtons.clear()

        modeButton = addRenderableWidget(
            Button.builder(Component.empty()) {
                mode = if (mode == ViewMode.RESEARCH) ViewMode.ITEMS else ViewMode.RESEARCH
                page = 0
                refreshButtons()
            }.bounds(leftPos + 8, topPos + 8, 100, 16).build(),
        )
        previousPageButton = addRenderableWidget(
            Button.builder(Component.literal("<")) {
                if (page > 0) {
                    page--
                    refreshButtons()
                }
            }.bounds(leftPos + 114, topPos + 8, 20, 16).build(),
        )
        nextPageButton = addRenderableWidget(
            Button.builder(Component.literal(">")) {
                if (page < maxPage()) {
                    page++
                    refreshButtons()
                }
            }.bounds(leftPos + 138, topPos + 8, 20, 16).build(),
        )

        repeat(PAGE_SIZE) { index ->
            val button = addRenderableWidget(
                Button.builder(Component.empty()) {
                    selectVisibleEntry(index)
                }.bounds(leftPos + 8, topPos + 36 + (index * 16), 172, 14).build(),
            )
            entryButtons += button
        }

        refreshButtons()
    }

    override fun containerTick() {
        super.containerTick()
        refreshButtons()
    }

    override fun renderBg(guiGraphics: GuiGraphics, partialTick: Float, mouseX: Int, mouseY: Int) {
        guiGraphics.fill(0, 0, width, height, 0x60000000)
        blitQuartered(guiGraphics, background, 0, 0, 256, 240, leftPos, topPos, imageWidth, imageHeight)
        blitQuartered(guiGraphics, widgets2, 0, 0, 256, 40, leftPos + 186, topPos + 36, 206, 22)
    }

    override fun renderLabels(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int) {
        val snapshot = ClientResearchState.snapshotFor(menu.researcherName)
        val currentResearch = snapshot?.currentResearch ?: Component.translatable("guistrings.research.no_research").string

        guiGraphics.drawString(font, title, 170, 12, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.researcher_name").append(": ${menu.researcherName}"), 170, 28, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.current_goal").append(": $currentResearch"), 186, 42, 0x404040, false)
        guiGraphics.drawString(font, Component.translatable("guistrings.research.progress").append(": ${snapshot?.currentProgress ?: 0}"), 186, 54, 0x404040, false)

        if (mode == ViewMode.RESEARCH) {
            renderResearchDetails(guiGraphics)
        } else {
            renderRecipeDetails(guiGraphics)
        }
    }

    private fun renderResearchDetails(guiGraphics: GuiGraphics) {
        val goal = selectedGoal() ?: return
        val snapshot = ClientResearchState.snapshotFor(menu.researcherName)
        val x = 190
        var y = 74

        guiGraphics.drawString(font, Component.translatable(goal.translationKey), x, y, 0x404040, false)
        y += 12
        val statusKey = if (goal.id in (snapshot?.completedResearch ?: emptyList())) {
            "guistrings.research.known_research"
        } else {
            "guistrings.research.unknown_research"
        }
        guiGraphics.drawString(font, Component.translatable(statusKey), x, y, 0x404040, false)
        y += 12
        guiGraphics.drawString(font, Component.translatable("guistrings.research.research_time", goal.time), x, y, 0x404040, false)
        y += 16

        y = drawLines(
            guiGraphics,
            x,
            y,
            Component.translatable("guistrings.research.resources_needed"),
            goal.resources.map(::describeRequirement).ifEmpty { listOf(Component.translatable("guistrings.no_selection")) },
        )
        y = drawLines(
            guiGraphics,
            x,
            y,
            Component.translatable("guistrings.research.research_needed"),
            goal.dependencies.map(::goalLabel).ifEmpty { listOf(Component.translatable("guistrings.no_selection")) },
        )
        drawLines(
            guiGraphics,
            x,
            y,
            Component.translatable("guistrings.research.researched_items"),
            ImportedResearchRegistry.recipesForResearch(goal.id)
                .map(::recipeLabel)
                .ifEmpty { listOf(Component.translatable("guistrings.no_selection")) },
        )
    }

    private fun renderRecipeDetails(guiGraphics: GuiGraphics) {
        val recipe = selectedRecipe() ?: return
        val x = 190
        var y = 74

        guiGraphics.drawString(font, Component.literal(recipe.label), x, y, 0x404040, false)
        y += 12
        guiGraphics.drawString(font, Component.literal("Entry: ${recipe.path}"), x, y, 0x404040, false)
        y += 12
        guiGraphics.drawString(font, Component.literal("Result: ${recipe.resultItemId ?: recipe.label}"), x, y, 0x404040, false)
        y += 16

        val researchLine = recipe.requiredResearch?.let(::goalLabel) ?: Component.translatable("guistrings.no_selection")
        drawLines(
            guiGraphics,
            x,
            y,
            Component.translatable("guistrings.research.research_needed"),
            listOf(researchLine),
        )
    }

    private fun drawLines(
        guiGraphics: GuiGraphics,
        x: Int,
        startY: Int,
        heading: Component,
        lines: List<Component>,
    ): Int {
        var y = startY
        guiGraphics.drawString(font, heading, x, y, 0x404040, false)
        y += 12

        lines.take(6).forEach { line ->
            guiGraphics.drawString(font, line, x + 6, y, 0x404040, false)
            y += 10
        }

        return y + 6
    }

    private fun refreshButtons() {
        modeButton.message = Component.literal(if (mode == ViewMode.RESEARCH) "Research" else "Items")
        previousPageButton.active = page > 0
        nextPageButton.active = page < maxPage()

        ensureSelection()

        val labels = when (mode) {
            ViewMode.RESEARCH -> visibleGoals().map { Component.translatable(it.translationKey) }
            ViewMode.ITEMS -> visibleRecipes().map { Component.literal(it.label) }
        }

        entryButtons.forEachIndexed { index, button ->
            val label = labels.getOrNull(index)
            button.active = label != null
            button.visible = true
            button.message = label ?: Component.empty()
        }
    }

    private fun selectVisibleEntry(index: Int) {
        when (mode) {
            ViewMode.RESEARCH -> {
                selectedGoalId = visibleGoals().getOrNull(index)?.id
            }
            ViewMode.ITEMS -> {
                selectedRecipePath = visibleRecipes().getOrNull(index)?.path
            }
        }
        refreshButtons()
    }

    private fun maxPage(): Int {
        val total = when (mode) {
            ViewMode.RESEARCH -> researchGoals().size
            ViewMode.ITEMS -> recipeEntries().size
        }
        return ((total - 1).coerceAtLeast(0)) / PAGE_SIZE
    }

    private fun ensureSelection() {
        when (mode) {
            ViewMode.RESEARCH -> {
                val ids = researchGoals().map(ImportedResearchGoal::id).toSet()
                if (selectedGoalId !in ids) {
                    selectedGoalId = researchGoals().firstOrNull()?.id
                }
            }
            ViewMode.ITEMS -> {
                val ids = recipeEntries().map(RecipeEntry::path).toSet()
                if (selectedRecipePath !in ids) {
                    selectedRecipePath = recipeEntries().firstOrNull()?.path
                }
            }
        }
    }

    private fun selectedGoal(): ImportedResearchGoal? = selectedGoalId?.let(ImportedResearchRegistry::getGoal)

    private fun selectedRecipe(): RecipeEntry? = recipeEntries().firstOrNull { it.path == selectedRecipePath }

    private fun visibleGoals(): List<ImportedResearchGoal> = researchGoals().drop(page * PAGE_SIZE).take(PAGE_SIZE)

    private fun visibleRecipes(): List<RecipeEntry> = recipeEntries().drop(page * PAGE_SIZE).take(PAGE_SIZE)

    private fun researchGoals(): List<ImportedResearchGoal> {
        return ImportedResearchRegistry.allGoals().sortedBy { Component.translatable(it.translationKey).string.lowercase() }
    }

    private fun recipeEntries(): List<RecipeEntry> {
        return ImportedResearchRegistry.allRecipePayloads().keys.map { path ->
            val resultItemId = ImportedResearchRegistry.getRecipeResultItem(path)
            RecipeEntry(
                path = path,
                requiredResearch = ImportedResearchRegistry.getRecipeResearch(path),
                resultItemId = resultItemId,
                label = displayNameForItem(resultItemId) ?: path,
            )
        }.sortedBy(RecipeEntry::label)
    }

    private fun goalLabel(goalId: String): Component {
        val goal = ImportedResearchRegistry.getGoal(goalId)
        return if (goal != null) {
            Component.translatable(goal.translationKey)
        } else {
            Component.literal(goalId)
        }
    }

    private fun recipeLabel(path: String): Component {
        val resultItemId = ImportedResearchRegistry.getRecipeResultItem(path)
        return Component.literal(displayNameForItem(resultItemId) ?: path)
    }

    private fun displayNameForItem(itemId: String?): String? {
        if (itemId == null) {
            return null
        }

        return try {
            val identifier = Identifier.parse(itemId)
            identifier.path.split('/').last().split('_').joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
        } catch (_: IllegalArgumentException) {
            itemId
        }
    }

    private fun describeRequirement(requirement: ResearchRequirement): Component {
        return when (requirement) {
            is ItemCountRequirement -> Component.literal("${requirement.count}x ${requirement.itemId}")
            is OreDictCountRequirement -> Component.literal("${requirement.count}x #${requirement.oreName}")
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
