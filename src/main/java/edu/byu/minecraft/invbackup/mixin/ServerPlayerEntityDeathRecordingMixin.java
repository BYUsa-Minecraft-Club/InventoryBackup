package edu.byu.minecraft.invbackup.mixin;

import edu.byu.minecraft.invbackup.PlayerBackupHolder;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityDeathRecordingMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void injectOnDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
        String deathMessage = player.getDamageTracker().getDeathMessage().getString();
        PlayerBackupData backupData = PlayerBackupData.forPlayer(player, LogType.DEATH, deathMessage);
        ((PlayerBackupHolder) player).inventoryBackup$addBackup(backupData);
    }

}
