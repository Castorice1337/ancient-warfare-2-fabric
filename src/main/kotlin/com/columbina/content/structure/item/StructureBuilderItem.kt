package com.columbina.content.structure.item

import com.columbina.content.structure.block.StructureBuilderBlock
import com.columbina.content.structure.blockentity.StructureBuilderBlockEntity
import com.columbina.content.structure.build.StructureBuildBounds
import com.columbina.content.structure.build.StructureBuilderTickedRuntime
import com.columbina.content.structure.template.ImportedTemplateRegistry
import com.columbina.runtime.init.ColumbinaBlocks
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.component.TooltipDisplay
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Consumer

class StructureBuilderItem(properties: Properties) : Item(properties.stacksTo(1)) {
    companion object {
        private const val STRUCTURE_NAME = "structureName"
        private const val PROGRESS = "progress"

        fun createStack(structureName: String, progress: String? = null): ItemStack {
            val stack = ItemStack(com.columbina.runtime.init.ColumbinaItems.STRUCTURE_BUILDER)
            setStructureName(stack, structureName)
            if (progress != null) {
                setProgress(stack, progress)
            }
            return stack
        }

        fun getStructureName(stack: ItemStack): String? {
            val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            return data.getString(STRUCTURE_NAME).orElse(null)
        }

        fun setStructureName(stack: ItemStack, structureName: String) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
                tag.putString(STRUCTURE_NAME, structureName)
            }
        }

        fun getProgress(stack: ItemStack): String? {
            val data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag()
            return data.getString(PROGRESS).orElse(null)
        }

        fun setProgress(stack: ItemStack, progress: String) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack) { tag ->
                tag.putString(PROGRESS, progress)
            }
        }
    }

    override fun useOn(context: UseOnContext): InteractionResult {
        val level = context.level
        val player = context.player ?: return InteractionResult.PASS
        val stack = context.itemInHand
        val structureName = getStructureName(stack) ?: return InteractionResult.FAIL
        val template = ImportedTemplateRegistry.getTemplate(structureName) ?: return InteractionResult.FAIL

        val placePos = placementPos(context)
        val face = player.direction
        val placed = level.setBlock(placePos, structureBuilderState(face), net.minecraft.world.level.block.Block.UPDATE_ALL)
        if (!placed) {
            return InteractionResult.FAIL
        }

        val blockEntity = level.getBlockEntity(placePos) as? StructureBuilderBlockEntity ?: return InteractionResult.FAIL
        setupStructureBuilder(level, placePos, blockEntity, structureName, face)
        getProgress(stack)?.let(blockEntity::deserializeProgressData)
        blockEntity.setOwner(player)

        if (!player.abilities.instabuild) {
            stack.shrink(1)
        }
        player.displayClientMessage(Component.literal("Placed builder for $structureName"), true)
        return InteractionResult.SUCCESS
    }

    fun setupStructureBuilder(
        level: net.minecraft.world.level.Level,
        pos: BlockPos,
        builderEntity: StructureBuilderBlockEntity,
        structureName: String,
        face: Direction,
    ) {
        val template = ImportedTemplateRegistry.getTemplate(structureName) ?: return
        val buildOrigin = pos.relative(face, template.size.z - 1 - template.offset.z + 1)
        builderEntity.setBuilder(
            StructureBuilderTickedRuntime(level, template, face, buildOrigin),
            structureName,
        )
    }

    fun previewBounds(targetPos: BlockPos, face: Direction, structureName: String): StructureBuildBounds? {
        val template = ImportedTemplateRegistry.getTemplate(structureName) ?: return null
        val buildOrigin = targetPos.relative(face, template.size.z - 1 - template.offset.z + 1)
        return StructureBuildBounds.fromPlacement(buildOrigin, face, template.size, template.offset)
    }

    fun preview(targetPos: BlockPos, face: Direction, structureName: String): String? {
        val bounds = previewBounds(targetPos, face, structureName) ?: return null
        return "${bounds.min} -> ${bounds.max}"
    }

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltipDisplay: TooltipDisplay,
        tooltipAdder: Consumer<Component>,
        tooltipFlag: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, tooltipFlag)
        val structureName = getStructureName(stack)
        tooltipAdder.accept(Component.literal("Structure: ${structureName ?: "none"}"))
        if (structureName != null) {
            val template = ImportedTemplateRegistry.getTemplate(structureName)
            if (template != null) {
                tooltipAdder.accept(Component.literal("Preview: ${template.size.x}x${template.size.y}x${template.size.z}"))
            }
        }
    }

    private fun placementPos(context: UseOnContext): BlockPos {
        val current = context.clickedPos
        val replaceable = context.level.getBlockState(current).canBeReplaced()
        return if (replaceable) current else current.relative(context.clickedFace)
    }

    private fun structureBuilderState(face: Direction): BlockState {
        return ColumbinaBlocks.STRUCTURE_BUILDER.defaultBlockState().setValue(StructureBuilderBlock.FACING, face)
    }
}
