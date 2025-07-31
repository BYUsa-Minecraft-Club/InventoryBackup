package edu.byu.minecraft.invbackup.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edu.byu.minecraft.InventoryBackup;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
                output.put(i, itemStack.copy());
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


    public void restore(ServerPlayerEntity targetPlayer) {
        InventoryBackup.data.addBackup(forPlayer(targetPlayer, LogType.FORCE));

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
