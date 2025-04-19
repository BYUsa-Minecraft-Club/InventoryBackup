package edu.byu.minecraft.invbackup.gui;

import com.mojang.authlib.GameProfile;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;

import java.util.UUID;

public class GuiUtils {
    static ItemStack getPlayerHead(UUID uuid, String playerName) {
        ItemStack playerHead = Items.PLAYER_HEAD.getDefaultStack();
        GameProfile profile = new GameProfile(uuid, playerName);

        NbtCompound headNbt = new NbtCompound();
        headNbt.put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), profile));
        NbtCompound displaytag = new NbtCompound();
        displaytag.putString("Name", profile.getName());
        headNbt.put("display", displaytag);

        playerHead.setNbt(headNbt);
        return playerHead;
    }
}
