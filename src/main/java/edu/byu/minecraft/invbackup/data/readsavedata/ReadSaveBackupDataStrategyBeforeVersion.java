package edu.byu.minecraft.invbackup.data.readsavedata;

import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.data.SaveData;
import edu.byu.minecraft.invbackup.data.SaveDataUtils;
import edu.byu.minecraft.invbackup.data.readplayerdata.ReadPlayerBackupDataStrategy;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;

import java.util.*;

public class ReadSaveBackupDataStrategyBeforeVersion implements ReadSaveBackupDataStrategy {


    @Override
    public SaveData fromNbt(Optional<Integer> version, NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        SaveData state = new SaveData();
        Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data = new HashMap<>();
        NbtCompound dataNbt = nbt.getCompoundOrEmpty("data");
        dataNbt.getKeys().forEach(key -> {
            EnumMap<LogType, List<PlayerBackupData>> logTypeMap = new EnumMap<>(LogType.class);
            data.put(UUID.fromString(key), logTypeMap);
            NbtCompound worldNbt = dataNbt.getCompoundOrEmpty(key);
            worldNbt.getKeys().forEach(logType -> {
                NbtCompound playerNbt = worldNbt.getCompoundOrEmpty(logType);
                int size = playerNbt.getInt("size").orElse(playerNbt.getSize());
                List<PlayerBackupData> backupDataList =
                        new LinkedList<>(Arrays.stream(new PlayerBackupData[size]).toList());
                logTypeMap.put(LogType.valueOf(logType), backupDataList);
                playerNbt.getKeys().forEach(num -> {
                    if (num.equals("size")) return;
                    backupDataList.set(Integer.parseInt(num),
                            ReadPlayerBackupDataStrategy.forVersion(version).fromNbt(playerNbt.getCompoundOrEmpty(num), lookup));
                });
            });
        });
        state.setData(data);

        Map<UUID, String> players = new HashMap<>();
        NbtList playersNbt = (NbtList) nbt.get("players");
        if(playersNbt != null) playersNbt.forEach(playerNbt -> {
            NbtCompound playerData = (NbtCompound) playerNbt;
            var hi = playerData.get("uuid");
            Optional<UUID> uuid = playerData.getIntArray("uuid").map(SaveDataUtils::uuidFromIntArray);
            Optional<String> ign = playerData.getString("ign");
            if(uuid.isPresent() && ign.isPresent()) {
                players.put(uuid.get(), ign.get());
            }
        });
        state.setPlayers(players);

        return state;
    }
}
