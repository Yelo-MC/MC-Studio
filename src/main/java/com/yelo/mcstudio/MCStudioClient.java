package com.yelo.mcstudio;

import com.yelo.mcstudio.item.BurnedDiscClientPlayback;
import com.yelo.mcstudio.networking.BurnedDiscPlaybackS2CPayload;
import com.yelo.mcstudio.screen.DawScreen;
import com.yelo.mcstudio.screen.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;

public class MCStudioClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModKeyBindings.init();
        MenuScreens.register(ModMenus.DAW_MENU, DawScreen::new);
        ClientPlayNetworking.registerGlobalReceiver(BurnedDiscPlaybackS2CPayload.ID, (payload, context) ->
                context.client().execute(() -> BurnedDiscClientPlayback.handle(payload))
        );
        WorldRenderEvents.START_MAIN.register(context -> BurnedDiscClientPlayback.renderTick());
    }
}
