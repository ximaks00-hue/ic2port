package dev.ic2port.client.screen;

import dev.ic2port.menu.MetalFormerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MetalFormerScreen extends MachineScreen<MetalFormerMenu> {

    private static final ResourceLocation FURNACE_TEXTURE =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    public MetalFormerScreen(final MetalFormerMenu menu, final Inventory playerInventory, final Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderMachineOverlay(final GuiGraphics guiGraphics, final int x, final int y) {
        if (this.menu.isCrafting()) {
            int pressHeight = this.menu.getProcessedProgressScaled(16);
            guiGraphics.blit(FURNACE_TEXTURE, x + 79, y + 34 + (16 - pressHeight), 176, 14, 24, pressHeight);
        }
        renderEnergyBar(guiGraphics, x, y, this.menu.getEnergyScaled(13));
    }
}
