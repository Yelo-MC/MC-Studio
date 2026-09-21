package com.yelo.mcstudio.networking;

import com.yelo.mcstudio.MCStudio;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BurnedDiscPlaybackS2CPayload(BlockPos blockPos, long[] notes, int bpm,
                                           boolean playing, String nowPlaying)
        implements CustomPacketPayload {
    public static final Type<BurnedDiscPlaybackS2CPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(MCStudio.MOD_ID, "burned_disc_playback")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BurnedDiscPlaybackS2CPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, BurnedDiscPlaybackS2CPayload::blockPos,
            ByteBufCodecs.LONG_ARRAY, BurnedDiscPlaybackS2CPayload::notes,
            ByteBufCodecs.VAR_INT, BurnedDiscPlaybackS2CPayload::bpm,
            ByteBufCodecs.BOOL, BurnedDiscPlaybackS2CPayload::playing,
            ByteBufCodecs.STRING_UTF8, BurnedDiscPlaybackS2CPayload::nowPlaying,
            BurnedDiscPlaybackS2CPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
