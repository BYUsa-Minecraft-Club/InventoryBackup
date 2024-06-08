package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
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
            return GuiSlot.of(new Slot(PlayerBackupData.copy(playerBackupData.getArmor().getHeldStacks()),
                    3 - id, 0, 0)); //3 - id -> helmet first
        }
        else if(id == 8) {
            return GuiSlot.of(new Slot(PlayerBackupData.copy(playerBackupData.getOffHand().getHeldStacks()),
                    0, 0, 0));
        }
        else if(id < 9) {
            return GuiSlot.empty();
        }
        else if(id < 36) {
            return GuiSlot.of(new Slot(PlayerBackupData.copy(playerBackupData.getMain().getHeldStacks())
                    , id, 0, 0));
        }
        else if (id < 45) {
            return GuiSlot.of(new Slot(PlayerBackupData.copy(playerBackupData.getMain().getHeldStacks()),
                    id - 36, 0, 0));
        }
        else if (id < 72) {
            return GuiSlot.of(new Slot(PlayerBackupData.copy(playerBackupData.getEnderChest().getHeldStacks()),
                    id - 45, 0, 0));
        }
        return GuiSlot.empty();
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        return switch (id) {
            case 0 -> GuiSlot.back(previousUi::open);
            case 2 -> viewInventory();
            case 4 -> GuiSlot.restore(Objects.requireNonNull(player.getServer()), playerBackupData);
            case 6 -> viewEnderChest();
            case 8 -> GuiSlot.teleport(player, playerBackupData.getWorld(), playerBackupData.getPos(), "Event");
            default ->  GuiSlot.empty();
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
            GuiElementBuilder icon = GuiSlot.builder(Items.PLAYER_HEAD, "View Inventory");
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
