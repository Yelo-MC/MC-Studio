package com.yelo.mcstudio.networking;

import com.yelo.mcstudio.MCStudio;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SetDawNoteC2SPayload(BlockPos blockPos, int instrument, int step, int pitch, boolean enabled)
        implements CustomPacketPayload {

    public static final Type<SetDawNoteC2SPayload> ID = new Type<>(
            Identifier.fromNamespaceAndPath(MCStudio.MOD_ID, "set_daw_note")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SetDawNoteC2SPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetDawNoteC2SPayload::blockPos,
            ByteBufCodecs.VAR_INT, SetDawNoteC2SPayload::instrument,
            ByteBufCodecs.VAR_INT, SetDawNoteC2SPayload::step,
            ByteBufCodecs.VAR_INT, SetDawNoteC2SPayload::pitch,
            ByteBufCodecs.BOOL, SetDawNoteC2SPayload::enabled,
            SetDawNoteC2SPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
