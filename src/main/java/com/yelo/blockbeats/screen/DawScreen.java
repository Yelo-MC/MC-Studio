package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.BlockBeats;
import com.yelo.blockbeats.ModKeyBindings;
import com.yelo.blockbeats.blockentity.DawBlockEntity;
import com.yelo.blockbeats.networking.SetDawBpmC2SPayload;
import com.yelo.blockbeats.networking.SetDawSongNameC2SPayload;
import io.wispforest.owo.ui.base.BaseUIModelContainerScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.DiscreteSliderComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class DawScreen extends BaseUIModelContainerScreen<FlowLayout, DawMenu> {
    private static final String[] INSTRUMENT_NAMES = {
            "Harp", "Bass", "Bell", "Pling",
            "Bass Drum", "Snare", "Hi-Hat", "Flute",
            "Guitar", "Chime", "Xylophone", "Iron Xylophone",
            "Cow Bell", "Didgeridoo", "Bit", "Banjo"
    };
    private static final Identifier DISC_SLOT_SPRITE =
            Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "textures/gui/disc_slot.png");
    private static final Identifier PAUSE_TEXTURE =
            Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "textures/gui/pause.png");
    private static final Identifier LOOP_TEXTURE =
            Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "textures/gui/loop.png");
    private static final Identifier LOOP_ON_TEXTURE =
            Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "textures/gui/loop_on.png");

    private final BlockPos blockPos;
    private final long[] packedNotes;
    private PianoRollComponent pianoRoll;
    private ScrollContainer<?> pianoRollScroll;
    private FlowLayout recordingControls;
    private StackLayout transport;
    private boolean recordingControlsAligned;
    private DiscreteSliderComponent bpmSlider;
    private TextureButtonComponent playButton;
    private TextureButtonComponent loopButton;
    private TextBoxComponent songNameField;
    private final InstrumentButtonComponent[] instrumentButtons = new InstrumentButtonComponent[DawBlockEntity.INSTRUMENT_COUNT];
    private boolean playing;
    private boolean stopped = true;
    private int bpm;
    private int nextStep;
    private long nextStepAt;
    private boolean loopEditing;

    public DawScreen(DawMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, FlowLayout.class,
                BaseUIModelScreen.DataSource.asset(Identifier.fromNamespaceAndPath(BlockBeats.MOD_ID, "daw_screen")));
        this.blockPos = menu.blockPos();
        this.packedNotes = menu.initialNotes();
        this.bpm = menu.initialBpm();
    }

    @Override
    protected void build(FlowLayout root) {
        this.pianoRoll = new PianoRollComponent(this.blockPos, this.packedNotes, this.menu.initialLoopPoint());

        FlowLayout bpmControls = root.childById(FlowLayout.class, "bpm-controls");
        FlowLayout playbackControls = root.childById(FlowLayout.class, "playback-controls");
        this.recordingControls = root.childById(FlowLayout.class, "recording-controls");
        this.transport = root.childById(StackLayout.class, "transport");
        FlowLayout songNameControls = root.childById(FlowLayout.class, "song-name-controls");
        FlowLayout hotbar = root.childById(FlowLayout.class, "hotbar");

        this.playButton = new TextureButtonComponent(Component.literal("▶"), null, button -> this.togglePlayback());
        this.playButton.sizing(Sizing.fixed(20));
        this.playButton.tooltip(Component.literal("Play / Pause"));
        playbackControls.child(this.playButton);

        var stopButton = UIComponents.button(Component.literal("■"), button -> this.stopPlayback());
        stopButton.sizing(Sizing.fixed(20));
        stopButton.tooltip(Component.literal("Stop"));
        playbackControls.child(stopButton);

        this.loopButton = new TextureButtonComponent(Component.empty(), LOOP_TEXTURE, button -> this.toggleLoopEditing());
        this.loopButton.sizing(Sizing.fixed(20));
        this.loopButton.tooltip(Component.literal("Set loop point"));
        playbackControls.child(this.loopButton);

        this.bpmSlider = UIComponents.discreteSlider(Sizing.fixed(110), 40, 240);
        this.bpmSlider.setFromDiscreteValue(this.bpm);
        this.bpmSlider.message(value -> Component.literal(value + " BPM"));
        this.bpmSlider.onChanged().subscribe(value -> this.setBpm((int) value));
        bpmControls.child(this.bpmSlider);

        this.songNameField = UIComponents.textBox(Sizing.fixed(130), this.menu.initialSongName());
        this.songNameField.setMaxLength(DawBlockEntity.MAX_SONG_NAME_LENGTH);
        this.songNameField.setHint(Component.literal("New Song").withColor(0x707070));
        this.songNameField.onChanged().subscribe(value ->
                ClientPlayNetworking.send(new SetDawSongNameC2SPayload(this.blockPos, value)));
        songNameControls.child(this.songNameField);

        this.recordingControls.child(this.recordingSlot(DawMenu.BLANK_DISC_SLOT, DISC_SLOT_SPRITE));
        this.recordingControls.child(new RecordingArrowComponent(this.menu));
        this.recordingControls.child(this.recordingSlot(DawMenu.OUTPUT_SLOT, null));
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            hotbar.child(this.recordingSlot(2 + hotbarSlot, null));
        }

        FlowLayout firstInstrumentColumn = root.childById(FlowLayout.class, "instrument-column-1");
        FlowLayout secondInstrumentColumn = root.childById(FlowLayout.class, "instrument-column-2");
        for (int instrument = 0; instrument < DawBlockEntity.INSTRUMENT_COUNT; instrument++) {
            this.instrumentButtons[instrument] = new InstrumentButtonComponent(
                    instrument,
                    INSTRUMENT_NAMES[instrument],
                    this::selectInstrument
            );
            (instrument < DawBlockEntity.INSTRUMENT_COUNT / 2 ? firstInstrumentColumn : secondInstrumentColumn)
                    .child(this.instrumentButtons[instrument]);
        }
        this.updateInstrumentButtons(0);

        root.childById(FlowLayout.class, "piano-roll-panel").child(this.pianoRoll);
        root.childById(StackLayout.class, "piano-roll-stack")
                .child(new PianoKeysComponent(this.pianoRoll::previewPitch)
                        .positioning(Positioning.absolute(2, 1)))
                .child(new PianoRollInsetComponent().margins(Insets.horizontal(1)));

        this.pianoRollScroll = root.childById(ScrollContainer.class, "piano-roll-scroll");

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }

    private StackLayout recordingSlot(int slotIndex, Identifier emptySlotSprite) {
        return UIContainers.stack(Sizing.fixed(18), Sizing.fixed(18))
                .child(new RecordingSlotBackgroundComponent(
                        emptySlotSprite,
                        () -> this.menu.getSlot(slotIndex).getItem().isEmpty()
                ))
                .child(this.slotAsComponent(slotIndex).margins(Insets.of(1)));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.recordingControlsAligned && this.pianoRollScroll != null && this.pianoRollScroll.width() > 0) {
            int pianoRollRight = this.pianoRollScroll.x() + this.pianoRollScroll.width() - 1;
            int localX = pianoRollRight - this.transport.x() - this.recordingControls.width();
            int localY = (this.transport.height() - this.recordingControls.height()) / 2;
            this.recordingControls.positioning(Positioning.absolute(localX, localY));
            this.recordingControlsAligned = true;
        }
        this.advancePlayback();
        graphics.fill(0, 0, this.width, this.height, 0xB0000000);
        super.render(graphics, mouseX, mouseY, partialTick);

        if (this.bpmSlider != null && this.bpmSlider.isInBoundingBox(mouseX, mouseY)) {
            this.uiAdapter.cursorAdapter.applyStyle(CursorStyle.HAND);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (this.songNameField != null && this.songNameField.isFocused()) {
            this.songNameField.keyPressed(input);
            return true;
        }
        if (ModKeyBindings.PLAY_PAUSE.matches(input)) {
            this.togglePlayback();
            return true;
        }
        if (ModKeyBindings.STOP.matches(input)) {
            this.stopPlayback();
            return true;
        }
        if (ModKeyBindings.LOOP_MODE.matches(input)) {
            this.toggleLoopEditing();
            return true;
        }
        return super.keyPressed(input);
    }

    private void advancePlayback() {
        if (!this.playing) return;

        long now = System.nanoTime();
        while (now >= this.nextStepAt) {
            if (this.nextStep >= this.pianoRoll.loopPoint()) this.nextStep = 0;
            this.pianoRoll.playStep(this.nextStep);
            this.nextStep++;
            if (this.nextStep >= this.pianoRoll.loopPoint()) this.nextStep = 0;
            this.nextStepAt += this.stepLengthNanos();
        }
    }

    private void togglePlayback() {
        if (this.playing) {
            this.pausePlayback();
        } else {
            if (this.stopped) {
                this.nextStep = 0;
                this.pianoRoll.stop();
            }
            this.stopped = false;
            this.playing = true;
            this.nextStepAt = System.nanoTime();
            this.playButton.setMessage(Component.empty());
            this.playButton.icon(PAUSE_TEXTURE);
        }
    }

    private void pausePlayback() {
        this.playing = false;
        this.playButton.setMessage(Component.literal("▶"));
        this.playButton.icon(null);
    }

    private void stopPlayback() {
        this.pausePlayback();
        this.stopped = true;
        this.nextStep = 0;
        this.pianoRoll.stop();
    }

    private void toggleLoopEditing() {
        this.loopEditing = !this.loopEditing;
        this.pianoRoll.loopEditing(this.loopEditing);
        this.loopButton.icon(this.loopEditing ? LOOP_ON_TEXTURE : LOOP_TEXTURE);
    }

    private void selectInstrument(int instrument) {
        this.pianoRoll.selectInstrument(instrument);
        this.pianoRoll.previewInstrument(instrument);
        this.updateInstrumentButtons(instrument);
    }

    private void updateInstrumentButtons(int selectedInstrument) {
        for (int instrument = 0; instrument < this.instrumentButtons.length; instrument++) {
            this.instrumentButtons[instrument].selected(instrument == selectedInstrument);
        }
    }

    private void setBpm(int bpm) {
        if (this.bpm == bpm) return;
        this.bpm = bpm;
        ClientPlayNetworking.send(new SetDawBpmC2SPayload(this.blockPos, bpm));
        if (this.playing) this.nextStepAt = System.nanoTime() + this.stepLengthNanos();
    }

    private long stepLengthNanos() {
        return 15_000_000_000L / this.bpm;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
