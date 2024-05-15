package edu.byu.minecraft.invbackup.data;

import edu.byu.minecraft.InventoryBackup;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.*;

public class SaveData extends PersistentState {

    private Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data = new HashMap<>();

    private Map<UUID, String> players = new HashMap<>();


    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound dataNbt = new NbtCompound();
        data.forEach((uuid, playerMap) -> {
            NbtCompound worldNbt = new NbtCompound();
            playerMap.forEach((logType, backupDataList) -> {
                NbtCompound playerNbt = new NbtCompound();
                playerNbt.putInt("size", backupDataList.size());
                for (int i = 0; i < backupDataList.size(); i++) {
                    playerNbt.put(String.valueOf(i), backupDataList.get(i).toNbt(lookup));
                }
                worldNbt.put(logType.name(), playerNbt);
            });
            dataNbt.put(uuid.toString(), worldNbt);
        });
        nbt.put("data", dataNbt);

        NbtList playersNbt = new NbtList();
        players.forEach((uuid, ign) -> {
            NbtCompound playerNbt = new NbtCompound();
            playerNbt.putUuid("uuid", uuid);
            playerNbt.putString("ign", ign);
            playersNbt.add(playerNbt);
        });
        nbt.put("players", playersNbt);

        return nbt;
    }


    public static SaveData createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        SaveData state = new SaveData();

        Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data = new HashMap<>();
        NbtCompound dataNbt = tag.getCompound("data");
        dataNbt.getKeys().forEach(key -> {
            EnumMap<LogType, List<PlayerBackupData>> logTypeMap = new EnumMap<>(LogType.class);
            data.put(UUID.fromString(key), logTypeMap);
            NbtCompound worldNbt = dataNbt.getCompound(key);
            worldNbt.getKeys().forEach(logType -> {
                NbtCompound playerNbt = worldNbt.getCompound(logType);
                int size = playerNbt.getInt("size");
                List<PlayerBackupData> backupDataList =
                        new LinkedList<>(Arrays.stream(new PlayerBackupData[size]).toList());
                logTypeMap.put(LogType.valueOf(logType), backupDataList);
                playerNbt.getKeys().forEach(num -> {
                    if (num.equals("size")) return;
                    backupDataList.set(Integer.parseInt(num), new PlayerBackupData(playerNbt.getCompound(num), lookup));
                });
            });
        });
        state.data = data;

        Map<UUID, String> players = new HashMap<>();
        NbtList playersNbt = (NbtList) tag.get("players");
        playersNbt.forEach(playerNbt -> {
            NbtCompound playerData = (NbtCompound) playerNbt;
            players.put(playerData.getUuid("uuid"), playerData.getString("ign"));
        });
        state.players = players;

        return state;
    }


    public static SaveData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();

        //This will break on update, view https://fabricmc.net/wiki/tutorial:persistent_states for new way
        SaveData state =
                persistentStateManager.getOrCreate(new Type<>(SaveData::new, SaveData::createFromNbt, null),  InventoryBackup.MOD_ID);
        state.markDirty();
        return state;
    }


    public void checkPlayer(ServerPlayerEntity player) {
        players.put(player.getUuid(), player.getGameProfile().getName());
    }


    public void addBackup(PlayerBackupData backup) {
        if (!data.containsKey(backup.getUuid())) {
            data.put(backup.getUuid(), new EnumMap<>(LogType.class));
        }
        if (!data.get(backup.getUuid()).containsKey(backup.getLogType())) {
            data.get(backup.getUuid()).put(backup.getLogType(), new LinkedList<>());
        }
        data.get(backup.getUuid()).get(backup.getLogType()).add(backup);

        int max = switch (backup.getLogType()) {
            case JOIN -> InventoryBackup.maxSavesJoin;
            case QUIT -> InventoryBackup.maxSavesQuit;
            case DEATH -> InventoryBackup.maxSavesDeath;
            case WORLD_CHANGE -> InventoryBackup.maxSavesWorldChange;
            case FORCE -> InventoryBackup.maxSavesForce;
        };

        while (max < data.get(backup.getUuid()).get(backup.getLogType()).size()) {
            data.get(backup.getUuid()).get(backup.getLogType()).removeFirst();
        }
    }


    public Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> getData() {
        return data;
    }


    public Map<UUID, String> getPlayers() {
        return players;
    }

}
