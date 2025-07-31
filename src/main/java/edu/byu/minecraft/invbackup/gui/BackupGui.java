package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
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
        this.playerBackupData = PlayerBackupData.copy(playerBackupData);
        this.setTitle(Text.of(String.format("%s's %s Backup", playerName, GuiUtils.readableLogType(playerBackupData.logType()))));
        this.previousUi = previousUi;
        this.updateDisplay();
    }


    @Override
    protected int getPageAmount() {
        return 2;
    }

    @Override
    protected GuiSlot getElement(int id) {
        ItemStack itemStack;
        if(id < 9) {
            if (id < 4) { //armor
                itemStack = playerBackupData.main().get(39 - id);
            } else if (id == 8) { //offhand
                itemStack = playerBackupData.main().get(40);
            } else {
                return GuiSlot.EMPTY;
            }
            return GuiSlot.of(new ShadowSlot(new SimpleInventory(itemStack != null ? itemStack.copy() : ItemStack.EMPTY),
                    0, 0, 0, GuiUtils.getShadowstack(id)));
        }
        else if(id < 36) {
            itemStack = playerBackupData.main().get(id);
        }
        else if (id < 45) {
            itemStack = playerBackupData.main().get(id - 36);
        }
        else if (id < 72) {
            itemStack = playerBackupData.enderChest().get(id - 45);
        }
        else {
            return GuiSlot.EMPTY;
        }

        return GuiSlot.of(new Slot(new SimpleInventory(itemStack != null ? itemStack.copy() : ItemStack.EMPTY), 0, 0, 0));
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        return switch (id) {
            case 0 -> GuiSlot.back(previousUi::open);
            case 2 -> viewInventory();
            case 4 -> GuiSlot.restore(Objects.requireNonNull(player.getServer()), playerBackupData);
            case 6 -> viewEnderChest();
            case 7 -> GuiSlot.of(GuiSlot.builder(GuiUtils.getPlayerHead(targetUUID, playerName),
                    GuiUtils.getBackupDetails(playerName, playerBackupData)));
            case 8 -> GuiSlot.teleport(player, playerBackupData.world(), playerBackupData.pos(), "Event");
            default -> GuiSlot.empty();
        };
    }

    public GuiSlot viewEnderChest() {
        if (canNextPage()) {
            GuiElementBuilder icon = GuiSlot.builder(Items.ENDER_CHEST, "View Ender Chest");
            return GuiSlot.of(icon.setCallback((x, y, z) -> {
                PagedGui.playClickSound(getPlayer());
                nextPage();
            }));
        }
        else return GuiSlot.EMPTY;
    }


    public GuiSlot viewInventory() {
        if (canPreviousPage()) {
            GuiElementBuilder icon = GuiSlot.builder(GuiUtils.getPlayerHead(targetUUID, playerName), "View Inventory");
            return GuiSlot.of(icon.setCallback((x, y, z) -> {
                PagedGui.playClickSound(getPlayer());
                previousPage();
            }));
        }
        else return GuiSlot.EMPTY;

    }

    @Override
    public void onClose() {
        super.onClose();
        InventoryBackup.data.markDirty();
    }
}
