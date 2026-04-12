package com.columbina.runtime.init

import com.columbina.content.structure.blockentity.DraftingStationBlockEntity
import com.columbina.content.structure.blockentity.StructureBuilderBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseControllerBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseInterfaceBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseStockLinkerBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseStockViewerBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseStorageBlockEntity
import com.columbina.content.research.blockentity.ResearchStationBlockEntity
import com.columbina.runtime.ColumbinaIds
import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.entity.BlockEntityType

object ColumbinaBlockEntities {
    lateinit var RESEARCH_STATION: BlockEntityType<ResearchStationBlockEntity>
        private set
    lateinit var WAREHOUSE_CONTROL: BlockEntityType<WarehouseControllerBlockEntity>
        private set
    lateinit var WAREHOUSE_STORAGE: BlockEntityType<WarehouseStorageBlockEntity>
        private set
    lateinit var WAREHOUSE_INTERFACE: BlockEntityType<WarehouseInterfaceBlockEntity>
        private set
    lateinit var WAREHOUSE_STOCK_VIEWER: BlockEntityType<WarehouseStockViewerBlockEntity>
        private set
    lateinit var WAREHOUSE_STOCK_LINKER: BlockEntityType<WarehouseStockLinkerBlockEntity>
        private set
    lateinit var DRAFTING_STATION: BlockEntityType<DraftingStationBlockEntity>
        private set
    lateinit var STRUCTURE_BUILDER: BlockEntityType<StructureBuilderBlockEntity>
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_STATION = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("research_station"),
            FabricBlockEntityTypeBuilder.create(::ResearchStationBlockEntity, ColumbinaBlocks.RESEARCH_STATION).build(),
        )

        WAREHOUSE_CONTROL = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("warehouse_control"),
            FabricBlockEntityTypeBuilder.create(::WarehouseControllerBlockEntity, ColumbinaBlocks.WAREHOUSE_CONTROL).build(),
        )
        WAREHOUSE_STORAGE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("warehouse_storage"),
            FabricBlockEntityTypeBuilder.create(
                WarehouseStorageBlockEntity::fromState,
                ColumbinaBlocks.WAREHOUSE_STORAGE_SMALL,
                ColumbinaBlocks.WAREHOUSE_STORAGE_MEDIUM,
                ColumbinaBlocks.WAREHOUSE_STORAGE_LARGE,
            ).build(),
        )
        WAREHOUSE_INTERFACE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("warehouse_interface"),
            FabricBlockEntityTypeBuilder.create(::WarehouseInterfaceBlockEntity, ColumbinaBlocks.WAREHOUSE_INTERFACE).build(),
        )
        WAREHOUSE_STOCK_VIEWER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("warehouse_stock_viewer"),
            FabricBlockEntityTypeBuilder.create(::WarehouseStockViewerBlockEntity, ColumbinaBlocks.WAREHOUSE_STOCK_VIEWER).build(),
        )
        WAREHOUSE_STOCK_LINKER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("warehouse_stock_linker"),
            FabricBlockEntityTypeBuilder.create(::WarehouseStockLinkerBlockEntity, ColumbinaBlocks.WAREHOUSE_STOCK_LINKER).build(),
        )
        DRAFTING_STATION = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("drafting_station"),
            FabricBlockEntityTypeBuilder.create(::DraftingStationBlockEntity, ColumbinaBlocks.DRAFTING_STATION).build(),
        )
        STRUCTURE_BUILDER = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            ColumbinaIds.legacyId("structure_builder_ticked"),
            FabricBlockEntityTypeBuilder.create(::StructureBuilderBlockEntity, ColumbinaBlocks.STRUCTURE_BUILDER).build(),
        )
    }
}
