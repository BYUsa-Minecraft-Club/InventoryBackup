package edu.byu.minecraft.invbackup.data;


import edu.byu.minecraft.InventoryBackup;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
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

    private final SimpleInventory main;

    private final SimpleInventory armor;

    private final SimpleInventory offHand;

    private final SimpleInventory enderChest;

    private final int experienceLevel;

    private final int totalExperience;

    private final float experienceProgress;

    private final float health;

    private final HungerManager hungerManager;

    private final Identifier world;

    private final Vec3d pos;

    private final LogType logType;

    private final String deathReason;


    public PlayerBackupData(ServerPlayerEntity player, LogType logType) {
        this(player, logType, null);
    }


    public PlayerBackupData(ServerPlayerEntity player, LogType logType, String deathReason) {
        this.uuid = player.getUuid();
        this.logType = logType;
        this.timestamp = System.currentTimeMillis();
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

        main = new SimpleInventory(PlayerInventory.MAIN_SIZE);
        armor = new SimpleInventory(PlayerInventory.ARMOR_SLOTS.length);
        offHand = new SimpleInventory(1);
        enderChest = new SimpleInventory(27);

        main.readNbtList((NbtList) nbt.get("main"), lookup);
        armor.readNbtList((NbtList) nbt.get("armor"), lookup);
        offHand.readNbtList((NbtList) nbt.get("offHand"), lookup);
        enderChest.readNbtList((NbtList) nbt.get("enderChest"), lookup);

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

        nbtCompound.put("main", main.toNbtList(lookup));
        nbtCompound.put("armor", armor.toNbtList(lookup));
        nbtCompound.put("offHand", offHand.toNbtList(lookup));
        nbtCompound.put("enderChest", enderChest.toNbtList(lookup));

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
        InventoryBackup.data.addBackup(new PlayerBackupData(targetPlayer, LogType.FORCE));

        restore(main, targetPlayer.getInventory().main);
        restore(armor, targetPlayer.getInventory().armor);
        restore(offHand, targetPlayer.getInventory().offHand);
        restore(enderChest, targetPlayer.getEnderChestInventory().heldStacks);

        targetPlayer.setExperienceLevel(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getNextLevelExperience()));
    }

    public static SimpleInventory copy(DefaultedList<ItemStack> stacks) {
        SimpleInventory inventory = new SimpleInventory(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            inventory.setStack(i, stacks.get(i).copy());
        }
        return inventory;
    }

    private void restore(SimpleInventory source, DefaultedList<ItemStack> target) {
        for (int i = 0; i < source.size(); i++) {
            target.set(i, source.getHeldStacks().get(i).copy());
        }
    }


    public UUID getUuid() {
        return uuid;
    }


    public Long getTimestamp() {
        return timestamp;
    }


    public SimpleInventory getMain() {
        return main;
    }

    public SimpleInventory  getArmor() {
        return armor;
    }

    public SimpleInventory  getOffHand() {
        return offHand;
    }

    public SimpleInventory  getEnderChest() {
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
