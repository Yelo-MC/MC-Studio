package com.yelo.mcstudio.networking;

import com.yelo.mcstudio.MCStudio;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetDawSongNameC2SPayload(BlockPos blockPos, String songName) implements CustomPacketPayload {
    public static final Type<SetDawSongNameC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(MCStudio.MOD_ID, "set_daw_song_name")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, SetDawSongNameC2SPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetDawSongNameC2SPayload::blockPos,
            ByteBufCodecs.STRING_UTF8, SetDawSongNameC2SPayload::songName,
            SetDawSongNameC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
