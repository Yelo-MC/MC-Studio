package com.yelo.blockbeats.screen;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

public class TextureButtonComponent extends ButtonComponent {
    private Identifier icon;

    public TextureButtonComponent(Component message, Identifier icon, Consumer<ButtonComponent> onPress) {
        super(message, onPress);
        this.icon = icon;
    }

    public TextureButtonComponent icon(Identifier icon) {
        this.icon = icon;
        return this;
    }

    @Override
    public void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderContents(graphics, mouseX, mouseY, delta);
        if (this.icon == null) return;

        int iconX = this.getX() + (this.width - 8) / 2;
        int iconY = this.getY() + (this.height - 8) / 2;
        ((OwoUIGraphics) graphics).blit(RenderPipelines.GUI_TEXTURED, this.icon,
                iconX, iconY, 0, 0, 8, 8, 8, 8);
    }
}
