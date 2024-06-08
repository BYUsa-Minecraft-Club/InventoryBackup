package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.mixin.PlayerManagerAccessor;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiInterface;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.UUID;

public class LiveInventoryGui extends PagedGui {
    private final String playerName;
    private final GuiInterface previousUi;
    private final ServerPlayerEntity target;


    public LiveInventoryGui(String playerName, GuiInterface previousUi, ServerPlayerEntity player) {
        super(player);
        this.playerName = playerName;
        this.setTitle(Text.of(String.format("%s's, live inventory", playerName)));
        this.previousUi = previousUi;
        this.target = InventoryBackup.getPlayer(playerName, Objects.requireNonNull(player.getServer()));
        this.updateDisplay();
    }


    @Override
    protected int getPageAmount() {
        return 2;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if(id < 4) {
            return GuiSlot.of(new Slot(target.getInventory(), 36 + 3 - id, 0, 0)); //3 - id -> helmet first
        }
        else if(id == 8) {
            return GuiSlot.of(new Slot(target.getInventory(), 40, 0, 0));
        }
        else if(id < 9) {
            return GuiSlot.empty();
        }
        else if(id < 36) {
            return GuiSlot.of(new Slot(target.getInventory(), id, 0, 0));
        }
        else if (id < 45) {
            return GuiSlot.of(new Slot(target.getInventory(), id - 36, 0, 0));
        }
        else if (id < 72) {
            return GuiSlot.of(new Slot(target.getEnderChestInventory(), id - 45, 0, 0));
        }
        return GuiSlot.empty();
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        return switch (id) {
            case 0 -> GuiSlot.back(previousUi::open);
            case 2 -> viewInventory();
            case 6 -> viewEnderChest();
            case 8 -> GuiSlot.teleport(player, player.getWorld().getRegistryKey().getValue(), player.getPos(), "Player");
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
        PlayerManager pm = Objects.requireNonNull(player.getServer()).getPlayerManager();
        if(!pm.getPlayerList().contains(target)) {
            ((PlayerManagerAccessor) pm).callSavePlayerData(target);
        }
    }
}
