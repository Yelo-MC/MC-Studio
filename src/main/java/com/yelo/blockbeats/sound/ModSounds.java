package com.yelo.blockbeats.sound;

import com.yelo.blockbeats.BlockBeats;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final Identifier BURNED_DISC_SILENCE_ID =
            Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "burned_disc_silence");
    public static final SoundEvent BURNED_DISC_SILENCE =
            SoundEvent.createVariableRangeEvent(BURNED_DISC_SILENCE_ID);

    private ModSounds() {}

    public static void init() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, BURNED_DISC_SILENCE_ID, BURNED_DISC_SILENCE);
    }
}
