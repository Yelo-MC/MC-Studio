package com.yelo.blockbeats.screen;

import com.yelo.blockbeats.blockentity.DawBlockEntity;
import com.yelo.blockbeats.item.ModItems;
import com.yelo.blockbeats.networking.OpenDawS2CPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DawMenu extends AbstractContainerMenu {
    public static final int BLANK_DISC_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;

    private final Container dawInventory;
    private final ContainerData dawData;
    private final Inventory playerInventory;
    private final BlockPos blockPos;
    private final long[] initialNotes;
    private final int initialBpm;
    private final String initialSongName;
    private final int initialLoopPoint;

    public DawMenu(int containerId, Inventory playerInventory, OpenDawS2CPayload data) {
        this(containerId, playerInventory, new SimpleContainer(2), new SimpleContainerData(1),
                data.blockPos(), data.notes(), data.bpm(), data.songName(), data.loopPoint());
    }

    public DawMenu(int containerId, Inventory playerInventory, DawBlockEntity daw) {
        this(containerId, playerInventory, daw, daw.getMenuData(), daw.getBlockPos(), daw.getPackedNotes(),
                daw.getBpm(), daw.getSongName(), daw.getLoopPoint());
    }

    private DawMenu(int containerId, Inventory playerInventory, Container inventory, ContainerData data,
                    BlockPos blockPos, long[] initialNotes, int initialBpm, String initialSongName,
                    int initialLoopPoint) {
        super(ModMenus.DAW_MENU, containerId);
        checkContainerSize(inventory, 2);
        checkContainerDataCount(data, 1);
        this.dawInventory = inventory;
        this.dawData = data;
        this.playerInventory = playerInventory;
        this.blockPos = blockPos;
        this.initialNotes = initialNotes.clone();
        this.initialBpm = initialBpm;
        this.initialSongName = initialSongName;
        this.initialLoopPoint = initialLoopPoint;

        inventory.startOpen(playerInventory.player);
        this.addSlot(new Slot(inventory, BLANK_DISC_SLOT, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.BLANK_DISC);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public void setByPlayer(ItemStack stack, ItemStack previousStack) {
                super.setByPlayer(stack, previousStack);
                if (!stack.isEmpty() && DawMenu.this.dawInventory instanceof DawBlockEntity daw) {
                    daw.setRecordingPlayer(playerInventory.player.getName().getString());
                }
            }
        });
        this.addSlot(new Slot(inventory, OUTPUT_SLOT, 0, 0) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        for (int hotbarSlot = 0; hotbarSlot < 9; hotbarSlot++) {
            this.addSlot(new Slot(playerInventory, hotbarSlot, 0, 0));
        }
        this.addDataSlots(data);
    }

    public BlockPos blockPos() {
        return this.blockPos;
    }

    public long[] initialNotes() {
        return this.initialNotes.clone();
    }

    public int initialBpm() {
        return this.initialBpm;
    }

    public String initialSongName() { return this.initialSongName; }

    public int initialLoopPoint() { return this.initialLoopPoint; }

    public int recordingProgress() {
        return this.dawData.get(0);
    }

    public Container dawInventory() {
        return this.dawInventory;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId == BLANK_DISC_SLOT
                && clickType == ClickType.PICKUP
                && this.getCarried().isEmpty()
                && this.getSlot(slotId).getItem().isEmpty()) {
            for (int index = 0; index < this.playerInventory.getContainerSize(); index++) {
                ItemStack candidate = this.playerInventory.getItem(index);
                if (this.getSlot(slotId).mayPlace(candidate)) {
                    this.getSlot(slotId).setByPlayer(candidate.split(1));
                    this.broadcastChanges();
                    return;
                }
            }
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.getSlot(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (slotIndex < 2) {
            moved = this.moveItemStackTo(stack, 2, 11, true);
        } else if (stack.is(ModItems.BLANK_DISC)) {
            moved = this.moveItemStackTo(stack, BLANK_DISC_SLOT, BLANK_DISC_SLOT + 1, false);
        } else {
            return ItemStack.EMPTY;
        }
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.dawInventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.dawInventory.stopOpen(player);
    }
}
