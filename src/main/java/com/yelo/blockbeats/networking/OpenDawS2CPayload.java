package com.yelo.blockbeats.networking;

import com.yelo.blockbeats.BlockBeats;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record OpenDawS2CPayload(BlockPos blockPos) implements CustomPacketPayload {
    public static final Identifier OPEN_DAW_PAYLOAD_ID = Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "open_daw");
    public static final CustomPacketPayload.Type<OpenDawS2CPayload> ID = new CustomPacketPayload.Type<>(OPEN_DAW_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDawS2CPayload> CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, OpenDawS2CPayload::blockPos, OpenDawS2CPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
