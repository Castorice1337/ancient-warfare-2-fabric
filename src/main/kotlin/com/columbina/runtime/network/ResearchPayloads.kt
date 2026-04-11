package com.columbina.runtime.network

import com.columbina.runtime.ColumbinaIds
import com.columbina.runtime.research.ResearchEntrySnapshot
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload

data class ResearchInitPayload(
    val playerKey: String,
    val snapshot: ResearchEntrySnapshot,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<ResearchInitPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ResearchInitPayload>(ColumbinaIds.columbinaId("research_init"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResearchInitPayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeUtf(payload.playerKey, 64)
                ResearchEntrySnapshot.STREAM_CODEC.encode(buffer, payload.snapshot)
            },
            { buffer ->
                ResearchInitPayload(
                    playerKey = buffer.readUtf(64),
                    snapshot = ResearchEntrySnapshot.STREAM_CODEC.decode(buffer),
                )
            },
        )
    }
}

data class ResearchStartPayload(
    val playerKey: String,
    val goal: String,
    val started: Boolean,
    val snapshot: ResearchEntrySnapshot,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<ResearchStartPayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ResearchStartPayload>(ColumbinaIds.columbinaId("research_start"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResearchStartPayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeUtf(payload.playerKey, 64)
                buffer.writeUtf(payload.goal, 256)
                buffer.writeBoolean(payload.started)
                ResearchEntrySnapshot.STREAM_CODEC.encode(buffer, payload.snapshot)
            },
            { buffer ->
                ResearchStartPayload(
                    playerKey = buffer.readUtf(64),
                    goal = buffer.readUtf(256),
                    started = buffer.readBoolean(),
                    snapshot = ResearchEntrySnapshot.STREAM_CODEC.decode(buffer),
                )
            },
        )
    }
}

data class ResearchUpdatePayload(
    val playerKey: String,
    val goal: String,
    val add: Boolean,
    val live: Boolean,
    val snapshot: ResearchEntrySnapshot,
) : CustomPacketPayload {
    override fun type(): CustomPacketPayload.Type<ResearchUpdatePayload> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<ResearchUpdatePayload>(ColumbinaIds.columbinaId("research_update"))

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResearchUpdatePayload> = StreamCodec.of(
            { buffer, payload ->
                buffer.writeUtf(payload.playerKey, 64)
                buffer.writeUtf(payload.goal, 256)
                buffer.writeBoolean(payload.add)
                buffer.writeBoolean(payload.live)
                ResearchEntrySnapshot.STREAM_CODEC.encode(buffer, payload.snapshot)
            },
            { buffer ->
                ResearchUpdatePayload(
                    playerKey = buffer.readUtf(64),
                    goal = buffer.readUtf(256),
                    add = buffer.readBoolean(),
                    live = buffer.readBoolean(),
                    snapshot = ResearchEntrySnapshot.STREAM_CODEC.decode(buffer),
                )
            },
        )
    }
}
