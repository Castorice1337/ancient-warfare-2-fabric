package com.columbina.runtime.research

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.Optional

data class ResearchEntrySnapshot(
    val currentResearch: String?,
    val currentProgress: Int,
    val completedResearch: List<String>,
    val queuedResearch: List<String>,
) {
    companion object {
        val EMPTY = ResearchEntrySnapshot(
            currentResearch = null,
            currentProgress = 0,
            completedResearch = emptyList(),
            queuedResearch = emptyList(),
        )

        val CODEC: Codec<ResearchEntrySnapshot> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.STRING.optionalFieldOf("currentResearch").forGetter { Optional.ofNullable(it.currentResearch) },
                Codec.INT.optionalFieldOf("currentProgress", 0).forGetter(ResearchEntrySnapshot::currentProgress),
                Codec.STRING.listOf().optionalFieldOf("completedResearch", emptyList()).forGetter(ResearchEntrySnapshot::completedResearch),
                Codec.STRING.listOf().optionalFieldOf("queuedResearch", emptyList()).forGetter(ResearchEntrySnapshot::queuedResearch),
            ).apply(instance) { currentResearch, currentProgress, completedResearch, queuedResearch ->
                ResearchEntrySnapshot(
                    currentResearch = currentResearch.orElse(null),
                    currentProgress = currentProgress,
                    completedResearch = completedResearch,
                    queuedResearch = queuedResearch,
                )
            }
        }

        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ResearchEntrySnapshot> = StreamCodec.of(
            { buffer, snapshot ->
                buffer.writeBoolean(snapshot.currentResearch != null)

                if (snapshot.currentResearch != null) {
                    buffer.writeUtf(snapshot.currentResearch, 256)
                }

                buffer.writeVarInt(snapshot.currentProgress)
                buffer.writeCollection(snapshot.completedResearch) { buf, value -> buf.writeUtf(value, 256) }
                buffer.writeCollection(snapshot.queuedResearch) { buf, value -> buf.writeUtf(value, 256) }
            },
            { buffer ->
                val currentResearch = if (buffer.readBoolean()) {
                    buffer.readUtf(256)
                } else {
                    null
                }

                val currentProgress = buffer.readVarInt()
                val completedResearch = buffer.readList { buf -> buf.readUtf(256) }
                val queuedResearch = buffer.readList { buf -> buf.readUtf(256) }

                ResearchEntrySnapshot(
                    currentResearch = currentResearch,
                    currentProgress = currentProgress,
                    completedResearch = completedResearch,
                    queuedResearch = queuedResearch,
                )
            },
        )
    }
}
