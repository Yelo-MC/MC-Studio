package com.yelo.blockbeats.networking;

import com.yelo.blockbeats.BlockBeats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenDawS2CPayload(BlockPos blockPos, long[] notes, int bpm, String songName, int loopPoint) implements CustomPacketPayload {
    public static final Identifier OPEN_DAW_PAYLOAD_ID = Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "open_daw");
    public static final CustomPacketPayload.Type<OpenDawS2CPayload> ID = new CustomPacketPayload.Type<>(OPEN_DAW_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDawS2CPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenDawS2CPayload::blockPos,
            ByteBufCodecs.LONG_ARRAY, OpenDawS2CPayload::notes,
            ByteBufCodecs.VAR_INT, OpenDawS2CPayload::bpm,
            ByteBufCodecs.STRING_UTF8, OpenDawS2CPayload::songName,
            ByteBufCodecs.VAR_INT, OpenDawS2CPayload::loopPoint,
            OpenDawS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
