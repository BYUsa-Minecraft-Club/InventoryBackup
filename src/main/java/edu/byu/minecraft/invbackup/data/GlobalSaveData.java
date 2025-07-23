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

    private static final Codec<GlobalSaveData> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                    Codec.unboundedMap(Uuids.CODEC, Codec.STRING).fieldOf("players").forGetter(GlobalSaveData::getPlayers))
            .apply(instance, GlobalSaveData::new));

    private Map<UUID, String> players;

    public GlobalSaveData() {
        this(new HashMap<>());
    }

    public GlobalSaveData(Map<UUID, String> players) {
        this.players = new HashMap<>(players);
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
