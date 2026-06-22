package dev.ic2port.client.screen;

import dev.ic2port.menu.ExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ExtractorScreen extends MachineScreen<ExtractorMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public ExtractorScreen(final ExtractorMenu menu, final Inventory playerInventory, final Component title) {
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
