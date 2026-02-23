package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.BlockBeats;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.core.ParentUIComponent;
import net.minecraft.resources.Identifier;

import java.awt.*;

public class DawScreen extends BaseUIModelScreen {
    protected DawScreen() {
        super(FlowLayout.class,
                DataSource.asset(Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "daw_screen")));
    }

    @Override
    protected void build(ParentUIComponent parentUIComponent) {

    }
}
