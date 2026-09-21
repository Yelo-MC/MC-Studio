package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.BlockBeats;
import com.yelo.blockbeats.networking.OpenDawS2CPayload;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModMenus {
    public static final ExtendedScreenHandlerType<DawMenu, OpenDawS2CPayload> DAW_MENU =
            new ExtendedScreenHandlerType<>(DawMenu::new, OpenDawS2CPayload.CODEC);

    private ModMenus() {}

    public static void init() {
        Registry.register(
                BuiltInRegistries.MENU,
                Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "daw"),
                DAW_MENU
        );
    }
}
