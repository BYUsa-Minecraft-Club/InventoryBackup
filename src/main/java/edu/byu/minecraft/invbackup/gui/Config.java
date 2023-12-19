package edu.byu.minecraft.invbackup.gui;

import net.minecraft.item.Items;

public class Config {

    public static Variant<IconData> previousButton =
            Variant.of(IconData.of(Items.ARROW, "Previous Page"),
                    IconData.of(Items.AIR, "Previous Page"));


    public static Variant<IconData> nextButton =
            Variant.of(IconData.of(Items.ARROW, "Next Page"),
                    IconData.of(Items.AIR, "Next Page"));


    public static IconData backButton = IconData.of(Items.BARRIER, "Previous Menu");

    public static IconData teleportButton = IconData.of(Items.ENDER_PEARL, "Teleport to Event");

    public static IconData restoreButton = IconData.of(Items.LIME_DYE, "Restore Inventory");

    public static IconData restoreOfflineButton = IconData.of(Items.LIGHT_GRAY_DYE, "Player Offline");


}
