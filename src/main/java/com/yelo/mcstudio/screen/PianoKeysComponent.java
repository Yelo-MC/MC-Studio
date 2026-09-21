package com.yelo.mcstudio.screen;

import com.yelo.mcstudio.blockentity.DawBlockEntity;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;

public class PianoKeysComponent extends BaseUIComponent {
    private static final String[] NOTE_NAMES = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
    private static final int LOWEST_MIDI_NOTE = 54;

    private final IntConsumer preview;

    public PianoKeysComponent(IntConsumer preview) {
        this.preview = preview;
        this.sizing(Sizing.fixed(PianoRollComponent.KEY_WIDTH),
                Sizing.fixed(DawBlockEntity.PITCH_COUNT * PianoRollComponent.CELL_HEIGHT));
        this.cursorStyle(CursorStyle.HAND);
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        for (int row = 0; row < DawBlockEntity.PITCH_COUNT; row++) {
            int pitch = DawBlockEntity.PITCH_COUNT - 1 - row;
            int keyY = this.y + row * PianoRollComponent.CELL_HEIGHT;
            int keyColor = isBlackKey(pitch) ? 0xff343741 : 0xffd5d7dc;
            graphics.fill(this.x, keyY, this.x + this.width - 1,
                    keyY + PianoRollComponent.CELL_HEIGHT - 1, keyColor);

            String noteName = noteName(pitch);
            if (noteName.equals("C4") || noteName.equals("C5")) {
                graphics.drawString(Minecraft.getInstance().font, Component.literal(noteName),
                        this.x + 2, keyY, 0xff202020, false);
            }
        }
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) return false;
        int localY = (int) click.y();
        if (localY < 0 || localY >= this.height) return false;
        this.preview.accept(DawBlockEntity.PITCH_COUNT - 1 - localY / PianoRollComponent.CELL_HEIGHT);
        return true;
    }

    private static boolean isBlackKey(int pitch) {
        return switch ((LOWEST_MIDI_NOTE + pitch) % 12) {
            case 1, 3, 6, 8, 10 -> true;
            default -> false;
        };
    }

    private static String noteName(int pitch) {
        int midiNote = LOWEST_MIDI_NOTE + pitch;
        return NOTE_NAMES[midiNote % 12] + (midiNote / 12 - 1);
    }
}
