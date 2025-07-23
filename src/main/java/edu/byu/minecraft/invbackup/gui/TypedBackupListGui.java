package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        String title = logType == null ? String.format("All of %s's backups", playerName) :
                String.format("%s's %s backups", playerName, GuiUtils.readableLogType(logType));
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

            Item displayItem = Config.logTypeItem(backupData.logType());
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
        ServerPlayerEntity targetPlayer = InventoryBackup.getPlayer(playerName, Objects.requireNonNull(player.getServer()));
        EnumMap<LogType, List<PlayerBackupData>> backups = ((PlayerBackupHolder) targetPlayer).getPlayerBackups();

        List<PlayerBackupData> backupList = (logType != null) ? backups.get(logType) :
                backups.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        backupList.sort(Comparator.comparing(PlayerBackupData::timestamp));
        return backupList;
    }

}
