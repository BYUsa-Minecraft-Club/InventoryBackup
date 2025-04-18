package edu.byu.minecraft.invbackup.data.readplayerdata;

import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.data.SaveDataUtils;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.UUID;

public class ReadPlayerBackupDataStrategyBeforeVersion implements ReadPlayerBackupDataStrategy {

    @Override
    public PlayerBackupData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        UUID uuid = nbt.getIntArray("uuid").map(SaveDataUtils::uuidFromIntArray).orElse(null);
        Long timestamp = nbt.getLong("timestamp").orElse(0L);

        SimpleInventory main = new SimpleInventory(PlayerInventory.MAIN_SIZE);
        SimpleInventory armor = new SimpleInventory(4);
        SimpleInventory offHand = new SimpleInventory(1);
        SimpleInventory enderChest = new SimpleInventory(27);

        main.readNbtList((NbtList) nbt.get("main"), lookup);
        armor.readNbtList((NbtList) nbt.get("armor"), lookup);
        offHand.readNbtList((NbtList) nbt.get("offHand"), lookup);
        enderChest.readNbtList((NbtList) nbt.get("enderChest"), lookup);

        for(int offset : PlayerInventory.EQUIPMENT_SLOTS.keySet()) {
            EquipmentSlot slotId = PlayerInventory.EQUIPMENT_SLOTS.get(offset);
            switch (slotId) {
                case OFFHAND -> main.setStack(offset, offHand.getStack(0));
                case FEET -> main.setStack(offset, armor.getStack(0));
                case LEGS -> main.setStack(offset, armor.getStack(1));
                case CHEST -> main.setStack(offset, armor.getStack(2));
                case HEAD -> main.setStack(offset, armor.getStack(3));
            }
        }

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
