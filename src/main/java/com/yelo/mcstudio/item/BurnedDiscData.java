package com.yelo.mcstudio.item;

import com.yelo.mcstudio.blockentity.DawBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class BurnedDiscData {
    private static final int LEGACY_STEP_COUNT = 64;
    private static final int LEGACY_WORDS_PER_INSTRUMENT =
            (LEGACY_STEP_COUNT * DawBlockEntity.PITCH_COUNT + Long.SIZE - 1) / Long.SIZE;
    private static final String NOTES_KEY = "MCStudioNotes";
    private static final String BPM_KEY = "MCStudioBpm";

    private BurnedDiscData() {}

    public static void write(ItemStack stack, long[] notes, int bpm, String playerName, String songName) {
        CompoundTag tag = new CompoundTag();
        tag.putLongArray(NOTES_KEY, notes);
        tag.putInt(BPM_KEY, bpm);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        int centerColor = Mth.hsvToRgb(ThreadLocalRandom.current().nextFloat(), 0.75F, 1.0F) & 0xFFFFFF;
        stack.set(DataComponents.CUSTOM_MODEL_DATA,
                new CustomModelData(List.of(), List.of(), List.of(), List.of(centerColor)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.literal(playerName + " - " + songName)
                        .withStyle(ChatFormatting.GRAY)
                        .withStyle(style -> style.withItalic(false))
        )));
        stack.set(DataComponents.TOOLTIP_DISPLAY,
                TooltipDisplay.DEFAULT.withHidden(DataComponents.JUKEBOX_PLAYABLE, true));
    }

    public static long[] notes(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        long[] stored = data.copyTag().getLongArray(NOTES_KEY).orElse(new long[0]);
        long[] notes = new long[DawBlockEntity.INSTRUMENT_COUNT * DawBlockEntity.WORDS_PER_INSTRUMENT];
        if (stored.length == DawBlockEntity.INSTRUMENT_COUNT * LEGACY_WORDS_PER_INSTRUMENT) {
            for (int instrument = 0; instrument < DawBlockEntity.INSTRUMENT_COUNT; instrument++) {
                System.arraycopy(stored, instrument * LEGACY_WORDS_PER_INSTRUMENT,
                        notes, instrument * DawBlockEntity.WORDS_PER_INSTRUMENT,
                        LEGACY_WORDS_PER_INSTRUMENT);
            }
            return notes;
        }
        System.arraycopy(stored, 0, notes, 0, Math.min(stored.length, notes.length));
        return notes;
    }

    public static int bpm(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getIntOr(BPM_KEY, 120);
    }

    public static String displayName(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        return lore == null || lore.lines().isEmpty() ? "Burned Disc" : lore.lines().getFirst().getString();
    }
}
