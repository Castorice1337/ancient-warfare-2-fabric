package com.columbina.runtime.init

import com.columbina.content.logistics.entity.CourierEntity
import com.columbina.runtime.ColumbinaIds
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object ColumbinaEntities {
    lateinit var COURIER: EntityType<CourierEntity>
        private set

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) {
            return
        }

        bootstrapped = true

        val id = ColumbinaIds.legacyId("courier")
        COURIER = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            id,
            FabricEntityTypeBuilder.create(MobCategory.CREATURE, ::CourierEntity)
                .dimensions(EntityDimensions.scalable(0.6f, 1.8f))
                .build(ResourceKey.create(Registries.ENTITY_TYPE, id)),
        )

        FabricDefaultAttributeRegistry.register(COURIER, CourierEntity.createAttributes())
    }
}
