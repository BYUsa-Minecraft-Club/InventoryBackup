package edu.byu.minecraft.invbackup.gui;

import edu.byu.minecraft.InventoryBackup;
import edu.byu.minecraft.invbackup.data.PlayerBackupData;
import edu.byu.minecraft.invbackup.mixin.PlayerManagerAccessor;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record GuiSlot(@Nullable GuiElementInterface element, @Nullable Slot slot) {

    static final GuiSlot EMPTY = GuiSlot.of(GuiElementBuilder.from(ItemStack.EMPTY));


    public static GuiSlot of(GuiElementBuilderInterface<?> element) {
        return new GuiSlot(element.build(), null);
    }

    public static GuiSlot of(Slot slot) {
        return new GuiSlot(null, slot);
    }

    public static GuiElementBuilder builder(Item base, String... text) {
        return builder(base.getDefaultStack(), text);
    }

    public static GuiElementBuilder builder(ItemStack stack, String... text) {
        List<Text> list = new ArrayList<>();
        for (String s : text) {
            list.add(Text.of(s));
        }
        if (list.isEmpty()) {
            list.add(Text.empty());
        }
        return GuiElementBuilder.from(stack).setName(list.removeFirst()).setLore(list).hideDefaultTooltip();
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


    public static GuiSlot teleport(ServerPlayerEntity player, Identifier world, Vec3d pos, String target) {
        return GuiSlot.of(Config.teleportButton(target).setCallback(() -> {
            PagedGui.playClickSound(player);
            MinecraftServer server = player.getServer();
            if (server == null) return;
            ServerWorld toWorld = server.getWorld(RegistryKey.of(RegistryKeys.WORLD, world));
            if (toWorld == null) return;

            player.teleport(toWorld, pos.getX(), pos.getY(), pos.getZ(), PositionFlag.DELTA, 0, 0, false);
            player.teleport(pos.getX(), pos.getY(), pos.getZ(), false);
            PagedGui.playClickSound(player, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT);
        }));
    }


    public static GuiSlot restore(MinecraftServer server, PlayerBackupData data) {
        String playerName = InventoryBackup.data.getPlayers().get(data.getUuid());
        if(playerName == null) return GuiSlot.empty();
        return GuiSlot.of(Config.restoreButton.setCallback(() -> {
            ServerPlayerEntity target = InventoryBackup.getPlayer(playerName, server);
            data.restore(target);
            InventoryBackup.savePlayerData(target);
        }));
    }

}
