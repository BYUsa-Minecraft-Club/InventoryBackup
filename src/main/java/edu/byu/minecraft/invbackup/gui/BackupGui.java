package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.GuiHelpers;
import eu.pb4.sgui.api.gui.GuiInterface;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;

import java.util.Objects;
import java.util.UUID;

public class BackupGui extends PagedGui {
    private final UUID targetUUID;
    private final String playerName;
    private final PlayerBackupData playerBackupData;
    private final GuiInterface previousUi;


    public BackupGui(UUID uuid, String playerName, PlayerBackupData playerBackupData, GuiInterface previousUi, ServerPlayerEntity player) {
        super(player);
        this.targetUUID = uuid;
        this.playerName = playerName;
        this.playerBackupData = playerBackupData;
        this.setTitle(WrappedText.of(String.format("%s's, %s backup", playerName, playerBackupData.getLogType())).text());
        this.previousUi = previousUi;
        this.updateDisplay();
    }


    @Override
    protected int getPageAmount() {
        return 2;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if(id < 27) {
            return GuiSlot.of(new OutputSlot(playerBackupData.getMainInventory(), id + 9, 0, 0));
        }
        else if (id < 36) {
            return GuiSlot.of(new OutputSlot(playerBackupData.getMainInventory(), id - 27, 0, 0));
        }
        else if (id < 42) {
            return GuiSlot.of(new OutputSlot(playerBackupData.getMainInventory(), id, 0, 0));
        }
        else if (id > 44 && id < 72) {
            return GuiSlot.of(new OutputSlot(playerBackupData.getEnderChest(), id - 45, 0, 0));
        }
        return GuiSlot.empty();
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        return switch (id) {
            case 0 -> GuiSlot.back(previousUi::open);
            case 2 -> GuiSlot.previousPage(this);
            case 4 -> GuiSlot.restore(Objects.requireNonNull(player.getServer()), playerBackupData);
            case 6 -> GuiSlot.nextPage(this);
            case 8 -> GuiSlot.teleport(player, playerBackupData.getWorld(), playerBackupData.getPos());
            default ->  GuiSlot.empty();
        };
    }
}
