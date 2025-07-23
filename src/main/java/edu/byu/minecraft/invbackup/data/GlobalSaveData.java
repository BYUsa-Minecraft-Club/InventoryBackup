package edu.byu.minecraft.invbackup.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import edu.byu.minecraft.invbackup.mixin.ServerPlayerEntityBackupMixin;
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
import java.util.function.BiConsumer;

public class GlobalSaveData extends PersistentState {

    private static final Codec<GlobalSaveData> NEW_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.unboundedMap(Uuids.CODEC, Codec.STRING).fieldOf("players").forGetter(GlobalSaveData::getPlayers))
            .apply(instance, GlobalSaveData::new));

    private static final Codec<Pair<UUID, String>> OLD_PLAYERS_CODEC = RecordCodecBuilder.create((i) -> i.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(ignored -> UUID.randomUUID()),
            Codec.STRING.fieldOf("ign").forGetter(ignored -> "")
    ).apply(i, Pair::new));

    private static final Codec<PlayerBackupData> OLD_PLAYER_BACKUPS_CODEC = RecordCodecBuilder.create((i) -> i.group(
        Codec.STRING.fieldOf("uuid").forGetter(data -> ""),
        Codec.LONG.fieldOf("timestamp").forGetter(data -> 0L),
        Codec.unboundedMap(Codec.STRING, ItemStack.CODEC).fieldOf("main").forGetter(data -> new HashMap<String, ItemStack>()),
        Codec.unboundedMap(Codec.STRING, ItemStack.CODEC).fieldOf("enderChest").forGetter(data -> new HashMap<String, ItemStack>()),
        Codec.INT.fieldOf("experienceLevel").forGetter(data -> 0),
        Codec.INT.fieldOf("totalExperience").forGetter(data -> 0),
        Codec.FLOAT.fieldOf("experienceProgress").forGetter(data -> 0F),
        Codec.STRING.fieldOf("world").forGetter(data -> ""),
        Codec.DOUBLE.fieldOf("xpos").forGetter(data -> 0D),
        Codec.DOUBLE.fieldOf("ypos").forGetter(data -> 0D),
        Codec.DOUBLE.fieldOf("zpos").forGetter(data -> 0D),
        Codec.STRING.fieldOf("logType").forGetter(data -> ""),
        Codec.STRING.optionalFieldOf("deathReason").forGetter(data -> Optional.empty())
    ).apply(i, (uuidStr, timestamp, main, enderChest, experienceLevel, totalExperience, experienceProgress, world, xpos,
             ypos, zpos, logType, deathReason) -> {
        UUID uuid = UUID.fromString(uuidStr);
        Map<Integer, ItemStack> main2 = new HashMap<>();
        for(Map.Entry<String, ItemStack> entry : main.entrySet()) {
            if(entry.getKey().equals("size")) continue;
            main2.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        Map<Integer, ItemStack> enderChest2 = new HashMap<>();
        for(Map.Entry<String, ItemStack> entry : enderChest.entrySet()) {
            if(entry.getKey().equals("size")) continue;
            enderChest2.put(Integer.parseInt(entry.getKey()), entry.getValue());
        }
        return new PlayerBackupData(uuid, timestamp, main2, enderChest2, experienceLevel, totalExperience,
                experienceProgress, Identifier.tryParse(world), new Vec3d(xpos, ypos, zpos), LogType.valueOf(logType),
                deathReason);
    }));

//    private static final Codec<EnumMap<LogType, List<PlayerBackupData>>> OLD_ENUM_MAP_CODEC = RecordCodecBuilder.create((i) -> i.group(
//        Codec.simpleMap(Codec.STRING, OLD_PLAYER_BACKUPS_CODEC)
//    ));

//    private static final Codec<Map<UUID, EnumMap<LogType, List<PlayerBackupData>>>> OLD_DATA_CODEC = ;

    @Deprecated
    private static final Codec<GlobalSaveData> OLD_CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    OLD_PLAYERS_CODEC.listOf().fieldOf("players").forGetter(ignored -> new ArrayList<>()),
                            Codec.unboundedMap(Codec.STRING,
                                    Codec.unboundedMap(Codec.STRING,
                                            Codec.unboundedMap(Codec.STRING, OLD_PLAYER_BACKUPS_CODEC))
                            ).optionalFieldOf("data").forGetter(data -> Optional.empty())
                    )
            .apply(instance, (pl, optionalMap) -> {
                Map<UUID, String> players = new HashMap<>();
                for (Pair<UUID, String> pair : pl) {
                    players.put(pair.getLeft(), pair.getRight());
                }
                var rawMap = optionalMap.orElse(new HashMap<>());
                Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> map = new HashMap<>();
                for(var entry : rawMap.entrySet()) {
                    EnumMap<LogType, List<PlayerBackupData>> map2 = new EnumMap<>(LogType.class);
                    for(var entry2 : entry.getValue().entrySet()) {
                        TreeMap<Integer, PlayerBackupData> map3 = new TreeMap<>();
                        for(var entry3 : entry2.getValue().entrySet()) {
                            if(entry3.getKey().equals("size")) continue;
                            map3.put(Integer.parseInt(entry3.getKey()), entry3.getValue());
                        }
                        List<PlayerBackupData> list = new ArrayList<>();
                        for(int map3key : map3.keySet()) {
                            list.add(map3.get(map3key));
                        }
                        map2.put(LogType.valueOf(entry2.getKey()), list);
                    }
                    map.put(UUID.fromString(entry.getKey()), map2);
                }

                return new GlobalSaveData(players, map);
            }));

    private static final Codec<GlobalSaveData> CODEC = Codec.withAlternative(NEW_CODEC, OLD_CODEC);

    public Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data;

    private Map<UUID, String> players;

    public GlobalSaveData() {
        this(new HashMap<>());
    }

    public GlobalSaveData(Map<UUID, String> players) {
        this.players = new HashMap<>(players);
    }

    @Deprecated
    public GlobalSaveData(Map<UUID, String> players, Map<UUID, EnumMap<LogType, List<PlayerBackupData>>> data) {
        this(players);
        this.data = data;
    }

    public static GlobalSaveData getServerState(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager()
                .getOrCreate(new PersistentStateType<>(InventoryBackup.MOD_ID, GlobalSaveData::new, CODEC, null));
    }

    public void checkPlayer(ServerPlayerEntity player) {
        var hi = players.get(player.getUuid());
        var hello = player.getGameProfile().getName();
        if (hi == null || !hi.equals(hello)) {
            players.put(player.getUuid(), player.getGameProfile().getName());
            markDirty();
        }
    }


    public Map<UUID, String> getPlayers() {
        return players;
    }

    public void setPlayers(Map<UUID, String> players) {
        this.players = players;
    }
}
