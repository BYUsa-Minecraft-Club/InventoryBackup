package edu.byu.minecraft.invbackup.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import edu.byu.minecraft.InventoryBackup;
import net.fabricmc.fabric.impl.attachment.AttachmentPersistentState;
import net.fabricmc.fabric.impl.attachment.AttachmentTargetImpl;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.nio.ByteBuffer;
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
        return createFromNbtV1(versionOpt, nbt, lookup);
        /*The nbt reading for this class didn't directly change for this class (it did for PlayerBackupData)
        when the version system was added, so versionless and v1 are the same. However, if this class and
        the reading from nbt for this class needs to change in a newer version, this method will need to change
        based on the different versions.
        */
    }


    public static SaveData createFromNbtV1(Optional<Integer> versionOpt, NbtCompound tag,
                                           RegistryWrapper.WrapperLookup lookup) {
        SaveData state = new SaveData();
        Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data = new HashMap<>();
        NbtCompound dataNbt = tag.getCompound("data").get();
        dataNbt.getKeys().forEach(key -> {
            EnumMap<LogType, List<PlayerBackupData>> logTypeMap = new EnumMap<>(LogType.class);
            data.put(UUID.fromString(key), logTypeMap);
            NbtCompound worldNbt = dataNbt.getCompound(key).get();
            worldNbt.getKeys().forEach(logType -> {
                NbtCompound playerNbt = worldNbt.getCompound(logType).get();
                int size = playerNbt.getInt("size").get();
                List<PlayerBackupData> backupDataList =
                        new LinkedList<>(Arrays.stream(new PlayerBackupData[size]).toList());
                logTypeMap.put(LogType.valueOf(logType), backupDataList);
                playerNbt.getKeys().forEach(num -> {
                    if (num.equals("size")) return;
                    backupDataList.set(Integer.parseInt(num),
                            PlayerBackupData.fromNbt(versionOpt, playerNbt.getCompound(num).get(), lookup));
                });
            });
        });
        state.data = data;

        Map<UUID, String> players = new HashMap<>();
        NbtList playersNbt = (NbtList) tag.get("players");
        if(playersNbt != null) playersNbt.forEach(playerNbt -> {
            NbtCompound playerData = (NbtCompound) playerNbt;
            var hi = playerData.get("uuid");
            UUID uuid;
            try {
                uuid = UUID.fromString(playerData.getString("uuid").get());
            } catch (Throwable t) {
                uuid = uuidFromIntArray(playerData.getIntArray("uuid").get());
            }
            players.put(uuid, playerData.getString("ign").get());
        });
        state.players = players;

        return state;
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
        if (!data.containsKey(backup.getUuid())) {
            data.put(backup.getUuid(), new EnumMap<>(LogType.class));
        }
        if (!data.get(backup.getUuid()).containsKey(backup.getLogType())) {
            data.get(backup.getUuid()).put(backup.getLogType(), new LinkedList<>());
        }
        data.get(backup.getUuid()).get(backup.getLogType()).add(backup);

        int max = InventoryBackup.maxSaves(backup.getLogType());

        while (max < data.get(backup.getUuid()).get(backup.getLogType()).size()) {
            data.get(backup.getUuid()).get(backup.getLogType()).removeFirst();
        }

        markDirty();
    }


    public Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> getData() {
        return data;
    }


    public Map<UUID, String> getPlayers() {
        return players;
    }

    static UUID uuidFromIntArray(int[] intArray) {
        if (intArray.length != 4) {
            throw new IllegalArgumentException("The integer array must have a length of 4.");
        }

        ByteBuffer buffer = ByteBuffer.allocate(16);
        for (int i : intArray) {
            buffer.putInt(i);
        }

        buffer.rewind();
        long mostSignificantBits = buffer.getLong();
        long leastSignificantBits = buffer.getLong();

        return new UUID(mostSignificantBits, leastSignificantBits);
    }

}
