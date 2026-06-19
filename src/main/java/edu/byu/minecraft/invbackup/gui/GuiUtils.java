package edu.byu.minecraft.invbackup.gui;

import com.mojang.authlib.GameProfile;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class GuiUtils {
    static ItemStack getPlayerHead(UUID uuid, String playerName) {
        ItemStack playerHead = Items.PLAYER_HEAD.getDefaultInstance();
        GameProfile profile = new GameProfile(uuid, playerName);
        ResolvableProfile pc = ResolvableProfile.createUnresolved(uuid);
        playerHead.set(DataComponents.PROFILE, pc);
        return playerHead;
    }

    static String[] getBackupDetails(String playerName, PlayerBackupData backupData) {
        String[] backupDetails = new String[backupData.deathReason().isPresent() ? 5 : 4];
        backupDetails[0] = String.format("%s's %s Backup", playerName, readableLogType(backupData.logType()));
        backupDetails[1] = new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss z").format(new Date(backupData.timestamp()));
        backupDetails[2] = String.format("World: %s", backupData.world().toString());
        backupDetails[3] = String.format("Location: %d %d %d", Math.round(backupData.pos().x()),
                        Math.round(backupData.pos().y()), Math.round(backupData.pos().z()));
        if(backupData.deathReason().isPresent()) {
            backupDetails[4] = backupData.deathReason().get();
        }
        return backupDetails;
    }

    static String readableLogType(LogType logType) {
        String replaced = logType.name().replace("_", " ");
        return Character.toUpperCase(replaced.charAt(0)) + replaced.substring(1).toLowerCase();
    }
}
