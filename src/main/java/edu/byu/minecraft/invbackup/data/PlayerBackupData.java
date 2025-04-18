package edu.byu.minecraft.invbackup.data;


import edu.byu.minecraft.InventoryBackup;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record PlayerBackupData(UUID uuid, Long timestamp, SimpleInventory main, SimpleInventory enderChest,
                               int experienceLevel, int totalExperience, float experienceProgress, Identifier world,
                               Vec3d pos, LogType logType, String deathReason) {


    public static PlayerBackupData forPlayer(ServerPlayerEntity player, LogType logType) {
        return forPlayer(player, logType, null);
    }


    public static PlayerBackupData forPlayer(ServerPlayerEntity player, LogType logType, String deathReason) {
        return new PlayerBackupData(player.getUuid(), System.currentTimeMillis(), copy(player.getInventory()),
                copy(player.getEnderChestInventory()), player.experienceLevel, player.totalExperience,
                player.experienceProgress, player.getWorld().getRegistryKey().getValue(), player.getPos(), logType,
                deathReason);
    }

    public static PlayerBackupData copy(PlayerBackupData copy) {
        return new PlayerBackupData(copy.uuid(), copy.timestamp(), copy.main(), copy.enderChest(),
                copy.experienceLevel(), copy.totalExperience(), copy.experienceProgress(), copy.world(), copy.pos(),
                copy.logType(), copy.deathReason());
    }

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("uuid", uuid.toString());
        nbtCompound.putLong("timestamp", timestamp);

        nbtCompound.put("main", invToNbt(main, lookup));
        nbtCompound.put("enderChest", invToNbt(enderChest, lookup));

        nbtCompound.putInt("experienceLevel", experienceLevel);
        nbtCompound.putInt("totalExperience", totalExperience);
        nbtCompound.putFloat("experienceProgress", experienceProgress);

        nbtCompound.putString("world", world.toString());
        nbtCompound.putDouble("xpos", pos.getX());
        nbtCompound.putDouble("ypos", pos.getY());
        nbtCompound.putDouble("zpos", pos.getZ());
        nbtCompound.putString("logType", logType.name());
        if (deathReason != null) nbtCompound.putString("deathReason", deathReason);

        return nbtCompound;
    }

    private NbtCompound invToNbt(SimpleInventory inv, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("size", inv.size());
        for (int i = 0; i < inv.size(); i++) {
            ItemStack itemStack = inv.getStack(i);
            if(!itemStack.isEmpty()) {
                nbtCompound.put(String.valueOf(i), itemStack.toNbt(lookup));
            }
        }
        return nbtCompound;
    }

    public void restore(ServerPlayerEntity targetPlayer) {
        InventoryBackup.data.addBackup(forPlayer(targetPlayer, LogType.FORCE));

        restore(main, targetPlayer.getInventory());
        restore(enderChest, targetPlayer.getEnderChestInventory());

        targetPlayer.setExperienceLevel(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getNextLevelExperience()));
    }

    private static SimpleInventory copy(Inventory from) {
        SimpleInventory inventory = new SimpleInventory(from.size());
        for (int i = 0; i < from.size(); i++) {
            inventory.setStack(i, from.getStack(i).copy());
        }
        return inventory;
    }

    private void restore(SimpleInventory source, Inventory target) {
        for (int i = 0; i < source.size(); i++) {
            target.setStack(i, source.getHeldStacks().get(i).copy());
        }
    }
}
