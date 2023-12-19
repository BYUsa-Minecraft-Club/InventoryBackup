package edu.byu.minecraft.invbackup.data;


import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class PlayerBackupData {

    private final UUID uuid;

    private final Long timestamp;

    private final PlayerInventory mainInventory;

    private final EnderChestInventory enderChest;

    private final int experienceLevel;

    private final int totalExperience;

    private final float experienceProgress;

    private final float health;

    private final HungerManager hungerManager;

    private final Identifier world;

    private final Vec3d pos;

    private final LogType logType;

    private final String deathReason;


    public PlayerBackupData(ServerPlayerEntity player, LogType logType, Long timestamp) {
        this(player, logType, timestamp, null);
    }


    public PlayerBackupData(ServerPlayerEntity player, LogType logType, Long timestamp, String deathReason) {
        this.uuid = player.getUuid();
        this.logType = logType;
        this.timestamp = timestamp;
        this.deathReason = deathReason;

        mainInventory = new PlayerInventory(null);
        PlayerInventory toCopy = player.getInventory();
        for (int i = 0; i < mainInventory.size(); i++) {
            mainInventory.setStack(i, toCopy.getStack(i).copy());
        }

        enderChest = new EnderChestInventory();
        EnderChestInventory toCopyEc = player.getEnderChestInventory();
        for (int i = 0; i < enderChest.size(); i++) {
            enderChest.setStack(i, toCopyEc.getStack(i).copy());
        }

        totalExperience = player.totalExperience;
        experienceLevel = player.experienceLevel;
        experienceProgress = player.experienceProgress;
        health = player.getHealth();

        hungerManager = new HungerManager();
        NbtCompound hungerManagerNbt = new NbtCompound();
        player.getHungerManager().writeNbt(hungerManagerNbt);
        hungerManager.readNbt(hungerManagerNbt);

        world = player.getWorld().getRegistryKey().getValue();
        pos = player.getPos();
    }

    public PlayerBackupData(NbtCompound nbt) {
        uuid = nbt.getUuid("uuid");
        timestamp = nbt.getLong("timestamp");

        mainInventory = new PlayerInventory(null);
        mainInventory.readNbt((NbtList) nbt.get("mainInventory"));

        enderChest = new EnderChestInventory();
        enderChest.readNbtList((NbtList) nbt.get("enderChest"));

        experienceLevel = nbt.getInt("experienceLevel");
        totalExperience = nbt.getInt("totalExperience");
        experienceProgress = nbt.getFloat("experienceProgress");
        health = nbt.getFloat("health");

        hungerManager = new HungerManager();
        hungerManager.readNbt(nbt.getCompound("hungerManager"));

        world = Identifier.tryParse(nbt.getString("world"));
        pos = new Vec3d(nbt.getDouble("xpos"), nbt.getDouble("ypos"), nbt.getDouble("zpos"));
        logType = LogType.valueOf(nbt.getString("logType"));
        deathReason = (nbt.contains("deathReason")) ? nbt.getString("deathReason") : null;
    }

    public NbtCompound toNbt() {
        NbtCompound nbtCompound = new NbtCompound();

        nbtCompound.putUuid("uuid", uuid);
        nbtCompound.putLong("timestamp", timestamp);
        nbtCompound.put("mainInventory", mainInventory.writeNbt(new NbtList()));
        nbtCompound.put("enderChest", enderChest.toNbtList());
        nbtCompound.putInt("experienceLevel", experienceLevel);
        nbtCompound.putInt("totalExperience", totalExperience);
        nbtCompound.putFloat("experienceProgress", experienceProgress);
        nbtCompound.putFloat("health", health);

        NbtCompound hungerManagerNbt = new NbtCompound();
        hungerManager.writeNbt(hungerManagerNbt);
        nbtCompound.put("hungerManager", hungerManagerNbt);

        nbtCompound.putString("world", world.toString());
        nbtCompound.putDouble("xpos", pos.getX());
        nbtCompound.putDouble("ypos", pos.getY());
        nbtCompound.putDouble("zpos", pos.getZ());
        nbtCompound.putString("logType", logType.name());
        if(deathReason != null) nbtCompound.putString("deathReason", deathReason);

        return nbtCompound;
    }


    public UUID getUuid() {
        return uuid;
    }


    public Long getTimestamp() {
        return timestamp;
    }


    public PlayerInventory getMainInventory() {
        return mainInventory;
    }


    public EnderChestInventory getEnderChest() {
        return enderChest;
    }


    public int getExperienceLevel() {
        return experienceLevel;
    }


    public int getTotalExperience() {
        return totalExperience;
    }


    public float getExperienceProgress() {
        return experienceProgress;
    }


    public float getHealth() {
        return health;
    }


    public HungerManager getHungerManager() {
        return hungerManager;
    }


    public Identifier getWorld() {
        return world;
    }


    public Vec3d getPos() {
        return pos;
    }


    public LogType getLogType() {
        return logType;
    }


    public String getDeathReason() {
        return deathReason;
    }

}
