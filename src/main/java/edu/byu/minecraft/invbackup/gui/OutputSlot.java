package edu.byu.minecraft.invbackup.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class OutputSlot extends Slot {

    public OutputSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        return false;
    }

    @Override
    public ItemStack takeStack(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakePartial(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
