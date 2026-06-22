package dev.ic2port.client.screen;

import dev.ic2port.menu.AlloySmelterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlloySmelterScreen extends MachineScreen<AlloySmelterMenu> {

    public AlloySmelterScreen(final AlloySmelterMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        renderProgressBar(guiGraphics, x, y, this.menu.getProgressScaled(24));
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
