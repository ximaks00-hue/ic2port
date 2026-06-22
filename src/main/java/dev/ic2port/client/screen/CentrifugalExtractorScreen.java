package dev.ic2port.client.screen;

import dev.ic2port.menu.CentrifugalExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CentrifugalExtractorScreen extends MachineScreen<CentrifugalExtractorMenu> {

    public CentrifugalExtractorScreen(
            final CentrifugalExtractorMenu menu,
            final Inventory playerInventory,
            final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        if (this.menu.isCrafting()) {
            renderProgressBar(guiGraphics, x, y, this.menu.getProcessedProgressScaled(24));
        }
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
