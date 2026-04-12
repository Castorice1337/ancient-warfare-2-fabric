package com.columbina.content.structure.template

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block

data class ImportedStructureRule(
    val number: Int,
    val plugin: String,
    val rawData: String,
    val blockName: String? = null,
) {
    val blockId: Identifier? = blockName?.let(Identifier::parse)

    fun resolveBlock(): Block? {
        val id = blockId ?: return null
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(null)
    }
}

data class ImportedTemplateBuildResource(
    val itemId: String,
    val count: Int,
)

data class ImportedStructureTemplate(
    val name: String,
    val version: String,
    val size: BlockPos,
    val offset: BlockPos,
    val modDependencies: Set<String>,
    val isSurvival: Boolean,
    val isWorldGenEnabled: Boolean,
    val blockRules: Map<Int, ImportedStructureRule>,
    val templateData: IntArray,
    val sourcePath: String,
) {
    fun getRuleAt(x: Int, y: Int, z: Int): ImportedStructureRule? {
        if (x !in 0 until size.x || y !in 0 until size.y || z !in 0 until size.z) {
            return null
        }
        val index = getIndex(x, y, z)
        val ruleNumber = templateData.getOrNull(index) ?: return null
        return blockRules[ruleNumber]
    }

    fun getBoundingOrigin(clickedPos: BlockPos, face: Direction): BlockPos {
        return clickedPos
            .relative(face.counterClockWise, offset.x)
            .relative(face, offset.z)
            .offset(0, -offset.y, 0)
    }

    fun getResourceList(): List<ImportedTemplateBuildResource> {
        val resources = linkedMapOf<String, Int>()

        for (ruleNumber in templateData) {
            val rule = blockRules[ruleNumber] ?: continue
            val blockId = rule.blockId ?: continue
            if (blockId.namespace == "minecraft" && blockId.path == "air") {
                continue
            }

            val block = rule.resolveBlock() ?: continue
            val item = block.asItem()
            if (item == net.minecraft.world.item.Items.AIR) {
                continue
            }

            val itemId = BuiltInRegistries.ITEM.getKey(item).toString()
            resources[itemId] = (resources[itemId] ?: 0) + 1
        }

        return resources.entries.map { ImportedTemplateBuildResource(it.key, it.value) }
    }

    fun getPlacedBlock(x: Int, y: Int, z: Int): Block? = getRuleAt(x, y, z)?.resolveBlock()

    fun getIndex(x: Int, y: Int, z: Int): Int = (y * size.x * size.z) + (z * size.x) + x
}
