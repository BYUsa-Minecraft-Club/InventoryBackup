package edu.byu.minecraft.invbackup.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.readsavedata.ReadSaveBackupDataStrategy;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class SaveData extends PersistentState {

    private static final int CURRENT_VERSION = 1;

    private Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data = new HashMap<>();

    private Map<UUID, String> players = new HashMap<>();


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
            playerNbt.putString("uuid", uuid.toString());
            playerNbt.putString("ign", ign);
            playersNbt.add(playerNbt);
        });
        nbt.put("players", playersNbt);
        nbt.putInt("version", CURRENT_VERSION);

        return nbt;
    }

    public static SaveData createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        Optional<Integer> versionOpt = nbt.getInt("version");
        return ReadSaveBackupDataStrategy.forVersion(versionOpt).fromNbt(versionOpt, nbt, lookup);
    }


    public static SaveData getServerState(MinecraftServer server) {
        PersistentStateManager persistentStateManager = server.getOverworld().getPersistentStateManager();
        return persistentStateManager.getOrCreate(new PersistentStateType<>(InventoryBackup.MOD_ID, SaveData::new,
                codec(server.getOverworld()), null));
    }

    public static Codec<SaveData> codec(final ServerWorld world) {
        return Codec.of(new Encoder<>() {
            public <T> DataResult<T> encode(SaveData input, DynamicOps<T> ops, T prefix) {
                NbtCompound nbtCompound = new NbtCompound();
                input.writeNbt(nbtCompound, world.getRegistryManager());
                return DataResult.success((T) nbtCompound);
            }
        }, new Decoder<>() {
            public <T> DataResult<Pair<SaveData, T>> decode(DynamicOps<T> ops, T input) {
                NbtElement hi = ops.convertTo(NbtOps.INSTANCE, input);
                var hello = SaveData.createFromNbt(hi.asCompound().get(), world.getRegistryManager());
                return DataResult.success(Pair.of(hello, ops.empty()));
            }
        });
    }


    public void checkPlayer(ServerPlayerEntity player) {
        players.put(player.getUuid(), player.getGameProfile().getName());
    }


    public void addBackup(PlayerBackupData backup) {
        if (!data.containsKey(backup.uuid())) {
            data.put(backup.uuid(), new EnumMap<>(LogType.class));
        }
        if (!data.get(backup.uuid()).containsKey(backup.logType())) {
            data.get(backup.uuid()).put(backup.logType(), new LinkedList<>());
        }
        data.get(backup.uuid()).get(backup.logType()).add(backup);

        int max = InventoryBackup.maxSaves(backup.logType());

        while (max < data.get(backup.uuid()).get(backup.logType()).size()) {
            data.get(backup.uuid()).get(backup.logType()).removeFirst();
        }

        markDirty();
    }

    public Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> getData() {
        return data;
    }

    public void setData(Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data) {
        this.data = data;
    }

    public Map<UUID, String> getPlayers() {
        return players;
    }

    public void setPlayers(Map<UUID, String> players) {
        this.players = players;
    }
}
