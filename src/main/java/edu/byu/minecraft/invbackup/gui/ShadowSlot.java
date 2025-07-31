package edu.byu.minecraft.invbackup.gui;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class ShadowSlot extends Slot {
    private final ItemStack backupItem;

    public ShadowSlot(Inventory inventory, int index, int x, int y, ItemStack backupItem) {
        super(inventory, index, x, y);
        this.backupItem = backupItem;
    }

    private boolean showBackupItem() {
        ItemStack stack = inventory.getStack(getIndex());
        return stack == null || stack.isEmpty();
    }

    @Override
    public ItemStack getStack() {
        ItemStack stack = super.getStack();
        if (stack == null || stack.isEmpty()) {
            return backupItem;
        }
        return stack;
    }



}
