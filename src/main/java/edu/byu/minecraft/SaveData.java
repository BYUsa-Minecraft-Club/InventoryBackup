package edu.byu.minecraft;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SaveData extends PersistentState {

    private ConcurrentHashMap<Identifier, ConcurrentHashMap<String, List<PlayerInventory>>> data = new ConcurrentHashMap<>();


    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound dataNbt = new NbtCompound();
        data.forEach((worldRegistryKey, playerMap) -> {
            NbtCompound worldNbt = new NbtCompound();
            playerMap.forEach((ign, inventoryList) -> {
                NbtCompound playerNbt = new NbtCompound();
                playerNbt.putInt("size", inventoryList.size());
                for (int i = 0; i < inventoryList.size(); i++) {
                    playerNbt.put(String.valueOf(i), inventoryList.get(i).writeNbt(new NbtList()));
                }
                worldNbt.put(ign, playerNbt);
            });
            dataNbt.put(worldRegistryKey.toString(), worldNbt);
        });
        nbt.put("data", dataNbt);
        return nbt;
    }


    public static SaveData createFromNbt(NbtCompound tag) {
        SaveData state = new SaveData();
        ConcurrentHashMap<Identifier, ConcurrentHashMap<String, List<PlayerInventory>>> data = new ConcurrentHashMap<>();
        NbtCompound dataNbt = tag.getCompound("data");
        dataNbt.getKeys().forEach(key -> {
            ConcurrentHashMap<String, List<PlayerInventory>> playerMap = new ConcurrentHashMap<>();
            data.put(new Identifier(key), playerMap);
            NbtCompound worldNbt = dataNbt.getCompound(key);
            worldNbt.getKeys().forEach(ign -> {
                NbtCompound playerNbt = worldNbt.getCompound(ign);
                int size = playerNbt.getInt("size");
                List<PlayerInventory> inventoryList =
                        new ArrayList<>(Arrays.stream(new PlayerInventory[size]).toList());
                playerMap.put(ign, inventoryList);
                playerNbt.getKeys().forEach(num -> {
                    if (num.equals("size")) return;
                    PlayerInventory playerInventory = new PlayerInventory(null);
                    playerInventory.readNbt(playerNbt.getList(num, 0));
                    inventoryList.set(Integer.parseInt(num), playerInventory);
                });
            });
        });

        state.data = data;
        return state;
    }


    public static SaveData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        //This will break on update, view https://fabricmc.net/wiki/tutorial:persistent_states for new way
        SaveData state =
                persistentStateManager.getOrCreate(SaveData::createFromNbt, SaveData::new, InventoryBackup.MOD_ID);
        state.markDirty();
        return state;
    }


    public void save(ServerPlayerEntity player) {
        if (player.getInventory().isEmpty()) return;

        Identifier worldKey = player.getWorld().getRegistryKey().getValue();
        data.putIfAbsent(worldKey, new ConcurrentHashMap<>());
        Map<String, List<PlayerInventory>> worldMap = data.get(worldKey);

        String playername = player.getEntityName();
        worldMap.putIfAbsent(playername, new ArrayList<>());
        List<PlayerInventory> playerInventories = worldMap.get(playername);

        if(!playerInventories.isEmpty() && equals(playerInventories.get(0), player.getInventory())) return;

        PlayerInventory copy = new PlayerInventory(null);
        copy.clone(player.getInventory());
        playerInventories.add(0, copy);
        while(playerInventories.size() > InventoryBackup.MAX_BACKUP_SIZE) playerInventories.remove(InventoryBackup.MAX_BACKUP_SIZE);
    }

    private boolean equals(PlayerInventory one, PlayerInventory two) {
        for(int i = 0; i < 42; i++) {
            if(!ItemStack.areEqual(one.getStack(i), two.getStack(i))) return false;
        }
        return true;
    }


    public PlayerInventory retrieve(Identifier worldRegistryKey, String ign, int num) {
        try {
            return data.get(worldRegistryKey).get(ign).get(num);
        } catch (Exception e) {
            return null;
        }
    }

}
