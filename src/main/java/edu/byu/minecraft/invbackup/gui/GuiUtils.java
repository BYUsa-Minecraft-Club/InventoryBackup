package edu.byu.minecraft.invbackup.gui;

import com.mojang.authlib.GameProfile;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class GuiUtils {
    static ItemStack getPlayerHead(UUID uuid, String playerName) {
        ItemStack playerHead = Items.PLAYER_HEAD.getDefaultStack();
        GameProfile profile = new GameProfile(uuid, playerName);
        ProfileComponent pc = new ProfileComponent(Optional.empty(), Optional.of(uuid), profile.getProperties());
        playerHead.set(DataComponentTypes.PROFILE, pc);
        return playerHead;
    }

    static String[] getBackupDetails(String playerName, PlayerBackupData backupData) {
        String[] backupDetails = new String[backupData.deathReason().isPresent() ? 5 : 4];
        backupDetails[0] = String.format("%s's %s Backup", playerName, readableLogType(backupData.logType()));
        backupDetails[1] = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss z").format(new Date(backupData.timestamp()));
        backupDetails[2] = String.format("World: %s", backupData.world().toString());
        backupDetails[3] = String.format("Location: %d %d %d", Math.round(backupData.pos().getX()),
                        Math.round(backupData.pos().getY()), Math.round(backupData.pos().getZ()));
        if(backupData.deathReason().isPresent()) {
            backupDetails[4] = backupData.deathReason().get();
        }
        return backupDetails;
    }

    static String readableLogType(LogType logType) {
        String replaced = logType.name().replace("_", " ");
        return Character.toUpperCase(replaced.charAt(0)) + replaced.substring(1).toLowerCase();
    }

    static ItemStack getShadowstack(int elementId) {
        return switch (elementId) {
            case 0 -> GuiConfig.EMPTY_HELMET_SLOT;
            case 1 -> GuiConfig.EMPTY_CHESTPLATE_SLOT;
            case 2 -> GuiConfig.EMPTY_LEGGINGS_SLOT;
            case 3 -> GuiConfig.EMPTY_BOOTS_SLOT;
            case 8 -> GuiConfig.EMPTY_OFFHAND_SLOT;
            default -> throw new IllegalStateException("Unexpected value: " + elementId);
        };
    }
}
