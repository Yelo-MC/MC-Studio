package com.yelo.mcstudio;

import com.yelo.mcstudio.block.ModBlocks;
import com.yelo.mcstudio.blockentity.ModBlockEntities;
import com.yelo.mcstudio.item.ModItems;
import com.yelo.mcstudio.networking.OpenDawS2CPayload;
import com.yelo.mcstudio.networking.BurnedDiscPlaybackS2CPayload;
import com.yelo.mcstudio.networking.SetDawNoteC2SPayload;
import com.yelo.mcstudio.networking.SetDawBpmC2SPayload;
import com.yelo.mcstudio.networking.SetDawSongNameC2SPayload;
import com.yelo.mcstudio.networking.SetDawLoopPointC2SPayload;
import com.yelo.mcstudio.screen.ModMenus;
import com.yelo.mcstudio.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import com.yelo.mcstudio.blockentity.DawBlockEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MCStudio implements ModInitializer {
	public static final String MOD_ID = "mcstudio";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        ModSounds.init();
        ModItems.init();
        ModMenus.init();
        PayloadTypeRegistry.playC2S().register(SetDawNoteC2SPayload.ID, SetDawNoteC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDawBpmC2SPayload.ID, SetDawBpmC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDawSongNameC2SPayload.ID, SetDawSongNameC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetDawLoopPointC2SPayload.ID, SetDawLoopPointC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(BurnedDiscPlaybackS2CPayload.ID, BurnedDiscPlaybackS2CPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetDawNoteC2SPayload.ID, (payload, context) -> {
            var player = context.player();
            var pos = payload.blockPos();

            if (!DawBlockEntity.isValidInstrument(payload.instrument())) return;
            if (!DawBlockEntity.isValidNote(payload.step(), payload.pitch())) return;
            if (player.distanceToSqr(pos.getCenter()) > 64.0) return;

            if (player.level().getBlockEntity(pos) instanceof DawBlockEntity daw) {
                daw.setNote(payload.instrument(), payload.step(), payload.pitch(), payload.enabled());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetDawBpmC2SPayload.ID, (payload, context) -> {
            var player = context.player();
            var pos = payload.blockPos();
            if (payload.bpm() < 40 || payload.bpm() > 240) return;
            if (player.distanceToSqr(pos.getCenter()) > 64.0) return;

            if (player.level().getBlockEntity(pos) instanceof DawBlockEntity daw) {
                daw.setBpm(payload.bpm());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetDawSongNameC2SPayload.ID, (payload, context) -> {
            var player = context.player();
            var pos = payload.blockPos();
            if (payload.songName().length() > DawBlockEntity.MAX_SONG_NAME_LENGTH) return;
            if (player.distanceToSqr(pos.getCenter()) > 64.0) return;

            if (player.level().getBlockEntity(pos) instanceof DawBlockEntity daw) {
                daw.setSongName(payload.songName());
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(SetDawLoopPointC2SPayload.ID, (payload, context) -> {
            var player = context.player();
            var pos = payload.blockPos();
            if (payload.loopPoint() < 1 || payload.loopPoint() > DawBlockEntity.STEP_COUNT) return;
            if (player.distanceToSqr(pos.getCenter()) > 64.0) return;

            if (player.level().getBlockEntity(pos) instanceof DawBlockEntity daw) {
                daw.setLoopPoint(payload.loopPoint());
            }
        });

    }
}
