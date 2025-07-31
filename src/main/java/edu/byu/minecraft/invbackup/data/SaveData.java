package edu.byu.minecraft.invbackup.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edu.byu.minecraft.InventoryBackup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.*;

public class SaveData extends PersistentState {

    private static final Codec<SaveData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.unboundedMap(Uuids.CODEC, Codec.unboundedMap(LogType.CODEC,
                            PlayerBackupData.CODEC.listOf())).fieldOf("data").forGetter(SaveData::getData),
                    Codec.unboundedMap(Uuids.CODEC, Codec.STRING).fieldOf("players").forGetter(SaveData::getPlayers))
            .apply(instance, SaveData::new));

    private final Map<UUID, Map<LogType, List<PlayerBackupData>>> data;

    private final Map<UUID, String> players;

    public SaveData() {
        this(new HashMap<>(), new HashMap<>());
    }

    public SaveData(Map<UUID, Map<LogType, List<PlayerBackupData>>> data, Map<UUID, String> players) {
        this.data = new HashMap<>();
        for(var entry : data.entrySet()) {
            Map<LogType, List<PlayerBackupData>> map2 = new EnumMap<>(LogType.class);
            for(var entry2 : entry.getValue().entrySet()) {
                map2.put(entry2.getKey(), new ArrayList<>(entry2.getValue()));
            }
            this.data.put(entry.getKey(), map2);
        }
        this.players = new HashMap<>(players);
    }

    public static SaveData getServerState(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(new PersistentStateType<>(InventoryBackup.MOD_ID, SaveData::new, CODEC, null));
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

    public Map<UUID, Map<LogType, List<PlayerBackupData>>> getData() {
        return data;
    }

    public Map<UUID, String> getPlayers() {
        return players;
    }

}
