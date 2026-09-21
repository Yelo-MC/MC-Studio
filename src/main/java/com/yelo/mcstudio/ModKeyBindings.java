package com.yelo.mcstudio;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public final class ModKeyBindings {
    public static final KeyMapping.Category MCSTUDIO_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(MCStudio.MOD_ID, "controls")
    );

    public static final KeyMapping PLAY_PAUSE = register("play_pause", GLFW.GLFW_KEY_SPACE);
    public static final KeyMapping STOP = register("stop", GLFW.GLFW_KEY_BACKSPACE);
    public static final KeyMapping LOOP_MODE = register("loop_mode", GLFW.GLFW_KEY_L);

    private ModKeyBindings() {}

    private static KeyMapping register(String name, int defaultKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + MCStudio.MOD_ID + "." + name,
                InputConstants.Type.KEYSYM,
                defaultKey,
                MCSTUDIO_CATEGORY
        ));
    }

    public static void init() {}
}
