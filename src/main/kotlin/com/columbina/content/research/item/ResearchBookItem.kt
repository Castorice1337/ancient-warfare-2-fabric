package com.columbina.content.research.item

import com.columbina.content.research.screen.ResearchBookScreenHandler
import com.columbina.runtime.init.ColumbinaItems
import com.columbina.runtime.research.ResearchRuntimeService
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.level.Level
import java.util.function.Consumer

class ResearchBookItem(properties: Properties) : Item(properties.stacksTo(1)) {
    companion object {
        private const val RESEARCHER_NAME = "researcherName"

        fun getResearcherName(stack: ItemStack): String? {
        if (!stack.`is`(ColumbinaItems.RESEARCH_BOOK)) {
            return null
        }

        val customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
        return customData.copyTag().getString(RESEARCHER_NAME).orElse(null)
    }
    }

    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)
        val researcherName = getResearcherName(stack)

        if (!level.isClientSide) {
            if (researcherName == null) {
                CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
                    tag.putString(RESEARCHER_NAME, player.name.string)
                }
                player.displayClientMessage(Component.translatable("guistrings.research.book_bound"), true)
            } else if (player is ServerPlayer) {
                openResearchBook(level as ServerLevel, player, researcherName)
            }
        }

        return InteractionResult.SUCCESS
    }

    private fun openResearchBook(level: ServerLevel, player: ServerPlayer, researcherName: String) {
        ResearchRuntimeService.sendInit(level, researcherName, player)
        player.openMenu(
            object : ExtendedScreenHandlerFactory<String> {
                override fun getDisplayName(): Component = Component.translatable("item.research_book.name")

                override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): ResearchBookScreenHandler {
                    return ResearchBookScreenHandler(syncId, playerInventory, researcherName)
                }

                override fun getScreenOpeningData(player: ServerPlayer): String = researcherName
            },
        )
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: TooltipDisplay,
        tooltipAdder: Consumer<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag)

        val researcherName = getResearcherName(stack)
        if (researcherName == null) {
            tooltipAdder.accept(
                Component.translatable("guistrings.research.researcher_name")
                    .append(": ")
                    .append(Component.translatable("guistrings.research.no_researcher")),
            )
            tooltipAdder.accept(Component.translatable("guistrings.research.right_click_to_bind"))
        } else {
            tooltipAdder.accept(Component.translatable("guistrings.research.researcher_name").append(": $researcherName"))
            tooltipAdder.accept(Component.translatable("guistrings.research.right_click_to_view"))
        }
    }
}
