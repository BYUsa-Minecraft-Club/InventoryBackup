package edu.byu.minecraft.invbackup.data;


import edu.byu.minecraft.InventoryBackup;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

public class PlayerBackupData {

    private static final int CURRENT_VERSION = 1;

    private UUID uuid;

    private Long timestamp;

    private SimpleInventory main;

    private SimpleInventory enderChest;

    private int experienceLevel;

    private int totalExperience;

    private float experienceProgress;

    private float health;

    private HungerManager hungerManager;

    private Identifier world;

    private Vec3d pos;

    private LogType logType;

    private String deathReason;
    
    private PlayerBackupData() {
        
    }


    public PlayerBackupData(ServerPlayerEntity player, LogType logType) {
        this(player, logType, null);
    }


    public PlayerBackupData(ServerPlayerEntity player, LogType logType, String deathReason) {
        this.uuid = player.getUuid();
        this.logType = logType;
        this.timestamp = System.currentTimeMillis();
        this.deathReason = deathReason;

        main = copy(player.getInventory());
        enderChest = copy(player.getEnderChestInventory());

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

    public PlayerBackupData(PlayerBackupData copy) {
        uuid = copy.uuid;
        timestamp = copy.timestamp;
        main = copy(copy.main);
        enderChest = copy(copy.enderChest);
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

    public static PlayerBackupData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Optional<Integer> versionOpt = nbt.getInt("version");
        if (versionOpt.isEmpty()) return fromNbtBeforeVersion(nbt, lookup);
        Integer version = versionOpt.get();
        return switch (version) {
            case 1 -> fromNbtV1(nbt, lookup);
            default -> throw new IllegalStateException("Unexpected value: " + version);
        };
    }

    public static PlayerBackupData fromNbtV1(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        PlayerBackupData data = new PlayerBackupData();
        data.uuid = UUID.fromString(nbt.getString("uuid").get());
        data.timestamp = nbt.getLong("timestamp").get();

        int mainSize = getMainSize();
        data.main = new SimpleInventory(mainSize);
        data.enderChest = new SimpleInventory(27);

        data.main.readNbtList((NbtList) nbt.get("main"), lookup);
        data.enderChest.readNbtList((NbtList) nbt.get("enderChest"), lookup);

        data.experienceLevel = nbt.getInt("experienceLevel").get();
        data.totalExperience = nbt.getInt("totalExperience").get();
        data.experienceProgress = nbt.getFloat("experienceProgress").get();
        data.health = nbt.getFloat("health").get();

        data.hungerManager = new HungerManager();
        data.hungerManager.readNbt(nbt.getCompound("hungerManager").get());

        data.world = Identifier.tryParse(nbt.getString("world").get());
        data.pos = new Vec3d(nbt.getDouble("xpos").get(), nbt.getDouble("ypos").get(), nbt.getDouble("zpos").get());
        data.logType = LogType.valueOf(nbt.getString("logType").get());
        Optional<String> deathReasonOpt = nbt.getString("deathReason");
        data.deathReason = deathReasonOpt.orElse(null);

        return data;
    }

    public static PlayerBackupData fromNbtBeforeVersion(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        PlayerBackupData data = new PlayerBackupData();
        data.uuid = SaveData.uuidFromIntArray(nbt.getIntArray("uuid").get());
        data.timestamp = nbt.getLong("timestamp").get();

        data.main =  new SimpleInventory(getMainSize());
        data.enderChest = new SimpleInventory(27);

        data.main.readNbtList((NbtList) nbt.get("main"), lookup);
        data.enderChest.readNbtList((NbtList) nbt.get("enderChest"), lookup);

        SimpleInventory armor = new SimpleInventory(4);
        armor.readNbtList((NbtList) nbt.get("armor"), lookup);
        SimpleInventory offHand = new SimpleInventory(1);
        offHand.readNbtList((NbtList) nbt.get("offHand"), lookup);

        for(int offset : PlayerInventory.EQUIPMENT_SLOTS.keySet()) {
            EquipmentSlot slotId = PlayerInventory.EQUIPMENT_SLOTS.get(offset);
            switch (slotId) {
                case OFFHAND -> data.main.setStack(offset, offHand.getStack(0));
                case FEET -> data.main.setStack(offset, armor.getStack(0));
                case LEGS -> data.main.setStack(offset, armor.getStack(1));
                case CHEST -> data.main.setStack(offset, armor.getStack(2));
                case HEAD -> data.main.setStack(offset, armor.getStack(3));
            }
        }

        data.experienceLevel = nbt.getInt("experienceLevel").get();
        data.totalExperience = nbt.getInt("totalExperience").get();
        data.experienceProgress = nbt.getFloat("experienceProgress").get();
        data.health = nbt.getFloat("health").get();

        data.hungerManager = new HungerManager();
        data.hungerManager.readNbt(nbt.getCompound("hungerManager").get());

        data.world = Identifier.tryParse(nbt.getString("world").get());
        data.pos = new Vec3d(nbt.getDouble("xpos").get(), nbt.getDouble("ypos").get(), nbt.getDouble("zpos").get());
        data.logType = LogType.valueOf(nbt.getString("logType").get());
        Optional<String> deathReasonOpt = nbt.getString("deathReason");
        data.deathReason = deathReasonOpt.orElse(null);

        return data;
    }


    public NbtCompound toNbt(RegistryWrapper.WrapperLookup lookup) {
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putInt("version", CURRENT_VERSION);
        nbtCompound.putString("uuid", uuid.toString());
        nbtCompound.putLong("timestamp", timestamp);

        nbtCompound.put("main", main.toNbtList(lookup));
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

        restore(main, targetPlayer.getInventory());
        restore(enderChest, targetPlayer.getEnderChestInventory());

        targetPlayer.setExperienceLevel(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getNextLevelExperience()));
    }

    private SimpleInventory copy(Inventory from) {
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

    private static int getMainSize() {
        int mainSize = PlayerInventory.MAIN_SIZE;
        for(int slot : PlayerInventory.EQUIPMENT_SLOTS.keySet()) {
            mainSize = Math.max(mainSize, slot + 1);
        }
        return mainSize;
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
