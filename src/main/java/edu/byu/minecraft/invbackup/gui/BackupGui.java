package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
        this.setTitle(Text.of(String.format("%s's, %s backup", playerName, playerBackupData.getLogType())));
        this.previousUi = previousUi;
        this.updateDisplay();
    }


    @Override
    protected int getPageAmount() {
        return 2;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if(id < 4) {
            return GuiSlot.of(playerBackupData.getArmor().get(3 - id)); //3 - id -> helmet first
        }
        else if(id == 8) {
            return GuiSlot.of(playerBackupData.getOffHand().getFirst());
        }
        else if(id < 9) {
            return GuiSlot.empty();
        }
        else if(id < 36) {
            return GuiSlot.of(playerBackupData.getMain().get(id));
        }
        else if (id < 45) {
            return GuiSlot.of(playerBackupData.getMain().get(id - 36));
        }
        else if (id < 72) {
            return GuiSlot.of(playerBackupData.getEnderChest().get(id - 45));
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
