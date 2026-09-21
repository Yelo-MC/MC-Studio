package com.yelo.blockbeats.mixin;

import com.yelo.blockbeats.item.BurnedDiscPlayback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JukeboxBlockEntity.class)
public abstract class JukeboxBlockEntityMixin {
    @Inject(method = "setTheItem", at = @At("HEAD"))
    private void blockbeats$stopRecordedNotes(ItemStack newStack, CallbackInfo ci) {
        JukeboxBlockEntity jukebox = (JukeboxBlockEntity) (Object) this;
        if (jukebox.getLevel() != null && jukebox.getTheItem().is(com.yelo.blockbeats.item.ModItems.BURNED_DISC)) {
            BurnedDiscPlayback.stop(jukebox.getLevel(), jukebox.getBlockPos());
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private static void blockbeats$playRecordedNotes(Level level, BlockPos pos, BlockState state,
                                                     JukeboxBlockEntity jukebox, CallbackInfo ci) {
        if (!level.isClientSide()) BurnedDiscPlayback.tick(level, pos, jukebox);
    }
}
