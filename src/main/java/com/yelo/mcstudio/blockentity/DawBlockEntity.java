package com.yelo.mcstudio.blockentity;

import com.yelo.mcstudio.item.ModItems;
import com.yelo.mcstudio.item.BurnedDiscData;
import com.yelo.mcstudio.networking.OpenDawS2CPayload;
import com.yelo.mcstudio.screen.DawMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.BitSet;

public class DawBlockEntity extends BlockEntity implements Container, ExtendedScreenHandlerFactory<OpenDawS2CPayload> {
    public static final int STEP_COUNT = 128;
    public static final int PITCH_COUNT = 25;
    public static final int INSTRUMENT_COUNT = 16;
    public static final int WORDS_PER_INSTRUMENT = (STEP_COUNT * PITCH_COUNT + Long.SIZE - 1) / Long.SIZE;
    public static final int RECORDING_TIME = 200;
    public static final int MAX_SONG_NAME_LENGTH = 32;

    private final BitSet[] notes = new BitSet[INSTRUMENT_COUNT];
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private int bpm = 120;
    private int loopPoint = STEP_COUNT;
    private int recordingProgress;
    private String songName = "";
    private String recordingPlayer = "";
    private final ContainerData menuData = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> DawBlockEntity.this.recordingProgress;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            if (index == 0) DawBlockEntity.this.recordingProgress = value;
        }
        @Override public int getCount() { return 1; }
    };

    public DawBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.DAW_BLOCK_ENTITY, blockPos, blockState);
        for (int instrument = 0; instrument < INSTRUMENT_COUNT; instrument++) {
            this.notes[instrument] = new BitSet(STEP_COUNT * PITCH_COUNT);
        }
    }

    public boolean hasNote(int instrument, int step, int pitch) {
        return isValidInstrument(instrument) && isValidNote(step, pitch) && this.notes[instrument].get(index(step, pitch));
    }

    public void setNote(int instrument, int step, int pitch, boolean enabled) {
        if (!isValidInstrument(instrument) || !isValidNote(step, pitch)) return;
        this.notes[instrument].set(index(step, pitch), enabled);
        this.setChanged();
    }

    public long[] getPackedNotes() {
        long[] packed = new long[INSTRUMENT_COUNT * WORDS_PER_INSTRUMENT];
        for (int instrument = 0; instrument < INSTRUMENT_COUNT; instrument++) {
            long[] usedWords = this.notes[instrument].toLongArray();
            System.arraycopy(usedWords, 0, packed, instrument * WORDS_PER_INSTRUMENT,
                    Math.min(usedWords.length, WORDS_PER_INSTRUMENT));
        }
        return packed;
    }

    public int getBpm() { return this.bpm; }

    public void setBpm(int bpm) {
        if (bpm < 40 || bpm > 240) return;
        this.bpm = bpm;
        this.setChanged();
    }

    public int getLoopPoint() { return this.loopPoint; }

    public void setLoopPoint(int loopPoint) {
        if (loopPoint < 1 || loopPoint > STEP_COUNT) return;
        this.loopPoint = loopPoint;
        this.setChanged();
    }

    public String getSongName() { return this.songName; }

    public void setSongName(String songName) {
        this.songName = songName.substring(0, Math.min(songName.length(), MAX_SONG_NAME_LENGTH));
        this.setChanged();
    }

    public void setRecordingPlayer(String recordingPlayer) {
        this.recordingPlayer = recordingPlayer;
        this.setChanged();
    }

    public ContainerData getMenuData() { return this.menuData; }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DawBlockEntity daw) {
        boolean changed = false;
        ItemStack blankDisc = daw.items.get(DawMenu.BLANK_DISC_SLOT);
        boolean canRecord = blankDisc.is(ModItems.BLANK_DISC)
                && daw.items.get(DawMenu.OUTPUT_SLOT).isEmpty();
        if (canRecord) {
            daw.recordingProgress++;
            changed = true;
            if (daw.recordingProgress >= RECORDING_TIME) {
                blankDisc.shrink(1);
                ItemStack burnedDisc = new ItemStack(ModItems.BURNED_DISC);
                String finalSongName = daw.songName.isBlank() ? "New Song" : daw.songName;
                BurnedDiscData.write(burnedDisc, daw.getPackedNotes(), daw.bpm,
                        daw.recordingPlayer, finalSongName);
                daw.items.set(DawMenu.OUTPUT_SLOT, burnedDisc);
                daw.recordingProgress = 0;
            }
        } else if (daw.recordingProgress != 0) {
            daw.recordingProgress = 0;
            changed = true;
        }
        if (changed) daw.setChanged();
    }

    public static boolean isValidInstrument(int instrument) { return instrument >= 0 && instrument < INSTRUMENT_COUNT; }
    public static boolean isValidNote(int step, int pitch) {
        return step >= 0 && step < STEP_COUNT && pitch >= 0 && pitch < PITCH_COUNT;
    }
    private static int index(int step, int pitch) { return step * PITCH_COUNT + pitch; }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int instrument = 0; instrument < INSTRUMENT_COUNT; instrument++) {
            long[] packed = new long[WORDS_PER_INSTRUMENT];
            for (int word = 0; word < WORDS_PER_INSTRUMENT; word++) {
                String key = "instrument_" + instrument + "_word_" + word;
                packed[word] = instrument == 0
                        ? input.getLong(key).orElse(input.getLongOr("note_word_" + word, 0L))
                        : input.getLongOr(key, 0L);
            }
            this.notes[instrument].clear();
            this.notes[instrument].or(BitSet.valueOf(packed));
        }
        this.bpm = input.getIntOr("bpm", 120);
        this.loopPoint = Math.clamp(input.getIntOr("loop_point", STEP_COUNT), 1, STEP_COUNT);
        this.recordingProgress = input.getIntOr("recording_progress", 0);
        this.songName = input.getStringOr("song_name", "");
        this.recordingPlayer = input.getStringOr("recording_player", "");
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        long[] packed = this.getPackedNotes();
        for (int instrument = 0; instrument < INSTRUMENT_COUNT; instrument++) {
            for (int word = 0; word < WORDS_PER_INSTRUMENT; word++) {
                output.putLong("instrument_" + instrument + "_word_" + word,
                        packed[instrument * WORDS_PER_INSTRUMENT + word]);
            }
        }
        output.putInt("bpm", this.bpm);
        output.putInt("loop_point", this.loopPoint);
        output.putInt("recording_progress", this.recordingProgress);
        output.putString("song_name", this.songName);
        output.putString("recording_player", this.recordingPlayer);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override public int getContainerSize() { return this.items.size(); }
    @Override public boolean isEmpty() { return this.items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return this.items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(this.items, slot); }
    @Override public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }
    @Override public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override public void clearContent() { this.items.clear(); this.setChanged(); }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case DawMenu.BLANK_DISC_SLOT -> stack.is(ModItems.BLANK_DISC);
            default -> false;
        };
    }

    @Override public Component getDisplayName() { return Component.translatable("container.mcstudio.daw"); }
    @Override public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new DawMenu(containerId, inventory, this);
    }
    @Override public OpenDawS2CPayload getScreenOpeningData(ServerPlayer player) {
        return new OpenDawS2CPayload(this.worldPosition, this.getPackedNotes(), this.bpm,
                this.songName, this.loopPoint);
    }
}
