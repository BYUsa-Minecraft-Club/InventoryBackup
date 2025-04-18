package edu.byu.minecraft.invbackup.data.readplayerdata;

import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;

public interface ReadPlayerBackupDataStrategy {
    PlayerBackupData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup);

    static ReadPlayerBackupDataStrategy forVersion(Optional<Integer> version) {
        if (version.isEmpty()) return new ReadPlayerBackupDataStrategyBeforeVersion();
        return switch (version.get()) {
            case 1 -> new ReadPlayerBackupDataStrategyV1();
            default -> throw new IllegalStateException("Unexpected value: " + version.get());
        };
    }

    default SimpleInventory nbtToInv(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        SimpleInventory inv = new SimpleInventory(nbt.getInt("size").get());
        nbt.getKeys().forEach(entry -> {
            if (entry.equals("size")) return;
            inv.setStack(Integer.parseInt(entry), ItemStack.fromNbt(lookup, nbt.get(entry)).orElse(ItemStack.EMPTY));
        });
        return inv;
    }
}
