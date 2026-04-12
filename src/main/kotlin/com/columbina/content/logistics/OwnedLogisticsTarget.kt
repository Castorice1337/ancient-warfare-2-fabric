package com.columbina.content.logistics

import net.minecraft.world.entity.player.Player

interface OwnedLogisticsTarget {
    var ownerName: String?
    var ownerUuid: String?

    fun setOwner(player: Player) {
        ownerName = player.name.string
        ownerUuid = player.uuid.toString()
    }

    fun canUse(player: Player): Boolean {
        if (ownerUuid.isNullOrBlank() && ownerName.isNullOrBlank()) {
            return true
        }
        return player.uuid.toString() == ownerUuid || player.name.string == ownerName
    }
}
