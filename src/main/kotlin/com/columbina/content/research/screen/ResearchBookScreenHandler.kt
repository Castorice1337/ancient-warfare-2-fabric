package com.columbina.content.research.screen

import com.columbina.runtime.init.ColumbinaScreenHandlers
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

class ResearchBookScreenHandler(
    syncId: Int,
    playerInventory: Inventory,
    val researcherName: String,
) : AbstractContainerMenu(ColumbinaScreenHandlers.RESEARCH_BOOK, syncId) {
    val playerKey: String = playerInventory.player.name.string

    override fun quickMoveStack(player: Player, i: Int): ItemStack = ItemStack.EMPTY

    override fun stillValid(player: Player): Boolean = true
}
