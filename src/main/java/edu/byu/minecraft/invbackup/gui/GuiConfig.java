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

}
