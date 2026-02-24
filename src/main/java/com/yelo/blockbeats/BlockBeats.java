package com.yelo.blockbeats;

import com.yelo.blockbeats.block.ModBlocks;
import com.yelo.blockbeats.blockentity.ModBlockEntities;
import com.yelo.blockbeats.networking.OpenDawS2CPayload;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockBeats implements ModInitializer {
	public static final String MOD_ID = "blockbeats";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModBlocks.init();
        ModBlockEntities.init();
        PayloadTypeRegistry.playS2C().register(OpenDawS2CPayload.ID, OpenDawS2CPayload.CODEC);

    }
}