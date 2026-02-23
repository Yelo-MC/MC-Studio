package com.yelo.blockbeats.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DawBlockEntity extends BlockEntity {
    public DawBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.DAW_BLOCK_ENTITY, blockPos, blockState);
    }
}
