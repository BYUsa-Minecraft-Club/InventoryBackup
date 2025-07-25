package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerBackupListGui extends PagedGui {
    private final UUID targetUUID;
    private final String playerName;
    private int ticker = 0;
    private List<LogType> types;

    public PlayerBackupListGui(UUID uuid, String playerName, ServerPlayerEntity player) {
        super(player);
        this.targetUUID = uuid;
        this.playerName = playerName;
        this.setTitle(Text.of(playerName + "'s backups"));
        this.types = getLogTypes();
        this.updateDisplay();
    }

    @Override
    protected int getPageAmount() {
        return 1;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if (id < this.types.size()) {
            LogType logType = types.get(id);

            var element = GuiSlot.builder(Config.logTypeItem(logType), GuiUtils.readableLogType(logType) + " Backups")
                    .setCallback((index, type, action) -> new TypedBackupListGui(targetUUID, playerName, logType, player).open());

            return GuiSlot.of(element);
        }
        else if (id == 9) {
            return GuiSlot.of(GuiSlot.builder(GuiUtils.getPlayerHead(targetUUID, playerName), "Live Inventory")
                    .setCallback((index, type, action) -> new LiveInventoryGui(playerName, this, player).open()));
        }
        else if (id == 10) {
            var element = GuiSlot.builder(Items.CHEST, "All Backups")
                    .setCallback((index, type, action) -> new TypedBackupListGui(targetUUID, playerName, null, player).open());

            return GuiSlot.of(element);
        }

        return GuiSlot.empty();
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        if(id == 0) {
            return GuiSlot.back(() -> new AllBackupListGui(player).open());
        }
        return GuiSlot.empty();
    }

    @Override
    public void onTick() {
        this.ticker++;
        if (this.ticker % 100 == 0) {
            this.types = getLogTypes();
            this.updateDisplay();
        }
        super.onTick();
    }

    private List<LogType> getLogTypes() {
        ServerPlayerEntity targetPlayer = InventoryBackup.getPlayer(playerName, player.getServer());
        return new ArrayList<>(((PlayerBackupHolder) targetPlayer).inventoryBackup$getPlayerBackups().keySet());
    }
}
