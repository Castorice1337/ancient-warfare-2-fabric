package com.columbina.content.research

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.Container
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

sealed interface ResearchRequirement {
    val count: Int

    fun countMatching(container: Container): Int

    fun tryConsume(container: Container): Boolean {
        var remaining = count

        for (slot in 0 until container.containerSize) {
            val stack = container.getItem(slot)
            val matches = countInStack(stack).coerceAtMost(remaining)

            if (matches <= 0) {
                continue
            }

            container.removeItem(slot, matches)
            remaining -= matches

            if (remaining <= 0) {
                container.setChanged()
                return true
            }
        }

        return false
    }

    fun canSatisfy(container: Container): Boolean = countMatching(container) >= count

    fun countInStack(stack: ItemStack): Int
}

data class ItemCountRequirement(
    val itemId: Identifier,
    override val count: Int,
) : ResearchRequirement {
    private val resolvedItemId: Identifier = LegacyItemIdCompat.resolve(itemId)

    override fun countMatching(container: Container): Int {
        var total = 0

        for (slot in 0 until container.containerSize) {
            total += countInStack(container.getItem(slot))
        }

        return total
    }

    override fun countInStack(stack: ItemStack): Int {
        if (stack.isEmpty) {
            return 0
        }

        val key = BuiltInRegistries.ITEM.getKey(stack.item)
        return if (key == resolvedItemId) stack.count else 0
    }
}

data class OreDictCountRequirement(
    val oreName: String,
    override val count: Int,
) : ResearchRequirement {
    override fun countMatching(container: Container): Int {
        var total = 0

        for (slot in 0 until container.containerSize) {
            total += countInStack(container.getItem(slot))
        }

        return total
    }

    override fun countInStack(stack: ItemStack): Int {
        if (stack.isEmpty) {
            return 0
        }

        return if (LegacyOreDictionaryCompat.matches(stack.item, oreName)) stack.count else 0
    }
}

object LegacyOreDictionaryCompat {
    fun matches(item: Item, oreName: String): Boolean {
        val path = BuiltInRegistries.ITEM.getKey(item).path

        return when (oreName) {
            "bearingIron" -> path.contains("iron_bearing")
            "bearingSteel" -> path.contains("steel_bearing")
            "cropWheat" -> item == Items.WHEAT
            "dustRedstone" -> item == Items.REDSTONE
            "dyeBlack" -> item == Items.BLACK_DYE
            "dyeBlue" -> item == Items.BLUE_DYE
            "dyeGray" -> item == Items.GRAY_DYE
            "dyeGreen" -> item == Items.GREEN_DYE
            "dyeLightGray" -> item == Items.LIGHT_GRAY_DYE
            "dyeRed" -> item == Items.RED_DYE
            "gearIron" -> path.contains("iron_gear")
            "gearSteel" -> path.contains("steel_gear")
            "gearWood" -> path.contains("wooden_gear")
            "gemDiamond" -> item == Items.DIAMOND
            "gemEmerald" -> item == Items.EMERALD
            "gunpowder" -> item == Items.GUNPOWDER
            "ingotGold" -> item == Items.GOLD_INGOT
            "ingotIron" -> item == Items.IRON_INGOT
            "ingotSteel" -> path.contains("steel_ingot")
            "chest" -> item == Items.CHEST
            "enderpearl" -> item == Items.ENDER_PEARL
            "feather" -> item == Items.FEATHER
            "fenceWood" -> path.endsWith("_fence")
            "leather" -> item == Items.LEATHER
            "netherrack" -> item == Items.NETHERRACK.asItem()
            "obsidian" -> item == Items.OBSIDIAN
            "paneGlass" -> item == Items.GLASS_PANE
            "paper" -> item == Items.PAPER
            "plankWood" -> path.endsWith("_planks")
            "sand" -> item == Items.SAND
            "shaftSteel" -> path.contains("steel_shaft")
            "shaftIron" -> path.contains("iron_shaft")
            "shaftWood" -> path.contains("wooden_shaft")
            "stickWood" -> item == Items.STICK
            "stone" -> item == Items.STONE.asItem()
            "string" -> item == Items.STRING
            "torch" -> item == Items.TORCH
            "workbench" -> item == Items.CRAFTING_TABLE
            else -> false
        }
    }
}

object LegacyItemIdCompat {
    private val aliases = mapOf(
        Identifier.parse("minecraft:fish") to Identifier.parse("minecraft:cod"),
        Identifier.parse("minecraft:noteblock") to Identifier.parse("minecraft:note_block"),
        Identifier.parse("minecraft:wool") to Identifier.parse("minecraft:white_wool"),
    )

    fun resolve(id: Identifier): Identifier = aliases[id] ?: id
}
