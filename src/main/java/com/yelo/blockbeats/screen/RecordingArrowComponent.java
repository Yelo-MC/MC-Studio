package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.blockentity.DawBlockEntity;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class RecordingArrowComponent extends BaseUIComponent {
    private static final Identifier FURNACE_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    private final DawMenu menu;

    public RecordingArrowComponent(DawMenu menu) {
        this.menu = menu;
        this.sizing(Sizing.fixed(24), Sizing.fixed(16));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        graphics.blit(RenderPipelines.GUI_TEXTURED, FURNACE_TEXTURE,
                this.x, this.y, 79, 34, 24, 16, 256, 256);

        int progress = Mth.ceil((this.menu.recordingProgress() / (float) DawBlockEntity.RECORDING_TIME) * 24.0F);
        if (progress > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_SPRITE, 24, 16, 0, 0,
                    this.x, this.y, progress, 16);
        }
    }
}
