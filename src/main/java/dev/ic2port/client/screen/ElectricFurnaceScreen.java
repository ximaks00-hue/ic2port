package dev.ic2port.client.screen;

import dev.ic2port.menu.ElectricFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;

public class ElectricFurnaceScreen extends MachineScreen<ElectricFurnaceMenu> {

    public ElectricFurnaceScreen(
            final ElectricFurnaceMenu menu,
            final net.minecraft.world.entity.player.Inventory playerInventory,
            final net.minecraft.network.chat.Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        renderProgressBar(guiGraphics, x, y, this.menu.getProcessedProgressScaled(24));
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
