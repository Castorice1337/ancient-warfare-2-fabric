package com.columbina.runtime.init

import com.columbina.content.logistics.warehouse.WarehouseControllerBlockEntity
import com.columbina.content.logistics.warehouse.WarehouseInterfaceBlockEntity
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
    }
}
