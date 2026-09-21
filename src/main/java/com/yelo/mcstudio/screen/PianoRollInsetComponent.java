package com.yelo.mcstudio.screen;

import com.yelo.mcstudio.blockentity.DawBlockEntity;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.NinePatchTexture;

public class PianoRollInsetComponent extends BaseUIComponent {
    public PianoRollInsetComponent() {
        this.sizing(Sizing.fill(100),
                Sizing.fixed(DawBlockEntity.PITCH_COUNT * PianoRollComponent.CELL_HEIGHT + 2));
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        this.drawEdge(graphics, this.x, this.y, this.x + this.width, this.y + 1);
        this.drawEdge(graphics, this.x, this.y + this.height - 1, this.x + this.width, this.y + this.height);
        this.drawEdge(graphics, this.x, this.y, this.x + 1, this.y + this.height);
        this.drawEdge(graphics, this.x + this.width - 1, this.y, this.x + this.width, this.y + this.height);
    }

    private void drawEdge(OwoUIGraphics graphics, int left, int top, int right, int bottom) {
        graphics.enableScissor(left, top, right, bottom);
        NinePatchTexture.draw(OwoUIGraphics.PANEL_INSET_NINE_PATCH_TEXTURE,
                graphics, this.x, this.y, this.width, this.height);
        graphics.disableScissor();
    }

    @Override
    public boolean isInBoundingBox(double x, double y) {
        return false;
    }
}
