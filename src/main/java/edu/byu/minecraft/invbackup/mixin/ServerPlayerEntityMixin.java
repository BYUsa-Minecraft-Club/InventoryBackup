package edu.byu.minecraft.invbackup.mixin;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.LogType;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void injectOnDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = ((ServerPlayerEntity) (Object) this);
        String deathMessage = player.getDamageTracker().getDeathMessage().getString();
        PlayerBackupData backupData = new PlayerBackupData(player, LogType.DEATH, deathMessage);
        InventoryBackup.data.addBackup(backupData);
    }

}
