package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record GuiSlot(@Nullable GuiElementInterface element) {

    private static final GuiSlot EMPTY = GuiSlot.of(ItemStack.EMPTY);


    public static GuiSlot of(GuiElementBuilderInterface<?> element) {
        return new GuiSlot(element.build());
    }


    public static GuiSlot of(ItemStack baseStack) {
        return of(GuiElementBuilder.from(baseStack).hideDefaultTooltip());
    }


    public static GuiElementBuilder builder(Item base, String... text) {
        List<Text> list = new ArrayList<>();
        for (String s : text) {
            list.add(Text.of(s));
        }
        if (list.isEmpty()) {
            list.add(Text.empty());
        }
        return GuiElementBuilder.from(base.getDefaultStack()).setName(list.removeFirst()).setLore(list).hideDefaultTooltip();
    }


    public static GuiSlot nextPage(PagedGui gui) {
        if (gui.canNextPage()) {
            return GuiSlot.of(Config.nextButton.setCallback((x, y, z) -> {
                PagedGui.playClickSound(gui.getPlayer());
                gui.nextPage();
            }));
        } else {
            return EMPTY;
        }
    }


    public static GuiSlot previousPage(PagedGui gui) {

        if (gui.canPreviousPage()) {
            return GuiSlot.of(Config.previousButton.setCallback((x, y, z) -> {
                PagedGui.playClickSound(gui.getPlayer());
                gui.previousPage();
            }));
        } else {
            return EMPTY;
        }
    }


    public static GuiSlot empty() {
        return EMPTY;
    }


    public static GuiSlot back(Runnable back) {
        return GuiSlot.of(Config.backButton.setCallback((x, y, z, d) -> {
            PagedGui.playClickSound(d.getPlayer());
            back.run();
        }));
    }


    public static GuiSlot teleport(ServerPlayerEntity player, Identifier world, Vec3d pos) {
        return GuiSlot.of(Config.teleportButton.setCallback(() -> {
            PagedGui.playClickSound(player);
            MinecraftServer server = player.getServer();
            if (server == null) return;
            ServerWorld toWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, world));
            if (toWorld == null) return;

            player.teleport(toWorld, pos.getX(), pos.getY(), pos.getZ(), 0, 0);
            player.teleport(pos.getX(), pos.getY(), pos.getZ());
            PagedGui.playClickSound(player, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT);
        }));
    }


    public static GuiSlot restore(MinecraftServer server, PlayerBackupData data) {
        ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(data.getUuid());
        if (targetPlayer == null) return GuiSlot.of(Config.restoreOfflineButton);
        else return GuiSlot.of(Config.restoreButton.setCallback(() -> {
            for (int i = 0; i < data.getMainInventory().size(); i++) {
                targetPlayer.getInventory().setStack(i, data.getMainInventory().getStack(i).copy());
            }
            for (int i = 0; i < data.getEnderChest().size(); i++) {
                targetPlayer.getEnderChestInventory().setStack(i, data.getEnderChest().getStack(i).copy());
            }
            targetPlayer.setExperienceLevel(data.getExperienceLevel());
            targetPlayer.setExperiencePoints(
                    (int) (data.getExperienceProgress() * targetPlayer.getNextLevelExperience()));
        }));
    }

}
