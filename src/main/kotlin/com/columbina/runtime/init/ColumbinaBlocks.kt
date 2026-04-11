package com.columbina.runtime.init

import com.columbina.content.logistics.warehouse.WarehouseControllerBlock
import com.columbina.content.logistics.warehouse.WarehouseInterfaceBlock
import com.columbina.content.logistics.warehouse.WarehouseStorageBlock
import com.columbina.content.logistics.warehouse.WarehouseStorageTier
import com.columbina.content.research.block.ResearchStationBlock
import com.columbina.runtime.ColumbinaIds
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockBehaviour

object ColumbinaBlocks {
    lateinit var RESEARCH_STATION: ResearchStationBlock
        private set
    lateinit var WAREHOUSE_CONTROL: WarehouseControllerBlock
        private set
    lateinit var WAREHOUSE_STORAGE_SMALL: WarehouseStorageBlock
        private set
    lateinit var WAREHOUSE_STORAGE_MEDIUM: WarehouseStorageBlock
        private set
    lateinit var WAREHOUSE_STORAGE_LARGE: WarehouseStorageBlock
        private set
    lateinit var WAREHOUSE_INTERFACE: WarehouseInterfaceBlock
        private set
    lateinit var WAREHOUSE_STOCK_VIEWER: net.minecraft.world.level.block.Block
        private set
    lateinit var WAREHOUSE_STOCK_LINKER: net.minecraft.world.level.block.Block
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        RESEARCH_STATION = Registry.register(
            BuiltInRegistries.BLOCK,
            ColumbinaIds.legacyId("research_station"),
            ResearchStationBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("research_station"))),
            ),
        )

        Registry.register(
            BuiltInRegistries.ITEM,
            ColumbinaIds.legacyId("research_station"),
            BlockItem(
                RESEARCH_STATION,
                Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ColumbinaIds.legacyId("research_station"))),
            ),
        )

        WAREHOUSE_CONTROL = registerBlockWithItem(
            "warehouse_control",
            WarehouseControllerBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_control"))),
            ),
        ) as WarehouseControllerBlock
        WAREHOUSE_STORAGE_SMALL = registerBlockWithItem(
            "warehouse_storage_small",
            WarehouseStorageBlock(
                WarehouseStorageTier.SMALL,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_storage_small"))),
            ),
        ) as WarehouseStorageBlock
        WAREHOUSE_STORAGE_MEDIUM = registerBlockWithItem(
            "warehouse_storage_medium",
            WarehouseStorageBlock(
                WarehouseStorageTier.MEDIUM,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_storage_medium"))),
            ),
        ) as WarehouseStorageBlock
        WAREHOUSE_STORAGE_LARGE = registerBlockWithItem(
            "warehouse_storage_large",
            WarehouseStorageBlock(
                WarehouseStorageTier.LARGE,
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_storage_large"))),
            ),
        ) as WarehouseStorageBlock
        WAREHOUSE_INTERFACE = registerBlockWithItem(
            "warehouse_interface",
            WarehouseInterfaceBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_interface"))),
            ),
        ) as WarehouseInterfaceBlock
        WAREHOUSE_STOCK_VIEWER = registerBlockWithItem(
            "warehouse_stock_viewer",
            net.minecraft.world.level.block.Block(
                BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_stock_viewer"))),
            ),
        )
        WAREHOUSE_STOCK_LINKER = registerBlockWithItem(
            "warehouse_stock_linker",
            net.minecraft.world.level.block.Block(
                BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK)
                    .setId(ResourceKey.create(Registries.BLOCK, ColumbinaIds.legacyId("warehouse_stock_linker"))),
            ),
        )
    }

    private fun registerBlockWithItem(path: String, block: net.minecraft.world.level.block.Block): net.minecraft.world.level.block.Block {
        val id = ColumbinaIds.legacyId(path)
        val registeredBlock = Registry.register(BuiltInRegistries.BLOCK, id, block)
        Registry.register(
            BuiltInRegistries.ITEM,
            id,
            BlockItem(
                registeredBlock,
                Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)),
            ),
        )
        return registeredBlock
    }
}
