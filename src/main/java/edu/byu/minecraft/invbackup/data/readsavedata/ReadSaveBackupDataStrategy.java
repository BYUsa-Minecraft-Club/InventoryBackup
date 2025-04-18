package edu.byu.minecraft.invbackup.data.readsavedata;

import edu.byu.minecraft.invbackup.data.SaveData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;

public interface ReadSaveBackupDataStrategy {
    SaveData fromNbt(Optional<Integer> version, NbtCompound nbt, RegistryWrapper.WrapperLookup lookup);

    static ReadSaveBackupDataStrategy forVersion(Optional<Integer> version) {
        if (version.isEmpty()) return new ReadSaveBackupDataStrategyBeforeVersion();
        return switch (version.get()) {
            case 1 -> new ReadSaveBackupDataStrategyV1();
            default -> throw new IllegalStateException("Unexpected value: " + version.get());
        };
    }
}
