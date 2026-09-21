package com.yelo.blockbeats.item;

import com.yelo.blockbeats.blockentity.DawBlockEntity;
import com.yelo.blockbeats.networking.BurnedDiscPlaybackS2CPayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

public final class BurnedDiscPlayback {
    private BurnedDiscPlayback() {}

    public static void tick(Level level, BlockPos pos, JukeboxBlockEntity jukebox) {
        ItemStack stack = jukebox.getTheItem();
        if (!stack.is(ModItems.BURNED_DISC) || jukebox.getSongPlayer().getSong() == null) return;

        long ticks = jukebox.getSongPlayer().getTicksSinceSongStarted();
        int bpm = BurnedDiscData.bpm(stack);
        if (ticks == 1 && level instanceof ServerLevel serverLevel) {
            var payload = new BurnedDiscPlaybackS2CPayload(
                    pos, BurnedDiscData.notes(stack), bpm, true, BurnedDiscData.displayName(stack)
            );
            PlayerLookup.tracking(serverLevel, pos).forEach(player -> ServerPlayNetworking.send(player, payload));
        }

        long stepNumber = ticks * bpm / 300L;
        if (stepNumber >= DawBlockEntity.STEP_COUNT) {
            jukebox.getSongPlayer().stop(level, jukebox.getBlockState());
        }
    }

    public static void stop(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var payload = new BurnedDiscPlaybackS2CPayload(pos, new long[0], 120, false, "");
        PlayerLookup.tracking(serverLevel, pos).forEach(player -> ServerPlayNetworking.send(player, payload));
    }
}
