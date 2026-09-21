package com.yelo.mcstudio.screen;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.function.BooleanSupplier;

public class RecordingSlotBackgroundComponent extends BaseUIComponent {
    private static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("container/slot");
    private final Identifier emptySlotSprite;
    private final BooleanSupplier empty;

    public RecordingSlotBackgroundComponent(Identifier emptySlotSprite, BooleanSupplier empty) {
        this.emptySlotSprite = emptySlotSprite;
        this.empty = empty;
        this.sizing(Sizing.fixed(18));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, 18, 18, 0, 0,
                this.x, this.y, 18, 18);
        if (this.empty.getAsBoolean() && this.emptySlotSprite != null) {
            if (this.emptySlotSprite.getPath().startsWith("textures/")) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, this.emptySlotSprite,
                        this.x + 1, this.y + 1, 0, 0, 16, 16, 16, 16);
            } else {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.emptySlotSprite, 16, 16, 0, 0,
                        this.x + 1, this.y + 1, 16, 16);
            }
        }
    }
}
