package com.yelo.mcstudio.networking;

import com.yelo.mcstudio.MCStudio;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetDawLoopPointC2SPayload(BlockPos blockPos, int loopPoint) implements CustomPacketPayload {
    public static final Type<SetDawLoopPointC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(MCStudio.MOD_ID, "set_daw_loop_point")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetDawLoopPointC2SPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetDawLoopPointC2SPayload::blockPos,
            ByteBufCodecs.VAR_INT, SetDawLoopPointC2SPayload::loopPoint,
            SetDawLoopPointC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
