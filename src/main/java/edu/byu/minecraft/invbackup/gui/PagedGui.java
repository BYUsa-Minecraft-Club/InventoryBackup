package edu.byu.minecraft.invbackup.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.MenuType;

public abstract class PagedGui extends SimpleGui {
    public static final int PAGE_SIZE = 9 * 5;
    protected int page = 0;

    public PagedGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
    }

    protected void nextPage() {
        this.page = Math.min(this.getPageAmount() - 1, this.page + 1);
        this.updateDisplay();
    }

    protected boolean canNextPage() {
        return this.getPageAmount() > this.page + 1;
    }

    protected void previousPage() {
        this.page = Math.max(0, this.page - 1);
        this.updateDisplay();
    }

    protected boolean canPreviousPage() {
        return this.page - 1 >= 0;
    }

    protected void updateDisplay() {
        var offset = this.page * PAGE_SIZE;

        for (int i = 0; i < PAGE_SIZE; i++) {
            var element = this.getElement(offset + i);

            if (element == null) {
                element = GuiSlot.empty();
            }

            if (element.element() != null) {
                this.setSlot(i, element.element());
            } else if (element.slot() != null) {
                this.setSlot(i, element.slot());
            }
        }

        for (int i = 0; i < 9; i++) {
            var navElement = this.getNavElement(i);

            if (navElement == null) {
                navElement = GuiSlot.empty();
            }

            if (navElement.element() != null) {
                this.setSlot(i + PAGE_SIZE, navElement.element());
            } else if (navElement.slot() != null) {
                this.setSlot(i + PAGE_SIZE, navElement.slot());
            }
        }
    }

    protected int getPage() {
        return this.page;
    }

    protected abstract int getPageAmount();

    protected abstract GuiSlot getElement(int id);

    protected abstract GuiSlot getNavElement(int id);

    public static void playClickSound(ServerPlayer player) {
        playClickSound(player, SoundEvents.UI_BUTTON_CLICK);
    }

    public static void playClickSound(ServerPlayer player, Holder<SoundEvent> soundEvent) {
        player.connection.send(new ClientboundSoundPacket(
                soundEvent,
                SoundSource.MASTER,
                player.getX(), player.getY(), player.getZ(),
                1, 1,
                player.getRandom().nextLong()
        ));
    }
}
