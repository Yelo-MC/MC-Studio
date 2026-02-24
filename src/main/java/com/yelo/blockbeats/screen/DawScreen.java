package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.BlockBeats;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.resources.Identifier;

public class DawScreen extends BaseUIModelScreen<FlowLayout> {


    public DawScreen() {
        super(FlowLayout.class,
                DataSource.asset(Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "daw_screen")));
    }

    @Override
    protected void build(FlowLayout flowLayout) {

    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
