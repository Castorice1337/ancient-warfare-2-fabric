package com.columbina.content.logistics.courier

import net.minecraft.core.Direction
import net.minecraft.world.Container
import net.minecraft.world.WorldlyContainer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

class DirectionalContainerView(
    private val target: WorldlyContainer,
    private val side: Direction,
) : Container {
    private val exposedSlots: IntArray = target.getSlotsForFace(side)

    override fun getContainerSize(): Int = exposedSlots.size

    override fun isEmpty(): Boolean = exposedSlots.none { !target.getItem(it).isEmpty }

    override fun getItem(slot: Int): ItemStack {
        val realSlot = exposedSlots.getOrNull(slot) ?: return ItemStack.EMPTY
        return target.getItem(realSlot)
    }

    override fun removeItem(slot: Int, amount: Int): ItemStack {
        val realSlot = exposedSlots.getOrNull(slot) ?: return ItemStack.EMPTY
        val stack = target.getItem(realSlot)
        if (!target.canTakeItemThroughFace(realSlot, stack, side)) {
            return ItemStack.EMPTY
        }
        return target.removeItem(realSlot, amount)
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        val realSlot = exposedSlots.getOrNull(slot) ?: return ItemStack.EMPTY
        val stack = target.getItem(realSlot)
        if (!target.canTakeItemThroughFace(realSlot, stack, side)) {
            return ItemStack.EMPTY
        }
        return target.removeItemNoUpdate(realSlot)
    }

    override fun setItem(slot: Int, stack: ItemStack) {
        val realSlot = exposedSlots.getOrNull(slot) ?: return
        if (!target.canPlaceItemThroughFace(realSlot, stack, side)) {
            return
        }
        target.setItem(realSlot, stack)
    }

    override fun setChanged() {
        target.setChanged()
    }

    override fun stillValid(player: Player): Boolean = target.stillValid(player)

    override fun clearContent() {
        exposedSlots.forEach { slot ->
            if (target.canTakeItemThroughFace(slot, target.getItem(slot), side)) {
                target.setItem(slot, ItemStack.EMPTY)
            }
        }
        target.setChanged()
    }

    override fun canPlaceItem(slot: Int, stack: ItemStack): Boolean {
        val realSlot = exposedSlots.getOrNull(slot) ?: return false
        return target.canPlaceItemThroughFace(realSlot, stack, side)
    }

    override fun getMaxStackSize(): Int = target.maxStackSize
}
