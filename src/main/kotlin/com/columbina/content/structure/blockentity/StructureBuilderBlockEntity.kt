package com.columbina.content.structure.blockentity

import com.columbina.content.structure.build.StructureBuilderTickedRuntime
import com.columbina.content.structure.item.StructureBuilderItem
import com.columbina.content.structure.template.ImportedTemplateRegistry
import com.columbina.runtime.init.ColumbinaBlockEntities
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.HolderLookup
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class StructureBuilderBlockEntity(
    pos: BlockPos,
    state: BlockState,
) : BlockEntity(ColumbinaBlockEntities.STRUCTURE_BUILDER, pos, state) {
    private var ownerName: String = ""
    private var structureName: String? = null
    private var isStarted: Boolean = false
    private var shouldRemove: Boolean = false
    private var workDelay: Int = 20
    var storedEnergy: Double = 0.0
        private set

    private var builder: StructureBuilderTickedRuntime? = null

    fun tick() {
        val currentLevel = level ?: return
        if (currentLevel.isClientSide) {
            return
        }

        val currentBuilder = builder
        if (shouldRemove || currentBuilder == null || currentBuilder.invalid || currentBuilder.isFinished()) {
            shouldRemove = true
            currentLevel.setBlock(blockPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)
            return
        }

        isStarted = true
        if (storedEnergy > 0.0) {
            storedEnergy = (storedEnergy - 1.0).coerceAtLeast(0.0)
            currentBuilder.tick()
        } else {
            if (workDelay-- <= 0) {
                currentBuilder.tick()
                workDelay = 20
            }
        }

        setChanged()
    }

    fun setOwner(player: Player) {
        ownerName = player.name.string
        setChanged()
    }

    fun setBuilder(builder: StructureBuilderTickedRuntime, structureName: String) {
        this.builder = builder
        this.structureName = structureName
        this.isStarted = false
        this.shouldRemove = false
        setChanged()
    }

    fun deserializeProgressData(progress: String) {
        builder?.deserializeProgressData(progress)
        setChanged()
    }

    fun createDropStack(): net.minecraft.world.item.ItemStack? {
        val currentStructure = structureName ?: return null
        val progress = builder?.serializeProgressData()
        return StructureBuilderItem.createStack(currentStructure, progress)
    }

    fun statusMessage(): Component {
        val currentBuilder = builder
        if (currentBuilder == null) {
            return Component.translatable("guistrings.structure.no_selection")
        }

        return if (currentBuilder.hasClearedArea) {
            val pass = currentBuilder.getPass() + 1
            val maxPasses = currentBuilder.getMaxPasses()
            val percent = String.format("%.2f%%", currentBuilder.getPercentDoneWithPass() * 100f)
            Component.translatable("guistrings.structure.builder.state", percent, pass, maxPasses)
        } else {
            val percent = String.format("%.2f%%", currentBuilder.getPercentDoneClearing() * 100f)
            Component.translatable("guistrings.structure.builder.clear_state", percent)
        }
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)
        ownerName = input.getStringOr("ownerName", "")
        structureName = input.getStringOr("structureName", "").ifBlank { null }
        isStarted = input.getBooleanOr("isStarted", false)
        shouldRemove = input.getBooleanOr("shouldRemove", false)
        workDelay = input.getIntOr("workDelay", 20)
        storedEnergy = input.getDoubleOr("storedEnergy", 0.0)

        val name = structureName
        if (name != null) {
            val template = ImportedTemplateRegistry.getTemplate(name)
            val face = Direction.values()[input.getIntOr("buildFace", Direction.NORTH.ordinal).coerceIn(0, Direction.values().lastIndex)]
            val buildOrigin = BlockPos(
                input.getIntOr("buildOriginX", blockPos.x),
                input.getIntOr("buildOriginY", blockPos.y),
                input.getIntOr("buildOriginZ", blockPos.z),
            )
            if (template != null) {
                builder = StructureBuilderTickedRuntime(level, template, face, buildOrigin)
                builder?.deserializeProgressData(input.getStringOr("progress", ""))
            }
        }
    }

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)
        output.putString("ownerName", ownerName)
        output.putString("structureName", structureName.orEmpty())
        output.putBoolean("isStarted", isStarted)
        output.putBoolean("shouldRemove", shouldRemove)
        output.putInt("workDelay", workDelay)
        output.putDouble("storedEnergy", storedEnergy)

        val currentBuilder = builder
        if (currentBuilder != null) {
            output.putInt("buildFace", currentBuilder.buildFace.ordinal)
            output.putInt("buildOriginX", currentBuilder.buildOrigin.x)
            output.putInt("buildOriginY", currentBuilder.buildOrigin.y)
            output.putInt("buildOriginZ", currentBuilder.buildOrigin.z)
            output.putString("progress", currentBuilder.serializeProgressData())
        }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener> = ClientboundBlockEntityDataPacket.create(this)

    override fun getUpdateTag(registries: HolderLookup.Provider) = saveWithoutMetadata(registries)
}
