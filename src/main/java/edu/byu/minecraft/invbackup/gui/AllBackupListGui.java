package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class AllBackupListGui extends PagedGui {
    private int ticker = 0;
    private List<Map.Entry<UUID, String>> players;


    public AllBackupListGui(ServerPlayer player) {
        super(player);

        this.setTitle(Component.nullToEmpty("All Players"));
        this.players = getPlayers();
        this.updateDisplay();
    }

    @Override
    protected int getPageAmount() {
        return this.players.size() / PAGE_SIZE + 1;
    }

    @Override
    protected GuiSlot getElement(int id) {
        if (id < this.players.size()) {
            Map.Entry<UUID, String> playerData = players.get(id);
            ItemStack playerHead = GuiUtils.getPlayerHead(playerData.getKey(), playerData.getValue());
            GuiElementBuilder element = GuiElementBuilder.from(playerHead)
                    .setName(Component.nullToEmpty(playerData.getValue()))
                    .setCallback(() -> new PlayerBackupListGui(playerData.getKey(),
                    playerData.getValue(), player).open());

            return GuiSlot.of(element);
        }

        return GuiSlot.empty();
    }

    @Override
    protected GuiSlot getNavElement(int id) {
        return switch (id) {
            case 2 -> GuiSlot.previousPage(this);
            case 6 -> GuiSlot.nextPage(this);
            default ->  GuiSlot.empty();
        };
    }

    @Override
    public void onTick() {
        this.ticker++;
        if (this.ticker % 100 == 0) {
            this.players = getPlayers();
            this.updateDisplay();
        }
        super.onTick();
    }

    private List<Map.Entry<UUID, String>> getPlayers() {
        List<Map.Entry<UUID, String>> ret = new ArrayList<>(InventoryBackup.data.getPlayers().entrySet());
        ret.sort((o1, o2) -> o1.getValue().compareToIgnoreCase(o2.getValue()));
        return ret;
    }
}
