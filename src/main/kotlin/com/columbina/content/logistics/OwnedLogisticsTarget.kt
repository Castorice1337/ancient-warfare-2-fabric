package com.columbina.content.logistics

import net.minecraft.world.entity.player.Player

interface OwnedLogisticsTarget {
    var ownerName: String?
    var ownerUuid: String?

    fun setOwner(player: Player) {
        ownerName = player.name.string
        ownerUuid = player.uuid.toString()
    }
}
