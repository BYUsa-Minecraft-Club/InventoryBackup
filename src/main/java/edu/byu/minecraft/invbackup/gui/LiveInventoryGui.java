package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.GuiLike;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;

public class LiveInventoryGui extends PagedGui {
    private final String playerName;
    private final GuiLike previousUi;
    private final ServerPlayer target;


    public LiveInventoryGui(String playerName, GuiLike previousUi, ServerPlayer player) {
        super(player);
        this.playerName = playerName;
        this.setTitle(Component.nullToEmpty(String.format("%s's Live Inventory", playerName)));
        this.previousUi = previousUi;
        this.target = InventoryBackup.getPlayer(playerName, player.level().getServer());
        this.updateDisplay();
    }


    @Override
    protected int getPageAmount() {
        return 2;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if (id < 4) {                                                        //3 - id -> helmet first
            return GuiSlot.of(new Slot(target.getInventory(), 36 + 3 - id, 0, 0));
        } else if (id == 8) {
            return GuiSlot.of(new Slot(target.getInventory(), 40, 0, 0));
        } else if (id == 4) {
            return GuiSlot.of(GuiElementBuilder.from(GuiSlot.EMPTY_STACK).setName(Component.nullToEmpty("← Armor ←")));
        } else if (id == 7) {
            return GuiSlot.of(GuiElementBuilder.from(GuiSlot.EMPTY_STACK).setName(Component.nullToEmpty("→ Offhand →")));
        } else if (id < 9) {
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
            case 0 -> GuiSlot.back(() -> {
                InventoryBackup.savePlayerData(target);
                previousUi.open();
            });
            case 2 -> viewInventory();
            case 6 -> viewEnderChest();
            case 8 -> GuiSlot.teleport(player, player.level().dimension().identifier(),
                    player.position(), "Player");
            default ->  GuiSlot.empty();
        };
    }

    public GuiSlot viewEnderChest() {
        if (canNextPage()) {
            GuiElementBuilder icon = GuiSlot.builder(Items.ENDER_CHEST, "View Ender Chest");
            return GuiSlot.of(icon.setCallback(() -> {
                PagedGui.playClickSound(getPlayer());
                nextPage();
            }));
        }
        else return GuiSlot.EMPTY;
    }


    public GuiSlot viewInventory() {
        if (canPreviousPage()) {
            GuiElementBuilder icon = GuiSlot.builder(GuiUtils.getPlayerHead(target.getUUID(), playerName), "View Inventory");
            return GuiSlot.of(icon.setCallback(() -> {
                PagedGui.playClickSound(getPlayer());
                previousPage();
            }));
        }
        else return GuiSlot.EMPTY;

    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        onManualClose();
    }

    @Override
    public void onManualClose() {
        InventoryBackup.savePlayerData(target);
    }
}
