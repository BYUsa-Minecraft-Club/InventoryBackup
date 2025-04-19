package edu.byu.minecraft.invbackup.data;


import edu.byu.minecraft.InventoryBackup;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
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
        enderChest = copy(player.getEnderChestInventory().stacks);

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

        main = nbtToInv(nbt.getCompound("main"));
        armor = nbtToInv(nbt.getCompound("armor"));
        offHand = nbtToInv(nbt.getCompound("offHand"));
        enderChest = nbtToInv(nbt.getCompound("enderChest"));

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

    private SimpleInventory nbtToInv(NbtCompound nbt) {
        SimpleInventory inv = new SimpleInventory(nbt.getInt("size"));
        nbt.getKeys().forEach(entry -> {
            if (entry.equals("size")) return;
            inv.setStack(Integer.parseInt(entry), ItemStack.fromNbt(nbt.getCompound(entry)));
        });
        return inv;
    }

    public PlayerBackupData(PlayerBackupData copy) {
        uuid = copy.uuid;
        timestamp = copy.timestamp;
        main = copy(copy.main.stacks);
        armor = copy(copy.armor.stacks);
        offHand = copy(copy.offHand.stacks);
        enderChest = copy(copy.enderChest.stacks);
        experienceLevel = copy.experienceLevel;
        totalExperience = copy.totalExperience;
        experienceProgress = copy.experienceProgress;
        health = copy.health;
        hungerManager = copy.hungerManager;
        world = copy.world;
        pos = copy.pos;
        logType = copy.logType;
        deathReason = copy.deathReason;
    }

    public NbtCompound toNbt() {
        NbtCompound nbtCompound = new NbtCompound();

        nbtCompound.putUuid("uuid", uuid);
        nbtCompound.putLong("timestamp", timestamp);

        nbtCompound.put("main", invToNbt(main));
        nbtCompound.put("armor", invToNbt(armor));
        nbtCompound.put("offHand", invToNbt(offHand));
        nbtCompound.put("enderChest", invToNbt(enderChest));

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

    private NbtCompound invToNbt(SimpleInventory inv) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("size", inv.size());
        for (int i = 0; i < inv.size(); i++) {
            ItemStack itemStack = inv.getStack(i);
            if(!itemStack.isEmpty()) {
                nbtCompound.put(String.valueOf(i), itemStack.writeNbt(new NbtCompound()));
            }
        }
        return nbtCompound;
    }

    public void restore(ServerPlayerEntity targetPlayer) {
        InventoryBackup.data.addBackup(new PlayerBackupData(targetPlayer, LogType.FORCE));

        restore(main, targetPlayer.getInventory().main);
        restore(armor, targetPlayer.getInventory().armor);
        restore(offHand, targetPlayer.getInventory().offHand);
        restore(enderChest, targetPlayer.getEnderChestInventory().stacks);

        targetPlayer.setExperienceLevel(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getNextLevelExperience()));
    }

    private SimpleInventory copy(DefaultedList<ItemStack> stacks) {
        SimpleInventory inventory = new SimpleInventory(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            inventory.setStack(i, stacks.get(i).copy());
        }
        return inventory;
    }

    private void restore(SimpleInventory source, DefaultedList<ItemStack> target) {
        for (int i = 0; i < source.size(); i++) {
            target.set(i, source.stacks.get(i).copy());
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
