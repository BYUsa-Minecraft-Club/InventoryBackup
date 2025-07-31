package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class TypedBackupListGui extends PagedGui {

    private final UUID targetUUID;

    private final String playerName;

    private final LogType logType;

    private int ticker = 0;

    private List<PlayerBackupData> data;


    public TypedBackupListGui(UUID uuid, String playerName, LogType logType, ServerPlayerEntity player) {
        super(player);
        this.targetUUID = uuid;
        this.playerName = playerName;
        this.logType = logType;
        String title = logType == null ? String.format("All of %s's Backups", playerName) :
                String.format("%s's %s Backups", playerName, GuiUtils.readableLogType(logType));
        this.setTitle(Text.of(title));
        this.data = getData();
        this.updateDisplay();
    }


    @Override
    protected int getPageAmount() {
        return this.data.size() / PAGE_SIZE + 1;
    }


    @Override
    protected GuiSlot getElement(int id) {
        if (id < this.data.size()) {
            PlayerBackupData backupData = data.get(id);
            String[] title = GuiUtils.getBackupDetails(playerName, backupData);

            Item displayItem = GuiConfig.logTypeItem(backupData.logType());
            var element = GuiSlot.builder(displayItem, title).setCallback(
                    (index, type, action) -> new BackupGui(targetUUID, playerName, backupData, this, player).open());

            return GuiSlot.of(element);
        }

        return GuiSlot.empty();
    }


    @Override
    protected GuiSlot getNavElement(int id) {
        return switch (id) {
            case 0 -> GuiSlot.back(() -> new PlayerBackupListGui(targetUUID, playerName, player).open());
            case 2 -> GuiSlot.previousPage(this);
            case 6 -> GuiSlot.nextPage(this);
            default -> GuiSlot.empty();
        };
    }


    @Override
    public void onTick() {
        this.ticker++;
        if (this.ticker % 100 == 0) {
            this.data = getData();
            this.updateDisplay();
        }
        super.onTick();
    }


    private List<PlayerBackupData> getData() {
        List<PlayerBackupData> ret;
        if (logType == null) {
            ret = new ArrayList<>();
            for (var hi : InventoryBackup.data.getData().get(targetUUID).entrySet()) {
                ret.addAll(hi.getValue());
            }
        } else {
            ret = InventoryBackup.data.getData().get(targetUUID).get(logType);
        }
        ret.sort(Comparator.comparing(PlayerBackupData::timestamp));
        return ret;
    }

}
