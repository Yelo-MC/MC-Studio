package com.yelo.blockbeats;

import com.yelo.blockbeats.item.BurnedDiscClientPlayback;
import com.yelo.blockbeats.networking.BurnedDiscPlaybackS2CPayload;
import com.yelo.blockbeats.screen.DawScreen;
import com.yelo.blockbeats.screen.ModMenus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.gui.screens.MenuScreens;

public class BlockBeatsClient implements ClientModInitializer {
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
