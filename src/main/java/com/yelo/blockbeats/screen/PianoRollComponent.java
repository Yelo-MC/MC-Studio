package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.blockentity.DawBlockEntity;
import com.yelo.blockbeats.networking.SetDawNoteC2SPayload;
import com.yelo.blockbeats.networking.SetDawLoopPointC2SPayload;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent.FocusSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import org.lwjgl.glfw.GLFW;

import java.util.BitSet;

public class PianoRollComponent extends BaseUIComponent {
    public static final int KEY_WIDTH = 22;
    public static final int CELL_WIDTH = 17;
    public static final int CELL_HEIGHT = 9;

    private static final int GRID_WIDTH = DawBlockEntity.STEP_COUNT * CELL_WIDTH;
    private static final int GRID_HEIGHT = DawBlockEntity.PITCH_COUNT * CELL_HEIGHT;
    private static final int LOWEST_MIDI_NOTE = 54;
    private static final Identifier[] INSTRUMENT_TEXTURES = {
            blockTexture("dirt"),
            blockTexture("oak_planks"),
            blockTexture("gold_block"),
            blockTexture("glowstone"),
            blockTexture("stone"),
            blockTexture("sand"),
            blockTexture("glass"),
            blockTexture("clay"),
            blockTexture("white_wool"),
            blockTexture("packed_ice"),
            blockTexture("bone_block_side"),
            blockTexture("iron_block"),
            blockTexture("soul_sand"),
            blockTexture("pumpkin_side"),
            blockTexture("emerald_block"),
            blockTexture("hay_block_side")
    };
    private final BlockPos blockPos;
    private final BitSet[] notes = new BitSet[DawBlockEntity.INSTRUMENT_COUNT];
    private int selectedInstrument;
    private int playhead = -1;
    private int loopPoint;
    private boolean loopEditing;

    public PianoRollComponent(BlockPos blockPos, long[] packedNotes, int loopPoint) {
        this.blockPos = blockPos;
        this.loopPoint = loopPoint;
        for (int instrument = 0; instrument < DawBlockEntity.INSTRUMENT_COUNT; instrument++) {
            long[] instrumentWords = new long[DawBlockEntity.WORDS_PER_INSTRUMENT];
            int sourceOffset = instrument * DawBlockEntity.WORDS_PER_INSTRUMENT;
            if (sourceOffset < packedNotes.length) {
                System.arraycopy(
                        packedNotes,
                        sourceOffset,
                        instrumentWords,
                        0,
                        Math.min(DawBlockEntity.WORDS_PER_INSTRUMENT, packedNotes.length - sourceOffset)
                );
            }
            this.notes[instrument] = BitSet.valueOf(instrumentWords);
        }
        this.sizing(Sizing.fixed(KEY_WIDTH + GRID_WIDTH), Sizing.fixed(GRID_HEIGHT));
        this.cursorStyle(CursorStyle.HAND);
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return KEY_WIDTH + GRID_WIDTH;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return GRID_HEIGHT;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        graphics.fill(this.x, this.y, this.x + this.width, this.y + this.height, 0xff17191f);
        int hoveredLoopStep = -1;
        if (this.loopEditing && mouseX >= this.x + KEY_WIDTH && mouseX < this.x + this.width
                && mouseY >= this.y && mouseY < this.y + this.height) {
            hoveredLoopStep = (mouseX - this.x - KEY_WIDTH) / CELL_WIDTH;
        }

        for (int row = 0; row < DawBlockEntity.PITCH_COUNT; row++) {
            int pitch = DawBlockEntity.PITCH_COUNT - 1 - row;
            int cellY = this.y + row * CELL_HEIGHT;
            for (int step = 0; step < DawBlockEntity.STEP_COUNT; step++) {
                int cellX = this.x + KEY_WIDTH + step * CELL_WIDTH;
                boolean blackKeyRow = isBlackKey(pitch);
                int background;
                if (blackKeyRow) {
                    background = step % 4 == 0 ? 0xff1e2128 : 0xff171a20;
                } else {
                    background = step % 4 == 0 ? 0xff2b3039 : 0xff23272f;
                }
                if (step == this.playhead) background = 0xff423d25;
                if (step == this.loopPoint) background = 0xff245f34;
                else if (step == hoveredLoopStep) background = 0xff183d24;
                graphics.fill(cellX, cellY, cellX + CELL_WIDTH - 1, cellY + CELL_HEIGHT - 1, background);

                if (this.hasNote(this.selectedInstrument, step, pitch)) {
                    drawInstrumentNote(graphics, cellX, cellY, this.selectedInstrument, false);
                } else {
                    int otherInstrument = this.instrumentAt(step, pitch);
                    if (otherInstrument >= 0) {
                        drawInstrumentNote(graphics, cellX, cellY, otherInstrument, true);
                    }
                }
            }
        }

    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1 && click.button() != GLFW.GLFW_MOUSE_BUTTON_2) {
            return super.onMouseDown(click, doubled);
        }

        return this.editAt(click, click.button() == GLFW.GLFW_MOUSE_BUTTON_1);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1 && click.button() != GLFW.GLFW_MOUSE_BUTTON_2) {
            return super.onMouseDrag(click, deltaX, deltaY);
        }

        if (this.loopEditing) return true;
        return this.editAt(click, click.button() == GLFW.GLFW_MOUSE_BUTTON_1);
    }

    private boolean editAt(MouseButtonEvent click, boolean enabled) {
        // owo dispatches coordinates relative to the receiving child component
        int localX = (int) click.x() - KEY_WIDTH;
        int localY = (int) click.y();
        if (localY < 0 || localY >= GRID_HEIGHT) return false;

        int pitch = DawBlockEntity.PITCH_COUNT - 1 - localY / CELL_HEIGHT;
        if (localX < 0) {
            if (enabled) {
                playPreview(this.selectedInstrument, pitch);
                return true;
            }
            return false;
        }

        int step = localX / CELL_WIDTH;
        if (!DawBlockEntity.isValidNote(step, pitch)) return false;
        if (this.loopEditing) {
            if (enabled && step > 0) {
                this.loopPoint = this.loopPoint == step ? DawBlockEntity.STEP_COUNT : step;
                ClientPlayNetworking.send(new SetDawLoopPointC2SPayload(this.blockPos, this.loopPoint));
            }
            return true;
        }
        if (this.hasNote(this.selectedInstrument, step, pitch) == enabled) return true;

        this.notes[this.selectedInstrument].set(index(step, pitch), enabled);
        ClientPlayNetworking.send(new SetDawNoteC2SPayload(this.blockPos, this.selectedInstrument, step, pitch, enabled));
        if (enabled) playPreview(this.selectedInstrument, pitch);
        return true;
    }

    public void playStep(int step) {
        this.playhead = step;
        for (int instrument = 0; instrument < DawBlockEntity.INSTRUMENT_COUNT; instrument++) {
            for (int pitch = 0; pitch < DawBlockEntity.PITCH_COUNT; pitch++) {
                if (this.hasNote(instrument, step, pitch)) playPreview(instrument, pitch);
            }
        }
    }

    public void selectInstrument(int instrument) {
        if (DawBlockEntity.isValidInstrument(instrument)) this.selectedInstrument = instrument;
    }

    public void previewInstrument(int instrument) {
        if (DawBlockEntity.isValidInstrument(instrument)) playPreview(instrument, 12);
    }

    public void previewPitch(int pitch) {
        if (pitch >= 0 && pitch < DawBlockEntity.PITCH_COUNT) playPreview(this.selectedInstrument, pitch);
    }

    public void stop() {
        this.playhead = -1;
    }

    public void loopEditing(boolean loopEditing) {
        this.loopEditing = loopEditing;
    }

    public int loopPoint() {
        return this.loopPoint;
    }

    private boolean hasNote(int instrument, int step, int pitch) {
        return this.notes[instrument].get(index(step, pitch));
    }

    private int instrumentAt(int step, int pitch) {
        for (int instrument = 0; instrument < DawBlockEntity.INSTRUMENT_COUNT; instrument++) {
            if (instrument != this.selectedInstrument && this.hasNote(instrument, step, pitch)) return instrument;
        }
        return -1;
    }

    private static Identifier blockTexture(String name) {
        return Identifier.fromNamespaceAndPath("minecraft", "textures/block/" + name + ".png");
    }

    public static Identifier instrumentTexture(int instrument) {
        return INSTRUMENT_TEXTURES[instrument];
    }

    private static void drawInstrumentNote(OwoUIGraphics graphics, int x, int y, int instrument, boolean darkened) {
        // Keep the texture's vertical pixel scale: use its top and bottom four rows
        // instead of squeezing all sixteen rows into the eight-pixel-high note cell.
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                INSTRUMENT_TEXTURES[instrument],
                x,
                y,
                0,
                0,
                CELL_WIDTH - 1,
                4,
                CELL_WIDTH - 1,
                4,
                16,
                16
        );
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                INSTRUMENT_TEXTURES[instrument],
                x,
                y + 4,
                0,
                12,
                CELL_WIDTH - 1,
                4,
                CELL_WIDTH - 1,
                4,
                16,
                16
        );
        if (darkened) {
            graphics.fill(x, y, x + CELL_WIDTH - 1, y + CELL_HEIGHT - 1, 0x88000000);
        }
    }

    private static int index(int step, int pitch) {
        return step * DawBlockEntity.PITCH_COUNT + pitch;
    }

    private static boolean isBlackKey(int pitch) {
        return switch ((LOWEST_MIDI_NOTE + pitch) % 12) {
            case 1, 3, 6, 8, 10 -> true;
            default -> false;
        };
    }

    private void playPreview(int instrument, int pitch) {
        float soundPitch = (float) Math.pow(2.0, (pitch - 12) / 12.0);
        var sound = switch (instrument) {
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
        };
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound.value(), soundPitch, 0.7F));
    }
}
