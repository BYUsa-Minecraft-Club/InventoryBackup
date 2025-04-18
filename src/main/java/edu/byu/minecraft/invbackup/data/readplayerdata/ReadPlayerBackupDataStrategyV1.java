package edu.byu.minecraft.invbackup.data.readplayerdata;

import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class ReadPlayerBackupDataStrategyV1 implements ReadPlayerBackupDataStrategy {

    @Override
    public PlayerBackupData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        UUID uuid = nbt.getString("uuid").map(UUID::fromString).orElse(null);
        Long timestamp = nbt.getLong("timestamp").orElse(0L);

        SimpleInventory main = nbtToInv(nbt.getCompoundOrEmpty("main"), lookup);
        SimpleInventory enderChest = nbtToInv(nbt.getCompoundOrEmpty("enderChest"), lookup);

        Integer experienceLevel = nbt.getInt("experienceLevel").orElse(0);
        Integer totalExperience = nbt.getInt("totalExperience").orElse(0);
        Float experienceProgress = nbt.getFloat("experienceProgress").orElse(0f);

        Identifier world = nbt.getString("world").map(Identifier::tryParse).orElse(World.OVERWORLD.getValue());
        Vec3d pos = new Vec3d(nbt.getDouble("xpos").orElse(0.0), nbt.getDouble("ypos").orElse(0.0),
                nbt.getDouble("zpos").orElse(0.0));
        LogType logType = nbt.getString("logType").map(LogType::valueOf).orElse(LogType.FORCE);
        String deathReason = nbt.getString("deathReason").orElse(null);

        return new PlayerBackupData(uuid, timestamp, main, enderChest, experienceLevel, totalExperience,
                experienceProgress, world, pos, logType, deathReason);
    }
}
