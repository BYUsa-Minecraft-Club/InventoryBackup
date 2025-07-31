package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.invbackup.data.LogType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

class GuiConfig {

    static final GuiElementBuilder PREVIOUS_PAGE_BUTTON = GuiSlot.builder(Items.ARROW, "Previous Page");

    static final GuiElementBuilder NEXT_PAGE_BUTTON = GuiSlot.builder(Items.ARROW, "Next Page");

    static final GuiElementBuilder PREVIOUS_MENU_BUTTON = GuiSlot.builder(Items.BARRIER, "Previous Menu");

    static final GuiElementBuilder RESTORE_INVENTORY_BUTTON = GuiSlot.builder(Items.LIME_DYE, "Restore Inventory");

    static final ItemStack EMPTY_HELMET_SLOT;

    static final ItemStack EMPTY_CHESTPLATE_SLOT;

    static final ItemStack EMPTY_LEGGINGS_SLOT;

    static final ItemStack EMPTY_BOOTS_SLOT;

    static final ItemStack EMPTY_OFFHAND_SLOT;

    static GuiElementBuilder teleportButton(String target) {
        return GuiSlot.builder(Items.ENDER_PEARL, "Teleport to " + target);
    }

    static Item logTypeItem(LogType type) {
        return switch (type) {
            case JOIN -> Items.BLUE_BED;
            case QUIT -> Items.FIREWORK_ROCKET;
            case DEATH -> Items.SKELETON_SKULL;
            case WORLD_CHANGE -> Items.END_PORTAL_FRAME;
            case FORCE -> Items.STRUCTURE_VOID;
        };
    }

    static {
        int byuRoyalBlue = 18362;
        EMPTY_HELMET_SLOT = new ItemStack(Items.LEATHER_HELMET);
        EMPTY_HELMET_SLOT.set(DataComponentTypes.ITEM_NAME, Text.of("Empty Helmet Slot"));
        EMPTY_HELMET_SLOT.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(byuRoyalBlue));
        EMPTY_CHESTPLATE_SLOT = new ItemStack(Items.LEATHER_CHESTPLATE);
        EMPTY_CHESTPLATE_SLOT.set(DataComponentTypes.ITEM_NAME, Text.of("Empty Chestplate Slot"));
        EMPTY_CHESTPLATE_SLOT.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(byuRoyalBlue));
        EMPTY_LEGGINGS_SLOT = new ItemStack(Items.LEATHER_LEGGINGS);
        EMPTY_LEGGINGS_SLOT.set(DataComponentTypes.ITEM_NAME, Text.of("Empty Leggings Slot"));
        EMPTY_LEGGINGS_SLOT.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(byuRoyalBlue));
        EMPTY_BOOTS_SLOT = new ItemStack(Items.LEATHER_BOOTS);
        EMPTY_BOOTS_SLOT.set(DataComponentTypes.ITEM_NAME, Text.of("Empty Boots Slot"));
        EMPTY_BOOTS_SLOT.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(byuRoyalBlue));

        EMPTY_OFFHAND_SLOT = new ItemStack(Items.SHIELD);
        EMPTY_OFFHAND_SLOT.set(DataComponentTypes.BASE_COLOR, DyeColor.BLUE);
        EMPTY_OFFHAND_SLOT.set(DataComponentTypes.CUSTOM_NAME, Text.of("Empty Offhand Slot"));
    }

}
