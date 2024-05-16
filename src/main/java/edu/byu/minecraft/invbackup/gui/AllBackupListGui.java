package edu.byu.minecraft.invbackup.gui;

import com.mojang.authlib.GameProfile;
import edu.byu.minecraft.InventoryBackup;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class AllBackupListGui extends PagedGui {
    private int ticker = 0;
    private List<Map.Entry<UUID, String>> players;


    public AllBackupListGui(ServerPlayerEntity player) {
        super(player);

        this.setTitle(Text.of("All Players"));
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

            ItemStack playerHead = Items.PLAYER_HEAD.getDefaultStack();
            GameProfile profile = new GameProfile(playerData.getKey(), playerData.getValue());
            ProfileComponent pc = new ProfileComponent(Optional.empty(), Optional.of(playerData.getKey()),
                    profile.getProperties());
            playerHead.set(DataComponentTypes.PROFILE, pc);

            GuiElementBuilder element = GuiElementBuilder.from(playerHead)
                    .setName(Text.of(playerData.getValue()))
                    .setCallback((index, type, action) -> new PlayerBackupListGui(playerData.getKey(),
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
        ret.sort(Map.Entry.comparingByValue());
        return ret;
    }
}
