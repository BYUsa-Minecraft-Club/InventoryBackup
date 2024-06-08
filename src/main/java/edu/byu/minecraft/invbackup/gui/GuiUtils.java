package edu.byu.minecraft.invbackup.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.Optional;
import java.util.UUID;

public class GuiUtils {
    static ItemStack getPlayerHead(UUID uuid, String playerName) {
        ItemStack playerHead = Items.PLAYER_HEAD.getDefaultStack();
        GameProfile profile = new GameProfile(uuid, playerName);
        ProfileComponent pc = new ProfileComponent(Optional.empty(), Optional.of(uuid), profile.getProperties());
        playerHead.set(DataComponentTypes.PROFILE, pc);
        return playerHead;
    }
}
