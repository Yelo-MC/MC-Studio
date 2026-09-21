package com.yelo.blockbeats.networking;

import com.yelo.blockbeats.BlockBeats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetDawBpmC2SPayload(BlockPos blockPos, int bpm) implements CustomPacketPayload {
    public static final Type<SetDawBpmC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "set_daw_bpm")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SetDawBpmC2SPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetDawBpmC2SPayload::blockPos,
            ByteBufCodecs.VAR_INT, SetDawBpmC2SPayload::bpm,
            SetDawBpmC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
