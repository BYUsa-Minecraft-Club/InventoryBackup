package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

import java.text.SimpleDateFormat;
import java.util.*;

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
        String logTypeStr = (logType == null) ? "ALL" : logType.name();
        this.setTitle(WrappedText.of(String.format("%s's, %s backups", playerName, logTypeStr)).text());
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
            String[] title = {String.format("%s's %s", playerName, backupData.getLogType()),
                    new SimpleDateFormat("MM-dd-yyyy 'at' HH:mm:ss z").format(new Date(backupData.getTimestamp())),
                    String.format("World: %s", backupData.getWorld().toString()),
                    String.format("Location: %d %d %d", Math.round(backupData.getPos().getX()),
                            Math.round(backupData.getPos().getY()), Math.round(backupData.getPos().getZ())),
                    (backupData.getDeathReason() == null) ? "" : backupData.getDeathReason()};
            Item displayItem = switch (backupData.getLogType()) {
                case JOIN -> Items.GREEN_BED;
                case QUIT -> Items.FIREWORK_ROCKET;
                case DEATH -> Items.IRON_SWORD;
                case WORLD_CHANGE -> Items.END_PORTAL_FRAME;
                case FORCE -> Items.STRUCTURE_VOID;
            };

            var element = IconData.of(displayItem, title).builder(new HashMap<>()).setCallback(
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
        ret.sort(Comparator.comparing(PlayerBackupData::getTimestamp));
        return ret;
    }

}
