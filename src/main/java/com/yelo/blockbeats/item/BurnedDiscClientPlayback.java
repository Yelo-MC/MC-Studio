package com.yelo.blockbeats.item;

import com.yelo.blockbeats.blockentity.DawBlockEntity;
import com.yelo.blockbeats.networking.BurnedDiscPlaybackS2CPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import java.util.HashMap;
import java.util.Map;

public final class BurnedDiscClientPlayback {
    private static final Map<BlockPos, Playback> PLAYBACKS = new HashMap<>();

    private BurnedDiscClientPlayback() {}

    public static void handle(BurnedDiscPlaybackS2CPayload payload) {
        if (payload.playing()) {
            PLAYBACKS.put(payload.blockPos().immutable(),
                    new Playback(payload.notes().clone(), payload.bpm(), 0, System.nanoTime()));
            if (!payload.nowPlaying().isBlank()) {
                Minecraft.getInstance().gui.setNowPlaying(Component.literal(payload.nowPlaying()));
            }
        } else {
            PLAYBACKS.remove(payload.blockPos());
        }
    }

    public static void renderTick() {
        long now = System.nanoTime();
        PLAYBACKS.entrySet().removeIf(entry -> {
            Playback playback = entry.getValue();
            while (playback.step < DawBlockEntity.STEP_COUNT && now >= playback.nextStepAt) {
                playStep(entry.getKey(), playback.notes, playback.step);
                playback.step++;
                playback.nextStepAt += 15_000_000_000L / playback.bpm;
            }
            return playback.step >= DawBlockEntity.STEP_COUNT;
        });
    }

    private static void playStep(BlockPos pos, long[] notes, int step) {
        for (int instrument = 0; instrument < DawBlockEntity.INSTRUMENT_COUNT; instrument++) {
            for (int pitch = 0; pitch < DawBlockEntity.PITCH_COUNT; pitch++) {
                int noteIndex = step * DawBlockEntity.PITCH_COUNT + pitch;
                int wordIndex = instrument * DawBlockEntity.WORDS_PER_INSTRUMENT + noteIndex / Long.SIZE;
                if ((notes[wordIndex] & (1L << (noteIndex % Long.SIZE))) == 0L) continue;

                Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(
                        soundForInstrument(instrument), SoundSource.RECORDS, 1.0F,
                        (float) Math.pow(2.0, (pitch - 12) / 12.0), RandomSource.create(), pos
                ));
            }
        }
    }

    private static SoundEvent soundForInstrument(int instrument) {
        return (switch (instrument) {
            case 1 -> SoundEvents.NOTE_BLOCK_BASS;
            case 2 -> SoundEvents.NOTE_BLOCK_BELL;
            case 3 -> SoundEvents.NOTE_BLOCK_PLING;
            case 4 -> SoundEvents.NOTE_BLOCK_BASEDRUM;
            case 5 -> SoundEvents.NOTE_BLOCK_SNARE;
            case 6 -> SoundEvents.NOTE_BLOCK_HAT;
            case 7 -> SoundEvents.NOTE_BLOCK_FLUTE;
            case 8 -> SoundEvents.NOTE_BLOCK_GUITAR;
            case 9 -> SoundEvents.NOTE_BLOCK_CHIME;
            case 10 -> SoundEvents.NOTE_BLOCK_XYLOPHONE;
            case 11 -> SoundEvents.NOTE_BLOCK_IRON_XYLOPHONE;
            case 12 -> SoundEvents.NOTE_BLOCK_COW_BELL;
            case 13 -> SoundEvents.NOTE_BLOCK_DIDGERIDOO;
            case 14 -> SoundEvents.NOTE_BLOCK_BIT;
            case 15 -> SoundEvents.NOTE_BLOCK_BANJO;
            default -> SoundEvents.NOTE_BLOCK_HARP;
        }).value();
    }

    private static final class Playback {
        private final long[] notes;
        private final int bpm;
        private int step;
        private long nextStepAt;

        private Playback(long[] notes, int bpm, int step, long nextStepAt) {
            this.notes = notes;
            this.bpm = bpm;
            this.step = step;
            this.nextStepAt = nextStepAt;
        }
    }
}
