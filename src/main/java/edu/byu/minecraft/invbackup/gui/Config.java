package edu.byu.minecraft.invbackup.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.Items;

public class Config {

    public static GuiElementBuilder previousButton = GuiSlot.builder(Items.ARROW, "Previous Page");

    public static GuiElementBuilder nextButton = GuiSlot.builder(Items.ARROW, "Next Page");

    public static GuiElementBuilder backButton = GuiSlot.builder(Items.BARRIER, "Previous Menu");

    public static GuiElementBuilder teleportButton = GuiSlot.builder(Items.ENDER_PEARL, "Teleport to Event");

    public static GuiElementBuilder restoreButton = GuiSlot.builder(Items.LIME_DYE, "Restore Inventory");

    public static GuiElementBuilder restoreOfflineButton = GuiSlot.builder(Items.LIGHT_GRAY_DYE, "Player Offline");


}
