package com.columbina.runtime

import net.minecraft.resources.Identifier

object ColumbinaIds {
    const val MOD_ID = "columbina"
    const val LEGACY_NAMESPACE = "ancientwarfare"

    fun columbinaId(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

    fun legacyId(path: String): Identifier = Identifier.fromNamespaceAndPath(LEGACY_NAMESPACE, path)
}
