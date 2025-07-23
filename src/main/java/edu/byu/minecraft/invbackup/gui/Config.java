package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.invbackup.data.LogType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

public class Config {

    public static GuiElementBuilder previousButton = GuiSlot.builder(Items.ARROW, "Previous Page");

    public static GuiElementBuilder nextButton = GuiSlot.builder(Items.ARROW, "Next Page");

    public static GuiElementBuilder backButton = GuiSlot.builder(Items.BARRIER, "Previous Menu");

    public static GuiElementBuilder teleportButton(String target) {
        return GuiSlot.builder(Items.ENDER_PEARL, "Teleport to " + target);
    }

    public static GuiElementBuilder restoreButton = GuiSlot.builder(Items.LIME_DYE, "Restore Inventory");

    public static GuiElementBuilder restoreOfflineButton = GuiSlot.builder(Items.LIGHT_GRAY_DYE, "Player Offline");

    public static Item logTypeItem(LogType type) {
        return switch (type) {
            case JOIN -> Items.GREEN_BED;
            case QUIT -> Items.FIREWORK_ROCKET;
            case DEATH -> Items.IRON_SWORD;
            case WORLD_CHANGE -> Items.END_PORTAL_FRAME;
            case FORCE -> Items.STRUCTURE_VOID;
        };
    }
}
