package edu.byu.minecraft.invbackup.data;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edu.byu.minecraft.InventoryBackup;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public record PlayerBackupData(UUID uuid, Long timestamp, Map<Integer, ItemStack> main, Map<Integer, ItemStack> enderChest,
                               int experienceLevel, int totalExperience, float experienceProgress, Identifier world,
                               Vec3 pos, LogType logType, Optional<String> deathReason) {

    private static final Codec<Map<Integer, ItemStack>> INT_ITEMSTACK_MAP_CODEC =
            Codec.unboundedMap(Codec.STRING.xmap(Integer::parseInt, String::valueOf),
            ItemStack.CODEC);

    public static final Codec<PlayerBackupData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.AUTHLIB_CODEC.fieldOf("uuid").forGetter(PlayerBackupData::uuid),
            Codec.LONG.fieldOf("timestamp").forGetter(PlayerBackupData::timestamp),
            INT_ITEMSTACK_MAP_CODEC.fieldOf("main").forGetter(PlayerBackupData::main),
            INT_ITEMSTACK_MAP_CODEC.fieldOf("enderChest").forGetter(PlayerBackupData::enderChest),
            Codec.INT.fieldOf("experienceLevel").forGetter(PlayerBackupData::experienceLevel),
            Codec.INT.fieldOf("totalExperience").forGetter(PlayerBackupData::totalExperience),
            Codec.FLOAT.fieldOf("experienceProgress").forGetter(PlayerBackupData::experienceProgress),
            Identifier.CODEC.fieldOf("world").forGetter(PlayerBackupData::world),
            Vec3.CODEC.fieldOf("pos").forGetter(PlayerBackupData::pos),
            LogType.CODEC.fieldOf("logType").forGetter(PlayerBackupData::logType),
            Codec.STRING.optionalFieldOf("deathReason").forGetter(PlayerBackupData::deathReason)
        ).apply(instance, PlayerBackupData::new));


    public static PlayerBackupData forPlayer(ServerPlayer player, LogType logType) {
        return forPlayer(player, logType, null);
    }


    public static PlayerBackupData forPlayer(ServerPlayer player, LogType logType, String deathReason) {
        return new PlayerBackupData(player.getUUID(), System.currentTimeMillis(),
                getStacks(player.getInventory()), getStacks(player.getEnderChestInventory()),
                player.experienceLevel, player.totalExperience, player.experienceProgress,
                player.level().dimension().identifier(), player.position(), logType,
                Optional.ofNullable(deathReason));
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


    public void restore(ServerPlayer targetPlayer) {
        InventoryBackup.data.addBackup(forPlayer(targetPlayer, LogType.FORCE));

        restore(main, targetPlayer.getInventory());
        restore(enderChest, targetPlayer.getEnderChestInventory());

        targetPlayer.setExperienceLevels(experienceLevel);
        targetPlayer.setExperiencePoints((int) (experienceProgress * targetPlayer.getXpNeededForNextLevel()));
    }

    private void restore(Map<Integer, ItemStack> source, Container target) {
        for (int i = 0; i < target.getContainerSize(); i++) {
            ItemStack stack = source.getOrDefault(i, ItemStack.EMPTY);
            target.setItem(i, stack.copy());
        }
    }
}
