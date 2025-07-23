package edu.byu.minecraft.invbackup.data;


import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public record PlayerBackupData(UUID uuid, Long timestamp, Map<Integer, ItemStack> main, Map<Integer, ItemStack> enderChest,
                               int experienceLevel, int totalExperience, float experienceProgress, Identifier world,
                               Vec3d pos, LogType logType, Optional<String> deathReason) {

    private static final Codec<Map<Integer, ItemStack>> INT_ITEMSTACK_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf),
            ItemStack.CODEC);

    public static final Codec<PlayerBackupData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(PlayerBackupData::uuid),
            Codec.LONG.fieldOf("timestamp").forGetter(PlayerBackupData::timestamp),
            INT_ITEMSTACK_MAP_CODEC.fieldOf("main").forGetter(PlayerBackupData::main),
            INT_ITEMSTACK_MAP_CODEC.fieldOf("enderChest").forGetter(PlayerBackupData::enderChest),
            Codec.INT.fieldOf("experienceLevel").forGetter(PlayerBackupData::experienceLevel),
            Codec.INT.fieldOf("totalExperience").forGetter(PlayerBackupData::totalExperience),
            Codec.FLOAT.fieldOf("experienceProgress").forGetter(PlayerBackupData::experienceProgress),
            Identifier.CODEC.fieldOf("world").forGetter(PlayerBackupData::world),
            Vec3d.CODEC.fieldOf("pos").forGetter(PlayerBackupData::pos),
            LogType.CODEC.fieldOf("logType").forGetter(PlayerBackupData::logType),
            Codec.STRING.optionalFieldOf("deathReason").forGetter(PlayerBackupData::deathReason)
        ).apply(instance, PlayerBackupData::new));


    public static PlayerBackupData forPlayer(ServerPlayerEntity player, LogType logType) {
        return forPlayer(player, logType, null);
    }


    public static PlayerBackupData forPlayer(ServerPlayerEntity player, LogType logType, String deathReason) {
        return new PlayerBackupData(player.getUuid(), System.currentTimeMillis(),
                getStacks(player.getInventory()), getStacks(player.getEnderChestInventory()),
                player.experienceLevel, player.totalExperience, player.experienceProgress,
                player.getWorld().getRegistryKey().getValue(), player.getPos(), logType, Optional.ofNullable(deathReason));
    }

    private static Map<Integer, ItemStack> getStacks(Iterable<ItemStack> iterable) {
        Map<Integer, ItemStack> output = new HashMap<>();
        int i = 0;
        for(ItemStack itemStack : iterable) {
            if(!itemStack.isEmpty()) {
                output.put(i, itemStack);
            }
            i++;
        }
        return output;
    }

    public static PlayerBackupData copy(PlayerBackupData copy) {
        return new PlayerBackupData(copy.uuid(), copy.timestamp(), copy.main(), copy.enderChest(),
                copy.experienceLevel(), copy.totalExperience(), copy.experienceProgress(), copy.world(), copy.pos(),
                copy.logType(), copy.deathReason());
    }

//    public static PlayerBackupData readData(ReadView readView) {
//        SimpleInventory main = new SimpleInventory();
//        main.readDataList(readView.getTypedListView("main", ItemStack.CODEC));
//
//        SimpleInventory enderChest = new SimpleInventory();
//        enderChest.readDataList(readView.getTypedListView("enderChest", ItemStack.CODEC));
//
//        UUID uuid = readView.getOptionalString("uuidStr").map(UUID::fromString).orElse(null);
//        Long timestamp = readView.getLong("timestamp", 0);
//        int experienceLevel = readView.getInt("experienceLevel", 0);
//        int totalExperience = readView.getInt("totalExperience", 0);
//        float experienceProgress = readView.getFloat("experienceProgress", 0);
//
//        Identifier world = readView.getOptionalString("world").map(Identifier::tryParse).orElse(World.OVERWORLD.getValue());
//        Vec3d pos = readView.read("pos", Vec3d.CODEC).orElse(Vec3d.ZERO);
//        LogType logType = readView.getOptionalString("logType").map(LogType::valueOf).orElse(LogType.FORCE);
//        String deathReason = readView.getString("deathReason", null);
//
//        return new PlayerBackupData(uuid, timestamp, main, enderChest, experienceLevel, totalExperience,
//                experienceProgress, world, pos, logType, deathReason);
//    }
//
//    public void writeData(WriteView writeView) {
//        main.toDataList(writeView.getListAppender("main", ItemStack.CODEC));
//        enderChest.toDataList(writeView.getListAppender("enderChest", ItemStack.CODEC));
//
//        writeView.putString("uuidStr", uuidStr.toString());
//        writeView.putLong("timestamp", timestamp);
//        writeView.putInt("experienceLevel", experienceLevel);
//        writeView.putInt("totalExperience", totalExperience);
//        writeView.putFloat("experienceProgress", experienceProgress);
//
//        writeView.putString("world", world.toString());
//        writeView.put("pos", Vec3d.CODEC, pos);
//        writeView.putString("logType", logType.toString());
//        writeView.putString("deathReason", deathReason);
//    }

//    public NbtCompound toNbt(RegistryWrapper.WrapperLookup lookup) {
//        NbtCompound nbtCompound = new NbtCompound();
//        nbtCompound.putString("uuidStr", uuidStr.toString());
//        nbtCompound.putLong("timestamp", timestamp);
//
//        nbtCompound.put("main", invToNbt(main, lookup));
//        nbtCompound.put("enderChest", invToNbt(enderChest, lookup));
//
//        nbtCompound.putInt("experienceLevel", experienceLevel);
//        nbtCompound.putInt("totalExperience", totalExperience);
//        nbtCompound.putFloat("experienceProgress", experienceProgress);
//
//        nbtCompound.putString("world", world.toString());
//        nbtCompound.putDouble("xpos", pos.getX());
//        nbtCompound.putDouble("ypos", pos.getY());
//        nbtCompound.putDouble("zpos", pos.getZ());
//        nbtCompound.putString("logType", logType.name());
//        if (deathReason != null) nbtCompound.putString("deathReason", deathReason);
//
//        return nbtCompound;
//    }
//
//    private NbtCompound invToNbt(SimpleInventory inv, RegistryWrapper.WrapperLookup lookup) {
//        NbtCompound nbtCompound = new NbtCompound();
//        nbtCompound.putInt("size", inv.size());
//        for (int i = 0; i < inv.size(); i++) {
//            ItemStack itemStack = inv.getStack(i);
//            if(!itemStack.isEmpty()) {
//                nbtCompound.put(String.valueOf(i),
//                        StackWithSlot.CODEC.encode(itemStack, NbtOps.INSTANCE, new NbtCompound()).getOrThrow());
//            }
//        }
//        return nbtCompound;
//    }

    public void restore(ServerPlayerEntity targetPlayer) {
        ((PlayerBackupHolder) targetPlayer).inventoryBackup$addBackup(forPlayer(targetPlayer, LogType.FORCE));

        restore(main, targetPlayer.getInventory());
        restore(enderChest, targetPlayer.getEnderChestInventory());

        targetPlayer.setExperienceLevel(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getNextLevelExperience()));
    }

    private void restore(Map<Integer, ItemStack> source, Inventory target) {
        for (int i = 0; i < target.size(); i++) {
            ItemStack stack = source.getOrDefault(i, ItemStack.EMPTY);
            target.setStack(i, stack.copy());
        }
    }
}
