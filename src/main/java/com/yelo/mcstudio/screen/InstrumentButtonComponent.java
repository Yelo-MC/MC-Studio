package com.yelo.mcstudio.screen;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;

public class InstrumentButtonComponent extends BaseUIComponent {
    private static final int SIZE = 26;
    private static final int ICON_OFFSET = (SIZE - 16) / 2;

    private final int instrument;
    private final IntConsumer onSelect;
    private boolean selected;

    public InstrumentButtonComponent(int instrument, String name, IntConsumer onSelect) {
        this.instrument = instrument;
        this.onSelect = onSelect;
        this.sizing(Sizing.fixed(SIZE));
        this.cursorStyle(CursorStyle.HAND);
        this.tooltip(Component.literal(name));
    }

    public void selected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        int outer = this.selected ? 0xfff0c541 : 0xff373737;
        int topLeft = this.selected ? 0xffffe47a : 0xff8b8b8b;
        int bottomRight = this.selected ? 0xff9b6f12 : 0xff1f1f1f;

        graphics.fill(this.x, this.y, this.x + SIZE, this.y + SIZE, outer);
        graphics.fill(this.x + 1, this.y + 1, this.x + SIZE - 1, this.y + SIZE - 1, bottomRight);
        graphics.fill(this.x + 1, this.y + 1, this.x + SIZE - 2, this.y + SIZE - 2, topLeft);
        graphics.fill(this.x + 2, this.y + 2, this.x + SIZE - 2, this.y + SIZE - 2, 0xff555555);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                PianoRollComponent.instrumentTexture(this.instrument),
                this.x + ICON_OFFSET,
                this.y + ICON_OFFSET,
                0,
                0,
                16,
                16,
                16,
                16,
                16,
                16
        );

    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        if (click.button() != GLFW.GLFW_MOUSE_BUTTON_1) return super.onMouseDown(click, doubled);
        this.onSelect.accept(this.instrument);
        return true;
    }
}
