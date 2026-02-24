package com.yelo.blockbeats;

import com.yelo.blockbeats.networking.OpenDawS2CPayload;
import com.yelo.blockbeats.screen.DawScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;

public class BlockBeatsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(OpenDawS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new DawScreen());
            });
        });
    }
}
