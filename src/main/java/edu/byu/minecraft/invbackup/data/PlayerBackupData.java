package edu.byu.minecraft.invbackup.data;


import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public class PlayerBackupData {

    private final UUID uuid;

    private final Long timestamp;

    private final DefaultedList<ItemStack> main;

    private final DefaultedList<ItemStack> armor;

    private final DefaultedList<ItemStack> offHand;

    private final DefaultedList<ItemStack> enderChest;

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

        main = copy(player.getInventory().main);
        armor = copy(player.getInventory().armor);
        offHand = copy(player.getInventory().offHand);
        enderChest = copy(player.getEnderChestInventory().getHeldStacks());

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

    public PlayerBackupData(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        uuid = nbt.getUuid("uuid");
        timestamp = nbt.getLong("timestamp");

        main = DefaultedList.ofSize(PlayerInventory.MAIN_SIZE, ItemStack.EMPTY);
        armor = DefaultedList.ofSize(PlayerInventory.ARMOR_SLOTS.length, ItemStack.EMPTY);
        offHand = DefaultedList.ofSize(1, ItemStack.EMPTY);
        enderChest = DefaultedList.ofSize(27, ItemStack.EMPTY);
        Inventories.readNbt((NbtCompound) nbt.get("main"), main, lookup);
        Inventories.readNbt((NbtCompound) nbt.get("armor"), armor, lookup);
        Inventories.readNbt((NbtCompound) nbt.get("offHand"), offHand, lookup);
        Inventories.readNbt((NbtCompound) nbt.get("enderChest"), enderChest, lookup);

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

    public NbtCompound toNbt(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbtCompound = new NbtCompound();

        nbtCompound.putUuid("uuid", uuid);
        nbtCompound.putLong("timestamp", timestamp);

        nbtCompound.put("main", Inventories.writeNbt(new NbtCompound(), main, lookup));
        nbtCompound.put("armor", Inventories.writeNbt(new NbtCompound(), armor, lookup));
        nbtCompound.put("offHand", Inventories.writeNbt(new NbtCompound(), offHand, lookup));
        nbtCompound.put("enderChest", Inventories.writeNbt(new NbtCompound(), enderChest, lookup));

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
        if (deathReason != null) nbtCompound.putString("deathReason", deathReason);

        return nbtCompound;
    }

    public void restore(ServerPlayerEntity targetPlayer) {
        restore(main, targetPlayer.getInventory().main);
        restore(armor, targetPlayer.getInventory().armor);
        restore(offHand, targetPlayer.getInventory().offHand);
        restore(enderChest, targetPlayer.getEnderChestInventory().heldStacks);

        targetPlayer.setExperienceLevel(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getNextLevelExperience()));
    }

    private DefaultedList<ItemStack> copy(DefaultedList<ItemStack> stacks) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(stacks.size(), ItemStack.EMPTY);
        for (int i = 0; i < stacks.size(); i++) {
            list.set(i, stacks.get(i).copy());
        }
        return list;
    }

    private void restore(DefaultedList<ItemStack> source, DefaultedList<ItemStack> target) {
        for (int i = 0; i < source.size(); i++) {
            target.set(i, source.get(i).copy());
        }
    }


    public UUID getUuid() {
        return uuid;
    }


    public Long getTimestamp() {
        return timestamp;
    }


    public DefaultedList<ItemStack> getMain() {
        return main;
    }

    public DefaultedList<ItemStack>  getArmor() {
        return armor;
    }

    public DefaultedList<ItemStack>  getOffHand() {
        return offHand;
    }

    public DefaultedList<ItemStack>  getEnderChest() {
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
